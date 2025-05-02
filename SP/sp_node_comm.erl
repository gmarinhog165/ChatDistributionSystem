%%% sp_node_comm.erl - Inter-node communication for SP nodes
-module(sp_node_comm).
-export([
    start/1,
    forward_get_scs/2,
    forward_register_topic/3,
    replicate_topic/3,
    broadcast_ring_update/1,
    get_remote_topics/1
]).

% Start the inter-node communication server
start(Port) ->
    {ok, ListenSock} = gen_tcp:listen(Port, [binary, {packet, 4}, {active, false}, {reuseaddr, true}]),
    accept_loop(ListenSock).

% Forward a get_scs request to another node
forward_get_scs({Ip, Port}, Topic) ->
    try
        {ok, Socket} = gen_tcp:connect(Ip, Port + 1, [binary, {packet, 4}, {active, false}]),
        Request = term_to_binary({get_scs, Topic}),
        ok = gen_tcp:send(Socket, Request),
        {ok, Response} = gen_tcp:recv(Socket, 0),
        gen_tcp:close(Socket),
        {ok, binary_to_term(Response)}
    catch
        _:_ -> {error, connection_failed}
    end.

% Forward a register_topic request to another node
forward_register_topic({Ip, Port}, Topic, SCs) ->
    try
        {ok, Socket} = gen_tcp:connect(Ip, Port + 1, [binary, {packet, 4}, {active, false}]),
        Request = term_to_binary({register_topic, Topic, SCs}),
        ok = gen_tcp:send(Socket, Request),
        gen_tcp:close(Socket),
        ok
    catch
        _:_ -> {error, connection_failed}
    end.

% Replicate a topic to another node
replicate_topic({Ip, Port}, Topic, SCs) ->
    try
        {ok, Socket} = gen_tcp:connect(Ip, Port + 1, [binary, {packet, 4}, {active, false}]),
        Request = term_to_binary({replicate_topic, Topic, SCs}),
        ok = gen_tcp:send(Socket, Request),
        gen_tcp:close(Socket),
        ok
    catch
        _:_ -> {error, connection_failed}
    end.

% Get topics from a remote node
get_remote_topics({Ip, Port}) ->
    try
        {ok, Socket} = gen_tcp:connect(Ip, Port + 1, [binary, {packet, 4}, {active, false}]),
        Request = term_to_binary({get_topics}),
        ok = gen_tcp:send(Socket, Request),
        {ok, Response} = gen_tcp:recv(Socket, 0),
        gen_tcp:close(Socket),
        {ok, binary_to_term(Response)}
    catch
        E:R ->
            io:format("Error retrieving topics from remote node (~p:~p): ~p:~p~n", [Ip, Port, E, R]),
            {error, connection_failed}
    end.

% Broadcast ring update to all nodes in the ring
broadcast_ring_update(Ring) ->
    % Get unique physical nodes from the ring
    UniqueNodes = lists:foldl(
        fun({_, NodeId, {Ip, Port}, _}, Acc) ->
            case lists:keymember(NodeId, 1, Acc) of
                true -> Acc;
                false -> [{NodeId, {Ip, Port}} | Acc]
            end
        end,
        [],
        Ring
    ),
    
    % Send update to each unique physical node
    lists:foreach(
        fun({_, {Ip, Port}}) ->
            try
                {ok, Socket} = gen_tcp:connect(Ip, Port + 1, [binary, {packet, 4}, {active, false}]),
                Request = term_to_binary({update_ring, Ring}),
                ok = gen_tcp:send(Socket, Request),
                gen_tcp:close(Socket)
            catch
                _:_ -> ok % Ignore connection failures
            end
        end,
        UniqueNodes
    ).

%% Private functions

% Accept loop for incoming connections
accept_loop(ListenSock) ->
    {ok, Socket} = gen_tcp:accept(ListenSock),
    spawn(fun() -> handle_node_request(Socket) end),
    accept_loop(ListenSock).

% Handle requests from other nodes
handle_node_request(Socket) ->
    case gen_tcp:recv(Socket, 0) of
        {ok, Data} ->
            case binary_to_term(Data) of
                {get_scs, Topic} ->
                    SCs = sp_server:get_scs(Topic),
                    Response = term_to_binary(SCs),
                    gen_tcp:send(Socket, Response);
                    
                {get_topics} ->
                    % Return only local and replicated topics, not a recursive call
                    Topics = sp_server:get_local_topics(),
                    Response = term_to_binary(Topics),
                    gen_tcp:send(Socket, Response);
                    
                {register_topic, Topic, SCs} ->
                    sp_server:register_topic(Topic, SCs);
                    
                {replicate_topic, Topic, SCs} ->
                    gen_server:cast(sp_server, {replicate_topic, Topic, SCs});
                    
                {update_ring, Ring} ->
                    sp_dht:update_ring(Ring);
                    
                {get_ring, NodeId, NodeAddr} ->
                    % A new node is joining
                    CurrentRing = sp_dht:get_ring(),
                    {Ip, Port} = NodeAddr,
                    
                    % Create virtual nodes for the new physical node
                    VNodes = lists:map(
                        fun(VNodeIndex) ->
                            Hash = sp_dht:hash_node({NodeId, Ip, Port}, VNodeIndex),
                            {Hash, NodeId, NodeAddr, VNodeIndex}
                        end,
                        lists:seq(0, 19) % 20 virtual nodes
                    ),
                    
                    % Combine with current ring
                    NewRing = VNodes ++ CurrentRing,
                    
                    % Sort by hash
                    SortedRing = lists:sort(
                        fun({Hash1, _, _, _}, {Hash2, _, _, _}) -> Hash1 =< Hash2 end, 
                        NewRing
                    ),
                    
                    % Send the ring information back
                    Response = term_to_binary(SortedRing),
                    gen_tcp:send(Socket, Response),
                    
                    % Update our ring and broadcast to others
                    sp_dht:update_ring(SortedRing),
                    broadcast_ring_update(SortedRing),
                    
                    % Notify the server to redistribute topics
                    sp_server ! {redistribute_topics, NodeId, NodeAddr}
            end;
            
        {error, closed} ->
            ok
    end,
    gen_tcp:close(Socket).