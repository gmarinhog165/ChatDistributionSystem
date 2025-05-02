%%% sp_tcp_listener.erl
-module(sp_tcp_listener).
-export([start/1]).

start(Port) ->
    {ok, ListenSock} = gen_tcp:listen(Port, [binary, {packet, line}, {active, false}, {reuseaddr, true}]),
    accept_loop(ListenSock).

accept_loop(ListenSock) ->
    {ok, Socket} = gen_tcp:accept(ListenSock),
    spawn(fun() -> handle_client(Socket) end),
    accept_loop(ListenSock).

handle_client(Socket) ->
    client_loop(Socket),
    gen_tcp:close(Socket).

client_loop(Socket) ->
    case gen_tcp:recv(Socket, 0) of
        {ok, <<"GET_TOPICS", _/binary>>} ->
            Topics = sp_server:get_topics(),
            Json = jsonify_list(Topics),
            gen_tcp:send(Socket, list_to_binary(Json ++ "\n")),
            client_loop(Socket);

        {ok, Bin} ->
            case binary:match(Bin, <<"GET_SCS:">>) of
                nomatch ->
                    gen_tcp:send(Socket, <<"ERROR\n">>),
                    client_loop(Socket);
                _ ->
                    [_, TopicBin] = binary:split(Bin, <<":">>),
                    Trimmed = re:replace(TopicBin, <<"\r?\n$">>, <<>>, [global, {return, binary}]),
                    Topic = binary_to_list(Trimmed),
                    SCs = sp_server:get_scs(Topic),
                    Json = jsonify_list(SCs),
                    gen_tcp:send(Socket, list_to_binary(Json ++ "\n")),
                    client_loop(Socket)
            end;

        {error, closed} ->
            ok
    end.

%% Converts list of strings or tuples to JSON-like string
jsonify_list(List) ->
    JsonStrings = [begin
                       case E of
                           {Ip, Port} ->
                               io_lib:format("{\"ip\":\"~s\",\"port\":~p}", [Ip, Port]);
                           _ when is_list(E) ->
                               io_lib:format("\"~s\"", [E]);
                           _ ->
                               io_lib:format("\"~p\"", [E])
                       end
                   end || E <- List],
    "[" ++ string:join([lists:flatten(S) || S <- JsonStrings], ",") ++ "]".
