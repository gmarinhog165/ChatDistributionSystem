package pt.uminho.di.proto;

import static pt.uminho.di.proto.ChatServiceGrpc.getServiceDescriptor;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;


@javax.annotation.Generated(
value = "by RxGrpc generator",
comments = "Source: ChatService.proto")
public final class Rx3ChatServiceGrpc {
    private Rx3ChatServiceGrpc() {}

    public static RxChatServiceStub newRxStub(io.grpc.Channel channel) {
        return new RxChatServiceStub(channel);
    }

    /**
     * <pre>
     *  The chat service definition
     * </pre>
     */
    public static final class RxChatServiceStub extends io.grpc.stub.AbstractStub<RxChatServiceStub> {
        private ChatServiceGrpc.ChatServiceStub delegateStub;

        private RxChatServiceStub(io.grpc.Channel channel) {
            super(channel);
            delegateStub = ChatServiceGrpc.newStub(channel);
        }

        private RxChatServiceStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
            delegateStub = ChatServiceGrpc.newStub(channel).build(channel, callOptions);
        }

        @java.lang.Override
        protected RxChatServiceStub build(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new RxChatServiceStub(channel, callOptions);
        }

        /**
         * <pre>
         *  Join a topic
         * </pre>
         */
        public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.JoinLeaveResponse> joinTopic(io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.JoinLeaveRequest> rxRequest) {
            return com.salesforce.rx3grpc.stub.ClientCalls.oneToOne(rxRequest,
                new com.salesforce.reactivegrpc.common.BiConsumer<pt.uminho.di.proto.JoinLeaveRequest, io.grpc.stub.StreamObserver<pt.uminho.di.proto.JoinLeaveResponse>>() {
                    @java.lang.Override
                    public void accept(pt.uminho.di.proto.JoinLeaveRequest request, io.grpc.stub.StreamObserver<pt.uminho.di.proto.JoinLeaveResponse> observer) {
                        delegateStub.joinTopic(request, observer);
                    }
                }, getCallOptions());
        }

        /**
         * <pre>
         *  Leave a topic
         * </pre>
         */
        public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.JoinLeaveResponse> leaveTopic(io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.JoinLeaveRequest> rxRequest) {
            return com.salesforce.rx3grpc.stub.ClientCalls.oneToOne(rxRequest,
                new com.salesforce.reactivegrpc.common.BiConsumer<pt.uminho.di.proto.JoinLeaveRequest, io.grpc.stub.StreamObserver<pt.uminho.di.proto.JoinLeaveResponse>>() {
                    @java.lang.Override
                    public void accept(pt.uminho.di.proto.JoinLeaveRequest request, io.grpc.stub.StreamObserver<pt.uminho.di.proto.JoinLeaveResponse> observer) {
                        delegateStub.leaveTopic(request, observer);
                    }
                }, getCallOptions());
        }

        /**
         * <pre>
         *  Send a message to a topic
         * </pre>
         */
        public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.SendMessageResponse> sendMessage(io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.SendMessageRequest> rxRequest) {
            return com.salesforce.rx3grpc.stub.ClientCalls.oneToOne(rxRequest,
                new com.salesforce.reactivegrpc.common.BiConsumer<pt.uminho.di.proto.SendMessageRequest, io.grpc.stub.StreamObserver<pt.uminho.di.proto.SendMessageResponse>>() {
                    @java.lang.Override
                    public void accept(pt.uminho.di.proto.SendMessageRequest request, io.grpc.stub.StreamObserver<pt.uminho.di.proto.SendMessageResponse> observer) {
                        delegateStub.sendMessage(request, observer);
                    }
                }, getCallOptions());
        }

        /**
         * <pre>
         *  Get list of active users in a topic
         * </pre>
         */
        public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.GetUsersResponse> getActiveUsers(io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.GetUsersRequest> rxRequest) {
            return com.salesforce.rx3grpc.stub.ClientCalls.oneToOne(rxRequest,
                new com.salesforce.reactivegrpc.common.BiConsumer<pt.uminho.di.proto.GetUsersRequest, io.grpc.stub.StreamObserver<pt.uminho.di.proto.GetUsersResponse>>() {
                    @java.lang.Override
                    public void accept(pt.uminho.di.proto.GetUsersRequest request, io.grpc.stub.StreamObserver<pt.uminho.di.proto.GetUsersResponse> observer) {
                        delegateStub.getActiveUsers(request, observer);
                    }
                }, getCallOptions());
        }

