%%%-------------------------------------------------------------------
%%% @doc Script para iniciar servidores SP em múltiplos nós
%%%-------------------------------------------------------------------

-module(start_servers).
-export([start/0, start_with_config/1]).

%% Configuração padrão dos servidores
-define(DEFAULT_CONFIG, [
    {"node1", 8000, none},
    {"node2", 7000, {"127.0.0.1", 8000}},
    {"node3", 6000, {"127.0.0.1", 8000}},
    {"node4", 5000, {"127.0.0.1", 8000}}
]).

%% Inicia todos os servidores com configuração padrão
start() ->
    start_with_config(?DEFAULT_CONFIG).

%% Inicia servidores com configuração personalizada
%% Config é uma lista de tuplas {NodeName, Port, BootstrapNode}
%% onde BootstrapNode pode ser none ou {Ip, Port}
start_with_config(Config) ->
    lists:foreach(fun(ServerConfig) ->
        spawn(fun() -> start_server(ServerConfig) end)
    end, Config),
    io:format("Todos os servidores iniciados.~n").

%% Inicia um único servidor com a configuração fornecida
start_server({NodeName, Port, none}) ->
    io:format("Iniciando ~p na porta ~p (nó inicial)~n", [NodeName, Port]),
    sp_app:start(NodeName, Port);

start_server({NodeName, Port, {BootstrapIp, BootstrapPort}}) ->
    io:format("Iniciando ~p na porta ~p (conectando a ~p:~p)~n", 
              [NodeName, Port, BootstrapIp, BootstrapPort]),
    sp_app:start(NodeName, {BootstrapIp, BootstrapPort}, Port).

%% Script de teste
%% Para usar, execute no shell Erlang:
%% 1. c(start_servers).
%% 2. start_servers:start().
%%
%% Para usar com configuração personalizada:
%% start_servers:start_with_config([
%%    {"node1", 8000, none},
%%    {"node2", 7000, {"127.0.0.1", 8000}},
%%    {"custom_node", 4000, {"127.0.0.1", 8000}}
%% ]).