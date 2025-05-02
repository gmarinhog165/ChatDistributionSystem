%%% sp_tcp_listener.erl - TCP listener for client connections
-module(sp_tcp_listener).
-export([start/1]).

start(Port) ->
    io:format("Starting TCP listener on port ~p~n", [Port]),
    case gen_tcp:listen(Port, [binary, {packet, line}, {active, false}, {reuseaddr, true}]) of
        {ok, ListenSock} ->
            io:format("Successfully created listen socket~n"),
            accept_loop(ListenSock);
        {error, Reason} ->
            io:format("Failed to create listen socket: ~p~n", [Reason]),
            {error, Reason}
    end.

accept_loop(ListenSock) ->
    io:format("Waiting for client connection...~n"),
    case gen_tcp:accept(ListenSock) of
        {ok, Socket} ->
            io:format("Accepted new client connection~n"),
            Pid = spawn(fun() -> handle_client(Socket) end),
            io:format("Spawned client handler process: ~p~n", [Pid]),
            accept_loop(ListenSock);
        {error, Reason} ->
            io:format("Error accepting connection: ~p~n", [Reason]),
            timer:sleep(1000),
            accept_loop(ListenSock)
    end.

handle_client(Socket) ->
    io:format("Client handler started for socket: ~p~n", [Socket]),
    client_loop(Socket),
    io:format("Client loop ended, closing socket~n"),
    gen_tcp:close(Socket).

client_loop(Socket) ->
    io:format("Waiting for client message...~n"),
    case gen_tcp:recv(Socket, 0) of
        {ok, <<"REGISTER_TOPIC:", Rest/binary>>} ->
            io:format("Received REGISTER_TOPIC command: ~p~n", [Rest]),
            try
                % Only split on the first colon to separate topic and SCs list
                case binary:split(Rest, <<":">>) of
                    [TopicBin, SCsBin] ->
                        % Clean the topic name
                        Topic = binary_to_list(re:replace(TopicBin, <<"\r?\n$">>, <<>>, [global, {return, binary}])),
                        io:format("Parsed topic name: '~s'~n", [Topic]),
                        
                        % Clean the SCs binary
                        CleanSCsBin = re:replace(SCsBin, <<"\r?\n$">>, <<>>, [global, {return, binary}]),
                        io:format("Cleaned SCs binary: ~p~n", [CleanSCsBin]),
                        
                        % Split the SC list by commas
                        SCsList = binary:split(CleanSCsBin, <<",">>, [global]),
                        io:format("SC list parts: ~p~n", [SCsList]),
                        
                        % Parse each SC into {IP, Port} tuple
                        SCs = lists:map(
                            fun(SCBin) ->
                                case binary:split(SCBin, <<":">>) of
                                    [IPBin, PortBin] ->
                                        IP = binary_to_list(IPBin),
                                        % Convert binary port to string, then to integer
                                        PortStr = binary_to_list(PortBin),
                                        io:format("Port string: ~p~n", [PortStr]),
                                        Port = list_to_integer(PortStr),
                                        io:format("Parsed SC: {~s, ~p}~n", [IP, Port]),
                                        {IP, Port};
                                    Other ->
                                        io:format("Invalid SC format: ~p~n", [Other]),
                                        throw({error, invalid_sc_format})
                                end
                            end,
                            SCsList
                        ),
                        
                        % Call the server module to register the topic
                        io:format("Calling sp_server:register_topic(~p, ~p)~n", [Topic, SCs]),
                        Result = sp_server:register_topic(Topic, SCs),
                        io:format("Registration result: ~p~n", [Result]),
                        gen_tcp:send(Socket, <<"OK\n">>);
                    Other ->
                        io:format("Invalid format for REGISTER_TOPIC, parts: ~p~n", [Other]),
                        gen_tcp:send(Socket, <<"ERROR: Invalid topic:scs format\n">>)
                end
            catch
                E:R:S ->
                    io:format("Exception during REGISTER_TOPIC: ~p:~p~nStacktrace: ~p~n", [E, R, S]),
                    gen_tcp:send(Socket, <<"ERROR: Invalid format or internal error\n">>)
            end,
            client_loop(Socket);

        {ok, <<"GET_TOPICS", _/binary>>} ->
            io:format("Received GET_TOPICS command~n"),
            try
                Topics = sp_server:get_topics(),
                io:format("Retrieved topics: ~p~n", [Topics]),
                Json = jsonify_list(Topics),
                io:format("Sending JSON response: ~s~n", [Json]),
                gen_tcp:send(Socket, list_to_binary(Json ++ "\n"))
            catch
                E:R:S ->
                    io:format("Exception during GET_TOPICS: ~p:~p~nStacktrace: ~p~n", [E, R, S]),
                    gen_tcp:send(Socket, <<"ERROR: Internal server error\n">>)
            end,
            client_loop(Socket);

        {ok, Bin} ->
            io:format("Received raw message: ~p~n", [Bin]),
            case binary:match(Bin, <<"GET_SCS:">>) of
                nomatch ->
                    io:format("Unknown command, sending ERROR~n"),
                    gen_tcp:send(Socket, <<"ERROR\n">>),
                    client_loop(Socket);
                {_, _} ->
                    try
                        io:format("Processing GET_SCS command~n"),
                        % Only split on the first colon
                        [_, TopicBin] = binary:split(Bin, <<":">>),
                        io:format("Raw topic binary: ~p~n", [TopicBin]),
                        Trimmed = re:replace(TopicBin, <<"\r?\n$">>, <<>>, [global, {return, binary}]),
                        Topic = binary_to_list(Trimmed),
                        io:format("Looking up SCs for topic: '~s'~n", [Topic]),
                        SCs = sp_server:get_scs(Topic),
                        io:format("Retrieved SCs: ~p~n", [SCs]),
                        Json = jsonify_list(SCs),
                        io:format("Sending JSON response: ~s~n", [Json]),
                        gen_tcp:send(Socket, list_to_binary(Json ++ "\n"))
                    catch
                        E:R:S ->
                            io:format("Exception during GET_SCS: ~p:~p~nStacktrace: ~p~n", [E, R, S]),
                            gen_tcp:send(Socket, <<"ERROR: Topic format error or internal error\n">>)
                    end,
                    client_loop(Socket)
            end;

        {error, closed} ->
            io:format("Connection closed by client~n"),
            ok;
            
        {error, Reason} ->
            io:format("Error receiving data: ~p~n", [Reason]),
            ok
    end.

%% Converts list of strings or tuples to JSON-like string
jsonify_list(List) ->
    io:format("Converting to JSON: ~p~n", [List]),
    JsonStrings = [begin
                       case E of
                           {Ip, Port} ->
                               io:format("Converting SC: {~s, ~p}~n", [Ip, Port]),
                               io_lib:format("{\"ip\":\"~s\",\"port\":~p}", [Ip, Port]);
                           _ when is_list(E) ->
                               io:format("Converting string: ~s~n", [E]),
                               io_lib:format("\"~s\"", [E]);
                           _ ->
                               io:format("Converting other: ~p~n", [E]),
                               io_lib:format("\"~p\"", [E])
                       end
                   end || E <- List],
    Result = "[" ++ string:join([lists:flatten(S) || S <- JsonStrings], ",") ++ "]",
    io:format("JSON result: ~s~n", [Result]),
    Result.