        /**
         * <pre>
         *  Get chat log for a topic
         * </pre>
         */
        public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.GetLogResponse> getChatLog(io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.GetLogRequest> rxRequest) {
            return com.salesforce.rx3grpc.stub.ClientCalls.oneToOne(rxRequest,
                new com.salesforce.reactivegrpc.common.BiConsumer<pt.uminho.di.proto.GetLogRequest, io.grpc.stub.StreamObserver<pt.uminho.di.proto.GetLogResponse>>() {
                    @java.lang.Override
                    public void accept(pt.uminho.di.proto.GetLogRequest request, io.grpc.stub.StreamObserver<pt.uminho.di.proto.GetLogResponse> observer) {
                        delegateStub.getChatLog(request, observer);
                    }
                }, getCallOptions());
        }

        /**
         * <pre>
         *  Subscribe to receive messages from a topic in real time
         * </pre>
         */
        public io.reactivex.rxjava3.core.Flowable<pt.uminho.di.proto.ChatMessage> subscribeToTopic(io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.SubscribeRequest> rxRequest) {
            return com.salesforce.rx3grpc.stub.ClientCalls.oneToMany(rxRequest,
                new com.salesforce.reactivegrpc.common.BiConsumer<pt.uminho.di.proto.SubscribeRequest, io.grpc.stub.StreamObserver<pt.uminho.di.proto.ChatMessage>>() {
                    @java.lang.Override
                    public void accept(pt.uminho.di.proto.SubscribeRequest request, io.grpc.stub.StreamObserver<pt.uminho.di.proto.ChatMessage> observer) {
                        delegateStub.subscribeToTopic(request, observer);
                    }
                }, getCallOptions());
        }

        /**
         * <pre>
         *  Join a topic
         * </pre>
         */
        public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.JoinLeaveResponse> joinTopic(pt.uminho.di.proto.JoinLeaveRequest rxRequest) {
            return com.salesforce.rx3grpc.stub.ClientCalls.oneToOne(io.reactivex.rxjava3.core.Single.just(rxRequest),
                new com.salesforce.reactivegrpc.common.BiConsumer<pt.uminho.di.proto.JoinLeaveRequest, io.grpc.stub.StreamObserver<pt.uminho.di.proto.JoinLeaveResponse>>() {
                    @java.lang.Override
                    public void accept(pt.uminho.di.proto.JoinLeaveRequest request, io.grpc.stub.StreamObserver<pt.uminho.di.proto.JoinLeaveResponse> observer) {
                        delegateStub.joinTopic(request, observer);
                    }
                }, getCallOptions());
        }

        /**
         * <pre>
         *  Leave a topic
         * </pre>
         */
        public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.JoinLeaveResponse> leaveTopic(pt.uminho.di.proto.JoinLeaveRequest rxRequest) {
            return com.salesforce.rx3grpc.stub.ClientCalls.oneToOne(io.reactivex.rxjava3.core.Single.just(rxRequest),
                new com.salesforce.reactivegrpc.common.BiConsumer<pt.uminho.di.proto.JoinLeaveRequest, io.grpc.stub.StreamObserver<pt.uminho.di.proto.JoinLeaveResponse>>() {
                    @java.lang.Override
                    public void accept(pt.uminho.di.proto.JoinLeaveRequest request, io.grpc.stub.StreamObserver<pt.uminho.di.proto.JoinLeaveResponse> observer) {
                        delegateStub.leaveTopic(request, observer);
                    }
                }, getCallOptions());
        }

        /**
         * <pre>
         *  Send a message to a topic
         * </pre>
         */
        public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.SendMessageResponse> sendMessage(pt.uminho.di.proto.SendMessageRequest rxRequest) {
            return com.salesforce.rx3grpc.stub.ClientCalls.oneToOne(io.reactivex.rxjava3.core.Single.just(rxRequest),
                new com.salesforce.reactivegrpc.common.BiConsumer<pt.uminho.di.proto.SendMessageRequest, io.grpc.stub.StreamObserver<pt.uminho.di.proto.SendMessageResponse>>() {
                    @java.lang.Override
                    public void accept(pt.uminho.di.proto.SendMessageRequest request, io.grpc.stub.StreamObserver<pt.uminho.di.proto.SendMessageResponse> observer) {
                        delegateStub.sendMessage(request, observer);
                    }
                }, getCallOptions());
        }

