%%% sp_server.erl - Main server module for Search Provider (SP)
-module(sp_server).
-behaviour(gen_server).

%% API
-export([
    start_link/2, 
    start_link/3, 
    get_topics/0,
    get_local_topics/0,
    get_scs/1, 
    register_topic/2,
    join_network/2
]).

%% gen_server callbacks
-export([
    init/1, 
    handle_call/3, 
    handle_cast/2, 
    handle_info/2, 
    terminate/2, 
    code_change/3
]).

-record(state, {
    node_id,              % This node's identifier
    replication_factor,   % Number of replicas
    local_topics = #{},   % Topics this node is primarily responsible for
    replicated_topics = #{} % Topics this node holds as replicas
}).

%%% === API Functions ===

% Start a new DHT network
start_link(NodeId, Port) ->
    start_link(NodeId, undefined, Port).

% Join an existing DHT network
start_link(NodeId, ContactNode, Port) ->
    gen_server:start_link({local, ?MODULE}, ?MODULE, [NodeId, ContactNode, Port], []).

% Get all topics from all nodes in the network
get_topics() ->
    % First get local topics
    LocalTopics = get_local_topics(),
    
    % Get our node info to avoid querying ourselves
    {MyNodeId, _} = sp_dht:get_node_info(),
    
    % Get list of physical nodes (deduplicated)
    PhysicalNodes = sp_dht:get_physical_nodes(),
    
    % Get topics from all other physical nodes
    AllTopics = lists:foldl(
        fun({NodeId, NodeAddr}, Acc) ->
            % Skip our own node
            case NodeId of
                MyNodeId -> 
                    Acc;
                _ ->
                    case sp_node_comm:get_remote_topics(NodeAddr) of
                        {ok, RemoteTopics} ->
                            Acc ++ RemoteTopics;
                        {error, _} ->
                            % If we can't connect to a node, just continue
                            Acc
                    end
            end
        end,
        LocalTopics,
        PhysicalNodes
    ),
    
    % Return unique topics
    lists:usort(AllTopics).

% Get only the topics managed by this node
get_local_topics() ->
    gen_server:call(?MODULE, get_local_topics).

get_scs(Topic) ->
    gen_server:call(?MODULE, {get_scs, Topic}).

register_topic(Topic, SCs) ->
    gen_server:cast(?MODULE, {register_topic, Topic, SCs}).

join_network(ContactNodeAddr, Port) ->
    gen_server:cast(?MODULE, {join_network, ContactNodeAddr, Port}).

%%% === Callbacks ===

