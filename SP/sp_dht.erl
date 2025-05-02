%%% sp_dht.erl - DHT implementation for search servers (SP)
-module(sp_dht).
-export([
    init/3, 
    join/3,
    hash_key/1, 
    hash_node/1,
    find_responsible_nodes/2, 
    is_responsible/3,
    get_node_info/0,
    get_ring/0,
    update_ring/1
]).

-define(HASH_MOD, 1024).

% Initialize the DHT
init(NodeId, NodeAddr, ReplicationFactor) ->
    case whereis(sp_dht_proc) of
        undefined ->
            Pid = spawn(fun() -> dht_loop(NodeId, NodeAddr, ReplicationFactor) end),
            register(sp_dht_proc, Pid);
        _ ->
            ok
    end,
    self_register(NodeId, NodeAddr).

% Join an existing DHT through a known node
join(ContactNodeAddr, NodeId, NodeAddr) ->
    io:format("Joining network through ~p~n", [ContactNodeAddr]),
    init(NodeId, NodeAddr, 3),

    ContactIp = element(1, ContactNodeAddr),
    ContactPort = element(2, ContactNodeAddr) + 1,

    io:format("Connecting to contact node at ~p:~p~n", [ContactIp, ContactPort]),

    case gen_tcp:connect(ContactIp, ContactPort, [binary, {packet, 4}, {active, false}], 5000) of
        {ok, Socket} ->
            io:format("Connected to contact node~n"),
            Message = term_to_binary({get_ring, NodeId, NodeAddr}),
            io:format("Sending get_ring message to contact node~n"),
            case gen_tcp:send(Socket, Message) of
                ok ->
                    io:format("Message sent successfully, waiting for response~n"),
                    case gen_tcp:recv(Socket, 0, 10000) of
                        {ok, Response} ->
                            io:format("Received response from contact node~n"),
                            Ring = binary_to_term(Response),
                            io:format("Parsed ring data: ~p~n", [Ring]),
                            gen_tcp:close(Socket),
                            update_ring(Ring),
                            ok;
                        {error, Reason} ->
                            io:format("Error receiving response: ~p~n", [Reason]),
                            gen_tcp:close(Socket),
                            {error, Reason}
                    end;
                {error, SendError} ->
                    io:format("Error sending message: ~p~n", [SendError]),
                    gen_tcp:close(Socket),
                    {error, SendError}
            end;
        {error, ConnectError} ->
            io:format("Failed to connect to contact node: ~p~n", [ConnectError]),
            {error, ConnectError}
    end.

% Hash a key (topic name)
hash_key(Key) ->
    HashBin = crypto:hash(sha, term_to_binary(Key)),
    <<Int:160/integer>> = HashBin,
    Int rem ?HASH_MOD.

% Hash a node using {NodeId, IP, Port}
hash_node({NodeId, IP, Port}) ->
    Composite = {NodeId, IP, Port},
    HashBin = crypto:hash(sha, term_to_binary(Composite)),
    <<Int:160/integer>> = HashBin,
    Int rem ?HASH_MOD.

% Find responsible nodes
find_responsible_nodes(Key, ReplicationFactor) ->
    KeyHash = hash_key(Key),
    io:format("hash_key for ~p: ~p~n", [Key, KeyHash]),
    sp_dht_proc ! {find_responsible, Key, KeyHash, ReplicationFactor, self()},
    receive
        {responsible_nodes, Nodes} -> Nodes
    after 5000 ->
        []
    end.

% Check responsibility
is_responsible(Key, NodeId, ReplicationFactor) ->
    ResponsibleNodes = find_responsible_nodes(Key, ReplicationFactor),
    lists:member(NodeId, [Id || {Id, _} <- ResponsibleNodes]).

% Get node info
get_node_info() ->
    sp_dht_proc ! {get_node_info, self()},
    receive
        {node_info, NodeId, NodeAddr} -> {NodeId, NodeAddr}
    after 5000 ->
        {error, timeout}
    end.

% Get ring
get_ring() ->
    sp_dht_proc ! {get_ring, self()},
    receive
        {ring, Ring} -> Ring
    after 5000 ->
        []
    end.

% Update ring
update_ring(NewRing) ->
    sp_dht_proc ! {update_ring, NewRing},
    ok.

