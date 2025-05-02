%%% sp_app.erl - Application startup module
-module(sp_app).
-export([start/0, start/2, start/3]).

% Start a new DHT network with a single node
start() ->
    start("node1", 8080).

% Start with a specific node ID and port
start(NodeId, Port) when is_list(NodeId), is_integer(Port) ->
    sp_server:start_link(NodeId, Port).

% Join an existing DHT network
start(NodeId, ContactAddr, Port) when is_list(NodeId), is_integer(Port) ->
    sp_server:start_link(NodeId, ContactAddr, Port).