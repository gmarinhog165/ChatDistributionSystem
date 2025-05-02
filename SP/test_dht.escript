#!/usr/bin/env escript
%% -*- erlang -*-
%%! -smp enable -sname sp_test

main(_) ->
    io:format("Starting SP DHT Test~n"),
    
    % Compile the modules
    compile_modules(),
    
    % Start the first node
    io:format("Starting first node (node1:8080)~n"),
    sp_server:start_link("node1", 8080),
    timer:sleep(1000),
    
    % Register some topics on the first node
    io:format("Registering topics on first node~n"),
    sp_server:register_topic("sports", [{"127.0.0.1", 9001}, {"127.0.0.1", 9002}]),
    sp_server:register_topic("music", [{"127.0.0.1", 9003}]),
    timer:sleep(1000),
    
    % Start the second node
    io:format("Starting second node (node2:8081)~n"),
    sp_server:start_link("node2", {"127.0.0.1", 8080}, 8081),
    timer:sleep(1000),
    
    % Register a topic on the second node
    io:format("Registering topic on second node~n"),
    sp_server:register_topic("movies", [{"127.0.0.1", 9004}, {"127.0.0.1", 9005}]),
    timer:sleep(1000),
    
    % Start the third node
    io:format("Starting third node (node3:8082)~n"),
    sp_server:start_link("node3", {"127.0.0.1", 8081}, 8082),
    timer:sleep(1000),
    
    % Get topics from each node
    io:format("Getting topics from nodes~n"),
    TopicsNode1 = sp_server:get_topics(),
    io:format("Topics on node1: ~p~n", [TopicsNode1]),
    
    % Let the test run for a while so we can manually test
    io:format("Test setup complete. System is running.~n"),
    io:format("You can now test with TestSPClientMain or wait for 60 seconds to exit.~n"),
    timer:sleep(600000),
    
    io:format("Test complete.~n").

compile_modules() ->
    compile:file(sp_dht),
    compile:file(sp_server),
    compile:file(sp_node_comm),
    compile:file(sp_tcp_listener),
    compile:file(sp_app).