%%% sp_server.erl
-module(sp_server).
-behaviour(gen_server).

%% API
-export([start_link/0, get_topics/0, get_scs/1, register_topic/2]).

%% gen_server callbacks
-export([init/1, handle_call/3, handle_cast/2, handle_info/2, terminate/2, code_change/3]).

-record(state, {
    topics = #{} :: map()
}).

%%% === API Functions ===

start_link() ->
    gen_server:start_link({local, ?MODULE}, ?MODULE, [], []).

get_topics() ->
    gen_server:call(?MODULE, get_topics).

get_scs(Topic) ->
    gen_server:call(?MODULE, {get_scs, Topic}).

register_topic(Topic, SCs) ->
    gen_server:cast(?MODULE, {register_topic, Topic, SCs}).

%%% === Callbacks ===

init([]) ->
    {ok, #state{}}.

handle_call(get_topics, _From, State) ->
    Topics = maps:keys(State#state.topics),
    {reply, Topics, State};

handle_call({get_scs, Topic}, _From, State) ->
    SCs = maps:get(Topic, State#state.topics, []),
    {reply, SCs, State};

handle_call(_, _From, State) ->
    {reply, error, State}.

handle_cast({register_topic, Topic, SCs}, State) ->
    Topics = State#state.topics,
    NewTopics = maps:put(Topic, SCs, Topics),
    {noreply, State#state{topics = NewTopics}};

handle_cast(_, State) ->
    {noreply, State}.

handle_info(_, State) ->
    {noreply, State}.

terminate(_, _) -> ok.
code_change(_, State, _) -> {ok, State}.