init([NodeId, ContactNode, Port]) ->
    % Initialize our node
    NodeAddr = {"127.0.0.1", Port},
    ReplicationFactor = 3,
    
    % Start the TCP listener for client connections
    spawn(fun() -> sp_tcp_listener:start(Port) end),
    
    % Start the inter-node communication server
    spawn(fun() -> sp_node_comm:start(Port + 1) end),
    
    % Inicialização do DHT deve vir depois dos servidores TCP estarem rodando
    timer:sleep(500), % Pequeno delay para garantir que os servidores TCP estejam prontos
    
    % Initialize our DHT component
    case ContactNode of
        undefined ->
            % Start a new DHT network
            io:format("Initializing as first node in network: ~p~n", [NodeId]),
            sp_dht:init(NodeId, NodeAddr, ReplicationFactor);
        _ ->
            % Join an existing network
            io:format("Joining existing network through: ~p~n", [ContactNode]),
            case sp_dht:join(ContactNode, NodeId, NodeAddr) of
                ok ->
                    io:format("Successfully joined network~n");
                {error, Reason} ->
                    io:format("Failed to join network: ~p~n", [Reason])
            end
    end,
    
    {ok, #state{node_id = NodeId, replication_factor = ReplicationFactor}}.

handle_call(get_local_topics, _From, State) ->
    % Combine local and replicated topics
    AllTopics = maps:keys(State#state.local_topics) ++ maps:keys(State#state.replicated_topics),
    UniqueTopics = lists:usort(AllTopics),
    {reply, UniqueTopics, State};

handle_call({get_scs, Topic}, _From, State) ->
    % Check if we have this topic locally
    case maps:get(Topic, State#state.local_topics, not_found) of
        not_found ->
            % Check if we have it as a replica
            case maps:get(Topic, State#state.replicated_topics, not_found) of
                not_found ->
                    % We don't have it - find responsible node and forward
                    case sp_dht:is_responsible(Topic, State#state.node_id, State#state.replication_factor) of
                        true ->
                            % We should have it but don't - return empty list
                            {reply, [], State};
                        false ->
                            % Forward to responsible node
                            ResponsibleNodes = sp_dht:find_responsible_nodes(Topic, 1),
                            case ResponsibleNodes of
                                [{NodeId, NodeAddr}|_] ->
                                    case sp_node_comm:forward_get_scs(NodeAddr, Topic) of
                                        {ok, SCs} -> {reply, SCs, State};
                                        _ -> {reply, [], State}
                                    end;
                                [] ->
                                    {reply, [], State}
                            end
                    end;
                SCs ->
                    {reply, SCs, State}
            end;
        SCs ->
            {reply, SCs, State}
    end;

handle_call(_, _From, State) ->
    {reply, error, State}.

handle_cast({register_topic, Topic, SCs}, State) ->
    % Determine if this node is responsible for the topic
    case sp_dht:is_responsible(Topic, State#state.node_id, State#state.replication_factor) of
        true ->
            % Store locally
            NewLocalTopics = maps:put(Topic, SCs, State#state.local_topics),
            
            % Replicate to other responsible nodes
            ResponsibleNodes = sp_dht:find_responsible_nodes(Topic, State#state.replication_factor),
            
            % Send to each replica (excluding ourselves)
            lists:foreach(
                fun({NodeId, NodeAddr}) ->
                    if 
                        NodeId =/= State#state.node_id ->
                            sp_node_comm:replicate_topic(NodeAddr, Topic, SCs);
                        true -> ok
                    end
                end,
                ResponsibleNodes
            ),
            
            {noreply, State#state{local_topics = NewLocalTopics}};
        false ->
            % Forward to a responsible node
            ResponsibleNodes = sp_dht:find_responsible_nodes(Topic, 1),
            case ResponsibleNodes of
                [{_NodeId, NodeAddr}|_] ->
                    sp_node_comm:forward_register_topic(NodeAddr, Topic, SCs),
                    {noreply, State}; % ADDED MISSING RETURN VALUE
                [] ->
                    % Fallback - store locally if no node found
                    NewLocalTopics = maps:put(Topic, SCs, State#state.local_topics),
                    {noreply, State#state{local_topics = NewLocalTopics}}
            end
    end;

handle_cast({replicate_topic, Topic, SCs}, State) ->
    % Store as a replica
    NewReplicatedTopics = maps:put(Topic, SCs, State#state.replicated_topics),
    {noreply, State#state{replicated_topics = NewReplicatedTopics}};

handle_cast({join_network, ContactNodeAddr, Port}, State) ->
    % Join the DHT network
    NodeAddr = {"127.0.0.1", Port},
    case sp_dht:join(ContactNodeAddr, State#state.node_id, NodeAddr) of
        ok ->
            io:format("Successfully joined network via explicit call~n");
        {error, Reason} ->
            io:format("Failed to join network via explicit call: ~p~n", [Reason])
    end,
    {noreply, State};

handle_cast(_, State) ->
    {noreply, State}.

handle_info({redistribute_topics, NewNodeId, NewNodeAddr}, State) ->
    % Check which of our local topics should now be handled by the new node
    % We should only redistribute topics that this node is no longer
    % the primary responsible node for (first in the responsible list)
    TopicsToRedistribute = maps:fold(
        fun(Topic, SCs, Acc) ->
            % Check if we're still the primary responsible node
            ResponsibleNodes = sp_dht:find_responsible_nodes(Topic, 1),
            case ResponsibleNodes of
                [{NodeId, _}|_] when NodeId =:= NewNodeId ->
                    % New node is now primary, we should redistribute
                    [{Topic, SCs}|Acc];
                _ ->
                    % We or someone else is still primary, keep it
                    Acc
            end
        end,
        [],
        State#state.local_topics
    ),
    
    % Send topics to the new node
    lists:foreach(
        fun({Topic, SCs}) ->
            sp_node_comm:replicate_topic(NewNodeAddr, Topic, SCs)
        end,
        TopicsToRedistribute
    ),
    
    % Remove redistributed topics from our local state
    NewLocalTopics = lists:foldl(
        fun({Topic, _}, Map) ->
            maps:remove(Topic, Map)
        end,
        State#state.local_topics,
        TopicsToRedistribute
    ),
    
    {noreply, State#state{local_topics = NewLocalTopics}};

handle_info(_, State) ->
    {noreply, State}.

terminate(_, _) -> ok.
code_change(_, State, _) -> {ok, State}.