        /**
         * <pre>
         *  Get list of active users in a topic
         * </pre>
         */
        public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.GetUsersResponse> getActiveUsers(pt.uminho.di.proto.GetUsersRequest rxRequest) {
            return com.salesforce.rx3grpc.stub.ClientCalls.oneToOne(io.reactivex.rxjava3.core.Single.just(rxRequest),
                new com.salesforce.reactivegrpc.common.BiConsumer<pt.uminho.di.proto.GetUsersRequest, io.grpc.stub.StreamObserver<pt.uminho.di.proto.GetUsersResponse>>() {
                    @java.lang.Override
                    public void accept(pt.uminho.di.proto.GetUsersRequest request, io.grpc.stub.StreamObserver<pt.uminho.di.proto.GetUsersResponse> observer) {
                        delegateStub.getActiveUsers(request, observer);
                    }
                }, getCallOptions());
        }

        /**
         * <pre>
         *  Get chat log for a topic
         * </pre>
         */
        public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.GetLogResponse> getChatLog(pt.uminho.di.proto.GetLogRequest rxRequest) {
            return com.salesforce.rx3grpc.stub.ClientCalls.oneToOne(io.reactivex.rxjava3.core.Single.just(rxRequest),
                new com.salesforce.reactivegrpc.common.BiConsumer<pt.uminho.di.proto.GetLogRequest, io.grpc.stub.StreamObserver<pt.uminho.di.proto.GetLogResponse>>() {
                    @java.lang.Override
                    public void accept(pt.uminho.di.proto.GetLogRequest request, io.grpc.stub.StreamObserver<pt.uminho.di.proto.GetLogResponse> observer) {
                        delegateStub.getChatLog(request, observer);
                    }
                }, getCallOptions());
        }