% Register self
self_register(NodeId, NodeAddr) ->
    IP = element(1, NodeAddr),
    Port = element(2, NodeAddr),
    NodeHash = hash_node({NodeId, IP, Port}),
    io:format("Registering node: ~p with hash ~p~n", [NodeId, NodeHash]),
    sp_dht_proc ! {register_node, NodeId, NodeAddr, NodeHash},
    ok.

% Main DHT loop
dht_loop(NodeId, NodeAddr, ReplicationFactor) ->
    IP = element(1, NodeAddr),
    Port = element(2, NodeAddr),
    NodeHash = hash_node({NodeId, IP, Port}),
    io:format("Initial node hash for ~p: ~p~n", [NodeId, NodeHash]),
    dht_loop(NodeId, NodeAddr, ReplicationFactor, [{NodeHash, NodeId, NodeAddr}]).

dht_loop(NodeId, NodeAddr, ReplicationFactor, Ring) ->
    receive
        {register_node, Id, Addr, Hash} ->
            NewRing = add_to_ring({Hash, Id, Addr}, Ring),
            io:format("Ring updated after registering ~p: ~p~n", [Id, NewRing]),
            dht_loop(NodeId, NodeAddr, ReplicationFactor, NewRing);

        {update_ring, NewRing} ->
            io:format("Ring updated with external data: ~p~n", [NewRing]),
            dht_loop(NodeId, NodeAddr, ReplicationFactor, NewRing);

        {get_ring, Pid} ->
            Pid ! {ring, Ring},
            dht_loop(NodeId, NodeAddr, ReplicationFactor, Ring);

        {get_node_info, Pid} ->
            Pid ! {node_info, NodeId, NodeAddr},
            dht_loop(NodeId, NodeAddr, ReplicationFactor, Ring);

        {find_responsible, Key, KeyHash, RF, Pid} ->
            ResponsibleNodes = find_unique_successors(KeyHash, Ring, RF),
            io:format("Find responsible for key ~p (hash: ~p): ~p~n", [Key, KeyHash, ResponsibleNodes]),
            Pid ! {responsible_nodes, ResponsibleNodes},
            dht_loop(NodeId, NodeAddr, ReplicationFactor, Ring)
    end.

% Add to ring
add_to_ring(NewNode = {_Hash, Id, _Addr}, Ring) ->
    FilteredRing = lists:filter(
        fun({_, NodeId, _}) -> NodeId =/= Id end,
        Ring
    ),
    lists:sort(
        fun({Hash1, _, _}, {Hash2, _, _}) -> Hash1 =< Hash2 end, 
        [NewNode | FilteredRing]
    ).

% Find N unique successors
find_unique_successors(Hash, Ring, N) ->
    SortedRing = lists:sort(fun({Hash1, _, _}, {Hash2, _, _}) -> Hash1 =< Hash2 end, Ring),
    find_unique_successors_impl(Hash, SortedRing, N, [], sets:new()).

find_unique_successors_impl(_, _, 0, Acc, _) ->
    lists:reverse(Acc);
find_unique_successors_impl(_Hash, [], N, Acc, _Seen) when N > 0 ->
    lists:reverse(Acc);
find_unique_successors_impl(Hash, SortedRing, N, Acc, Seen) ->
    {NewAcc, NewSeen, Remaining} = find_successors_pass(
        Hash, SortedRing, N, Acc, Seen, 
        fun({NodeHash, _, _}) -> NodeHash >= Hash end
    ),
    if 
        Remaining > 0 ->
            {_FinalAcc, FinalSeen, _} = find_successors_pass(
                Hash, SortedRing, Remaining, NewAcc, NewSeen, 
                fun(_) -> true end
            ),
            lists:reverse(_FinalAcc);
        true ->
            lists:reverse(NewAcc)
    end.

find_successors_pass(_Hash, SortedRing, N, Acc, Seen, FilterFun) ->
    lists:foldl(
        fun(Node = {_, NodeId, NodeAddr}, {AccNodes, SeenIds, Count}) ->
            if 
                Count =< 0 ->
                    {AccNodes, SeenIds, Count};
                true ->
                    case FilterFun(Node) of
                        false ->
                            {AccNodes, SeenIds, Count};
                        true ->
                            case sets:is_element(NodeId, SeenIds) of
                                true -> 
                                    {AccNodes, SeenIds, Count};
                                false -> 
                                    {[{NodeId, NodeAddr}|AccNodes], 
                                     sets:add_element(NodeId, SeenIds), 
                                     Count-1}
                            end
                    end
            end
        end,
        {Acc, Seen, N},
        SortedRing
    ).