        /**
         * <pre>
         *  Subscribe to receive messages from a topic in real time
         * </pre>
         */
        public io.reactivex.rxjava3.core.Flowable<pt.uminho.di.proto.ChatMessage> subscribeToTopic(pt.uminho.di.proto.SubscribeRequest rxRequest) {
            return com.salesforce.rx3grpc.stub.ClientCalls.oneToMany(io.reactivex.rxjava3.core.Single.just(rxRequest),
                new com.salesforce.reactivegrpc.common.BiConsumer<pt.uminho.di.proto.SubscribeRequest, io.grpc.stub.StreamObserver<pt.uminho.di.proto.ChatMessage>>() {
                    @java.lang.Override
                    public void accept(pt.uminho.di.proto.SubscribeRequest request, io.grpc.stub.StreamObserver<pt.uminho.di.proto.ChatMessage> observer) {
                        delegateStub.subscribeToTopic(request, observer);
                    }
                }, getCallOptions());
        }

    }

    /**
     * <pre>
     *  The chat service definition
     * </pre>
     */
    public static abstract class ChatServiceImplBase implements io.grpc.BindableService {

        /**
         * <pre>
         *  Join a topic
         * </pre>
         */
        public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.JoinLeaveResponse> joinTopic(pt.uminho.di.proto.JoinLeaveRequest request) {
            return joinTopic(io.reactivex.rxjava3.core.Single.just(request));
        }

        /**
         * <pre>
         *  Join a topic
         * </pre>
         */
        public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.JoinLeaveResponse> joinTopic(io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.JoinLeaveRequest> request) {
            throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
        }

        /**
         * <pre>
         *  Leave a topic
         * </pre>
         */
        public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.JoinLeaveResponse> leaveTopic(pt.uminho.di.proto.JoinLeaveRequest request) {
            return leaveTopic(io.reactivex.rxjava3.core.Single.just(request));
        }

        /**
         * <pre>
         *  Leave a topic
         * </pre>
         */
        public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.JoinLeaveResponse> leaveTopic(io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.JoinLeaveRequest> request) {
            throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
        }

        /**
         * <pre>
         *  Send a message to a topic
         * </pre>
         */
        public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.SendMessageResponse> sendMessage(pt.uminho.di.proto.SendMessageRequest request) {
            return sendMessage(io.reactivex.rxjava3.core.Single.just(request));
        }

        /**
         * <pre>
         *  Send a message to a topic
         * </pre>
         */
        public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.SendMessageResponse> sendMessage(io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.SendMessageRequest> request) {
            throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
        }

        /**
         * <pre>
         *  Get list of active users in a topic
         * </pre>
         */
        public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.GetUsersResponse> getActiveUsers(pt.uminho.di.proto.GetUsersRequest request) {
            return getActiveUsers(io.reactivex.rxjava3.core.Single.just(request));
        }

        /**
         * <pre>
         *  Get list of active users in a topic
         * </pre>
         */
        public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.GetUsersResponse> getActiveUsers(io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.GetUsersRequest> request) {
            throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
        }

        /**
         * <pre>
         *  Get chat log for a topic
         * </pre>
         */
        public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.GetLogResponse> getChatLog(pt.uminho.di.proto.GetLogRequest request) {
            return getChatLog(io.reactivex.rxjava3.core.Single.just(request));
        }

        /**
         * <pre>
         *  Get chat log for a topic
         * </pre>
         */
        public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.GetLogResponse> getChatLog(io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.GetLogRequest> request) {
            throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
        }

        /**
         * <pre>
         *  Subscribe to receive messages from a topic in real time
         * </pre>
         */
        public io.reactivex.rxjava3.core.Flowable<pt.uminho.di.proto.ChatMessage> subscribeToTopic(pt.uminho.di.proto.SubscribeRequest request) {
            return subscribeToTopic(io.reactivex.rxjava3.core.Single.just(request));
        }

        /**
         * <pre>
         *  Subscribe to receive messages from a topic in real time
         * </pre>
         */
        public io.reactivex.rxjava3.core.Flowable<pt.uminho.di.proto.ChatMessage> subscribeToTopic(io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.SubscribeRequest> request) {
            throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
        }

        @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(
                            pt.uminho.di.proto.ChatServiceGrpc.getJoinTopicMethod(),
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            pt.uminho.di.proto.JoinLeaveRequest,
                                            pt.uminho.di.proto.JoinLeaveResponse>(
                                            this, METHODID_JOIN_TOPIC)))
                    .addMethod(
                            pt.uminho.di.proto.ChatServiceGrpc.getLeaveTopicMethod(),
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            pt.uminho.di.proto.JoinLeaveRequest,
                                            pt.uminho.di.proto.JoinLeaveResponse>(
                                            this, METHODID_LEAVE_TOPIC)))
                    .addMethod(
                            pt.uminho.di.proto.ChatServiceGrpc.getSendMessageMethod(),
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            pt.uminho.di.proto.SendMessageRequest,
                                            pt.uminho.di.proto.SendMessageResponse>(
                                            this, METHODID_SEND_MESSAGE)))
                    .addMethod(
                            pt.uminho.di.proto.ChatServiceGrpc.getGetActiveUsersMethod(),
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            pt.uminho.di.proto.GetUsersRequest,
                                            pt.uminho.di.proto.GetUsersResponse>(
                                            this, METHODID_GET_ACTIVE_USERS)))
                    .addMethod(
                            pt.uminho.di.proto.ChatServiceGrpc.getGetChatLogMethod(),
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            pt.uminho.di.proto.GetLogRequest,
                                            pt.uminho.di.proto.GetLogResponse>(
                                            this, METHODID_GET_CHAT_LOG)))
                    .addMethod(
                            pt.uminho.di.proto.ChatServiceGrpc.getSubscribeToTopicMethod(),
                            asyncServerStreamingCall(
                                    new MethodHandlers<
                                            pt.uminho.di.proto.SubscribeRequest,
                                            pt.uminho.di.proto.ChatMessage>(
                                            this, METHODID_SUBSCRIBE_TO_TOPIC)))
                    .build();
        }

        protected io.grpc.CallOptions getCallOptions(int methodId) {
            return null;
        }

        protected Throwable onErrorMap(Throwable throwable) {
            return com.salesforce.rx3grpc.stub.ServerCalls.prepareError(throwable);
        }

    }

    public static final int METHODID_JOIN_TOPIC = 0;
    public static final int METHODID_LEAVE_TOPIC = 1;
    public static final int METHODID_SEND_MESSAGE = 2;
    public static final int METHODID_GET_ACTIVE_USERS = 3;
    public static final int METHODID_GET_CHAT_LOG = 4;
    public static final int METHODID_SUBSCRIBE_TO_TOPIC = 5;

    private static final class MethodHandlers<Req, Resp> implements
            io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final ChatServiceImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(ChatServiceImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_JOIN_TOPIC:
                    com.salesforce.rx3grpc.stub.ServerCalls.oneToOne((pt.uminho.di.proto.JoinLeaveRequest) request,
                            (io.grpc.stub.StreamObserver<pt.uminho.di.proto.JoinLeaveResponse>) responseObserver,
                            new com.salesforce.reactivegrpc.common.Function<pt.uminho.di.proto.JoinLeaveRequest, io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.JoinLeaveResponse>>() {
                                @java.lang.Override
                                public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.JoinLeaveResponse> apply(pt.uminho.di.proto.JoinLeaveRequest single) {
                                    return serviceImpl.joinTopic(single);
                                }
                            }, serviceImpl::onErrorMap);
                    break;
                case METHODID_LEAVE_TOPIC:
                    com.salesforce.rx3grpc.stub.ServerCalls.oneToOne((pt.uminho.di.proto.JoinLeaveRequest) request,
                            (io.grpc.stub.StreamObserver<pt.uminho.di.proto.JoinLeaveResponse>) responseObserver,
                            new com.salesforce.reactivegrpc.common.Function<pt.uminho.di.proto.JoinLeaveRequest, io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.JoinLeaveResponse>>() {
                                @java.lang.Override
                                public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.JoinLeaveResponse> apply(pt.uminho.di.proto.JoinLeaveRequest single) {
                                    return serviceImpl.leaveTopic(single);
                                }
                            }, serviceImpl::onErrorMap);
                    break;
                case METHODID_SEND_MESSAGE:
                    com.salesforce.rx3grpc.stub.ServerCalls.oneToOne((pt.uminho.di.proto.SendMessageRequest) request,
                            (io.grpc.stub.StreamObserver<pt.uminho.di.proto.SendMessageResponse>) responseObserver,
                            new com.salesforce.reactivegrpc.common.Function<pt.uminho.di.proto.SendMessageRequest, io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.SendMessageResponse>>() {
                                @java.lang.Override
                                public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.SendMessageResponse> apply(pt.uminho.di.proto.SendMessageRequest single) {
                                    return serviceImpl.sendMessage(single);
                                }
                            }, serviceImpl::onErrorMap);
                    break;
                case METHODID_GET_ACTIVE_USERS:
                    com.salesforce.rx3grpc.stub.ServerCalls.oneToOne((pt.uminho.di.proto.GetUsersRequest) request,
                            (io.grpc.stub.StreamObserver<pt.uminho.di.proto.GetUsersResponse>) responseObserver,
                            new com.salesforce.reactivegrpc.common.Function<pt.uminho.di.proto.GetUsersRequest, io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.GetUsersResponse>>() {
                                @java.lang.Override
                                public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.GetUsersResponse> apply(pt.uminho.di.proto.GetUsersRequest single) {
                                    return serviceImpl.getActiveUsers(single);
                                }
                            }, serviceImpl::onErrorMap);
                    break;
                case METHODID_GET_CHAT_LOG:
                    com.salesforce.rx3grpc.stub.ServerCalls.oneToOne((pt.uminho.di.proto.GetLogRequest) request,
                            (io.grpc.stub.StreamObserver<pt.uminho.di.proto.GetLogResponse>) responseObserver,
                            new com.salesforce.reactivegrpc.common.Function<pt.uminho.di.proto.GetLogRequest, io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.GetLogResponse>>() {
                                @java.lang.Override
                                public io.reactivex.rxjava3.core.Single<pt.uminho.di.proto.GetLogResponse> apply(pt.uminho.di.proto.GetLogRequest single) {
                                    return serviceImpl.getChatLog(single);
                                }
                            }, serviceImpl::onErrorMap);
                    break;
                case METHODID_SUBSCRIBE_TO_TOPIC:
                    com.salesforce.rx3grpc.stub.ServerCalls.oneToMany((pt.uminho.di.proto.SubscribeRequest) request,
                            (io.grpc.stub.StreamObserver<pt.uminho.di.proto.ChatMessage>) responseObserver,
                            new com.salesforce.reactivegrpc.common.Function<pt.uminho.di.proto.SubscribeRequest, io.reactivex.rxjava3.core.Flowable<pt.uminho.di.proto.ChatMessage>>() {
                                @java.lang.Override
                                public io.reactivex.rxjava3.core.Flowable<pt.uminho.di.proto.ChatMessage> apply(pt.uminho.di.proto.SubscribeRequest single) {
                                    return serviceImpl.subscribeToTopic(single);
                                }
                            }, serviceImpl::onErrorMap);
                    break;
                default:
                    throw new java.lang.AssertionError();
            }
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public io.grpc.stub.StreamObserver<Req> invoke(io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                default:
                    throw new java.lang.AssertionError();
            }
        }
    }

}
