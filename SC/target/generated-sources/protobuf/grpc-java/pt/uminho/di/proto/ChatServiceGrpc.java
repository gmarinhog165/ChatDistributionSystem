package pt.uminho.di.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * The chat service definition
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.61.1)",
    comments = "Source: ChatService.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class ChatServiceGrpc {

  private ChatServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "pt.uminho.di.proto.ChatService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<pt.uminho.di.proto.JoinLeaveRequest,
      pt.uminho.di.proto.JoinLeaveResponse> getJoinTopicMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "JoinTopic",
      requestType = pt.uminho.di.proto.JoinLeaveRequest.class,
      responseType = pt.uminho.di.proto.JoinLeaveResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.uminho.di.proto.JoinLeaveRequest,
      pt.uminho.di.proto.JoinLeaveResponse> getJoinTopicMethod() {
    io.grpc.MethodDescriptor<pt.uminho.di.proto.JoinLeaveRequest, pt.uminho.di.proto.JoinLeaveResponse> getJoinTopicMethod;
    if ((getJoinTopicMethod = ChatServiceGrpc.getJoinTopicMethod) == null) {
      synchronized (ChatServiceGrpc.class) {
        if ((getJoinTopicMethod = ChatServiceGrpc.getJoinTopicMethod) == null) {
          ChatServiceGrpc.getJoinTopicMethod = getJoinTopicMethod =
              io.grpc.MethodDescriptor.<pt.uminho.di.proto.JoinLeaveRequest, pt.uminho.di.proto.JoinLeaveResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "JoinTopic"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.uminho.di.proto.JoinLeaveRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.uminho.di.proto.JoinLeaveResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ChatServiceMethodDescriptorSupplier("JoinTopic"))
              .build();
        }
      }
    }
    return getJoinTopicMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pt.uminho.di.proto.JoinLeaveRequest,
      pt.uminho.di.proto.JoinLeaveResponse> getLeaveTopicMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "LeaveTopic",
      requestType = pt.uminho.di.proto.JoinLeaveRequest.class,
      responseType = pt.uminho.di.proto.JoinLeaveResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.uminho.di.proto.JoinLeaveRequest,
      pt.uminho.di.proto.JoinLeaveResponse> getLeaveTopicMethod() {
    io.grpc.MethodDescriptor<pt.uminho.di.proto.JoinLeaveRequest, pt.uminho.di.proto.JoinLeaveResponse> getLeaveTopicMethod;
    if ((getLeaveTopicMethod = ChatServiceGrpc.getLeaveTopicMethod) == null) {
      synchronized (ChatServiceGrpc.class) {
        if ((getLeaveTopicMethod = ChatServiceGrpc.getLeaveTopicMethod) == null) {
          ChatServiceGrpc.getLeaveTopicMethod = getLeaveTopicMethod =
              io.grpc.MethodDescriptor.<pt.uminho.di.proto.JoinLeaveRequest, pt.uminho.di.proto.JoinLeaveResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "LeaveTopic"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.uminho.di.proto.JoinLeaveRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.uminho.di.proto.JoinLeaveResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ChatServiceMethodDescriptorSupplier("LeaveTopic"))
              .build();
        }
      }
    }
    return getLeaveTopicMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pt.uminho.di.proto.SendMessageRequest,
      pt.uminho.di.proto.SendMessageResponse> getSendMessageMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SendMessage",
      requestType = pt.uminho.di.proto.SendMessageRequest.class,
      responseType = pt.uminho.di.proto.SendMessageResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.uminho.di.proto.SendMessageRequest,
      pt.uminho.di.proto.SendMessageResponse> getSendMessageMethod() {
    io.grpc.MethodDescriptor<pt.uminho.di.proto.SendMessageRequest, pt.uminho.di.proto.SendMessageResponse> getSendMessageMethod;
    if ((getSendMessageMethod = ChatServiceGrpc.getSendMessageMethod) == null) {
      synchronized (ChatServiceGrpc.class) {
        if ((getSendMessageMethod = ChatServiceGrpc.getSendMessageMethod) == null) {
          ChatServiceGrpc.getSendMessageMethod = getSendMessageMethod =
              io.grpc.MethodDescriptor.<pt.uminho.di.proto.SendMessageRequest, pt.uminho.di.proto.SendMessageResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SendMessage"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.uminho.di.proto.SendMessageRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.uminho.di.proto.SendMessageResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ChatServiceMethodDescriptorSupplier("SendMessage"))
              .build();
        }
      }
    }
    return getSendMessageMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pt.uminho.di.proto.GetUsersRequest,
      pt.uminho.di.proto.GetUsersResponse> getGetActiveUsersMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetActiveUsers",
      requestType = pt.uminho.di.proto.GetUsersRequest.class,
      responseType = pt.uminho.di.proto.GetUsersResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.uminho.di.proto.GetUsersRequest,
      pt.uminho.di.proto.GetUsersResponse> getGetActiveUsersMethod() {
    io.grpc.MethodDescriptor<pt.uminho.di.proto.GetUsersRequest, pt.uminho.di.proto.GetUsersResponse> getGetActiveUsersMethod;
    if ((getGetActiveUsersMethod = ChatServiceGrpc.getGetActiveUsersMethod) == null) {
      synchronized (ChatServiceGrpc.class) {
        if ((getGetActiveUsersMethod = ChatServiceGrpc.getGetActiveUsersMethod) == null) {
          ChatServiceGrpc.getGetActiveUsersMethod = getGetActiveUsersMethod =
              io.grpc.MethodDescriptor.<pt.uminho.di.proto.GetUsersRequest, pt.uminho.di.proto.GetUsersResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetActiveUsers"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.uminho.di.proto.GetUsersRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.uminho.di.proto.GetUsersResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ChatServiceMethodDescriptorSupplier("GetActiveUsers"))
              .build();
        }
      }
    }
    return getGetActiveUsersMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pt.uminho.di.proto.GetLogRequest,
      pt.uminho.di.proto.GetLogResponse> getGetChatLogMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetChatLog",
      requestType = pt.uminho.di.proto.GetLogRequest.class,
      responseType = pt.uminho.di.proto.GetLogResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.uminho.di.proto.GetLogRequest,
      pt.uminho.di.proto.GetLogResponse> getGetChatLogMethod() {
    io.grpc.MethodDescriptor<pt.uminho.di.proto.GetLogRequest, pt.uminho.di.proto.GetLogResponse> getGetChatLogMethod;
    if ((getGetChatLogMethod = ChatServiceGrpc.getGetChatLogMethod) == null) {
      synchronized (ChatServiceGrpc.class) {
        if ((getGetChatLogMethod = ChatServiceGrpc.getGetChatLogMethod) == null) {
          ChatServiceGrpc.getGetChatLogMethod = getGetChatLogMethod =
              io.grpc.MethodDescriptor.<pt.uminho.di.proto.GetLogRequest, pt.uminho.di.proto.GetLogResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetChatLog"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.uminho.di.proto.GetLogRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.uminho.di.proto.GetLogResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ChatServiceMethodDescriptorSupplier("GetChatLog"))
              .build();
        }
      }
    }
    return getGetChatLogMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pt.uminho.di.proto.SubscribeRequest,
      pt.uminho.di.proto.ChatMessage> getSubscribeToTopicMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SubscribeToTopic",
      requestType = pt.uminho.di.proto.SubscribeRequest.class,
      responseType = pt.uminho.di.proto.ChatMessage.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<pt.uminho.di.proto.SubscribeRequest,
      pt.uminho.di.proto.ChatMessage> getSubscribeToTopicMethod() {
    io.grpc.MethodDescriptor<pt.uminho.di.proto.SubscribeRequest, pt.uminho.di.proto.ChatMessage> getSubscribeToTopicMethod;
    if ((getSubscribeToTopicMethod = ChatServiceGrpc.getSubscribeToTopicMethod) == null) {
      synchronized (ChatServiceGrpc.class) {
        if ((getSubscribeToTopicMethod = ChatServiceGrpc.getSubscribeToTopicMethod) == null) {
          ChatServiceGrpc.getSubscribeToTopicMethod = getSubscribeToTopicMethod =
              io.grpc.MethodDescriptor.<pt.uminho.di.proto.SubscribeRequest, pt.uminho.di.proto.ChatMessage>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SubscribeToTopic"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.uminho.di.proto.SubscribeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.uminho.di.proto.ChatMessage.getDefaultInstance()))
              .setSchemaDescriptor(new ChatServiceMethodDescriptorSupplier("SubscribeToTopic"))
              .build();
        }
      }
    }
    return getSubscribeToTopicMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ChatServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ChatServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ChatServiceStub>() {
        @java.lang.Override
        public ChatServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ChatServiceStub(channel, callOptions);
        }
      };
    return ChatServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ChatServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ChatServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ChatServiceBlockingStub>() {
        @java.lang.Override
        public ChatServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ChatServiceBlockingStub(channel, callOptions);
        }
      };
    return ChatServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ChatServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ChatServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ChatServiceFutureStub>() {
        @java.lang.Override
        public ChatServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ChatServiceFutureStub(channel, callOptions);
        }
      };
    return ChatServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * The chat service definition
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * Join a topic
     * </pre>
     */
    default void joinTopic(pt.uminho.di.proto.JoinLeaveRequest request,
        io.grpc.stub.StreamObserver<pt.uminho.di.proto.JoinLeaveResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getJoinTopicMethod(), responseObserver);
    }

    /**
     * <pre>
     * Leave a topic
     * </pre>
     */
    default void leaveTopic(pt.uminho.di.proto.JoinLeaveRequest request,
        io.grpc.stub.StreamObserver<pt.uminho.di.proto.JoinLeaveResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getLeaveTopicMethod(), responseObserver);
    }

    /**
     * <pre>
     * Send a message to a topic
     * </pre>
     */
    default void sendMessage(pt.uminho.di.proto.SendMessageRequest request,
        io.grpc.stub.StreamObserver<pt.uminho.di.proto.SendMessageResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSendMessageMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get list of active users in a topic
     * </pre>
     */
    default void getActiveUsers(pt.uminho.di.proto.GetUsersRequest request,
        io.grpc.stub.StreamObserver<pt.uminho.di.proto.GetUsersResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetActiveUsersMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get chat log for a topic
     * </pre>
     */
    default void getChatLog(pt.uminho.di.proto.GetLogRequest request,
        io.grpc.stub.StreamObserver<pt.uminho.di.proto.GetLogResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetChatLogMethod(), responseObserver);
    }

    /**
     * <pre>
     * Subscribe to receive messages from a topic in real time
     * </pre>
     */
    default void subscribeToTopic(pt.uminho.di.proto.SubscribeRequest request,
        io.grpc.stub.StreamObserver<pt.uminho.di.proto.ChatMessage> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSubscribeToTopicMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service ChatService.
   * <pre>
   * The chat service definition
   * </pre>
   */
  public static abstract class ChatServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return ChatServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service ChatService.
   * <pre>
   * The chat service definition
   * </pre>
   */
  public static final class ChatServiceStub
      extends io.grpc.stub.AbstractAsyncStub<ChatServiceStub> {
    private ChatServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChatServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ChatServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Join a topic
     * </pre>
     */
    public void joinTopic(pt.uminho.di.proto.JoinLeaveRequest request,
        io.grpc.stub.StreamObserver<pt.uminho.di.proto.JoinLeaveResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getJoinTopicMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Leave a topic
     * </pre>
     */
    public void leaveTopic(pt.uminho.di.proto.JoinLeaveRequest request,
        io.grpc.stub.StreamObserver<pt.uminho.di.proto.JoinLeaveResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getLeaveTopicMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Send a message to a topic
     * </pre>
     */
    public void sendMessage(pt.uminho.di.proto.SendMessageRequest request,
        io.grpc.stub.StreamObserver<pt.uminho.di.proto.SendMessageResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSendMessageMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get list of active users in a topic
     * </pre>
     */
    public void getActiveUsers(pt.uminho.di.proto.GetUsersRequest request,
        io.grpc.stub.StreamObserver<pt.uminho.di.proto.GetUsersResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetActiveUsersMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get chat log for a topic
     * </pre>
     */
    public void getChatLog(pt.uminho.di.proto.GetLogRequest request,
        io.grpc.stub.StreamObserver<pt.uminho.di.proto.GetLogResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetChatLogMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Subscribe to receive messages from a topic in real time
     * </pre>
     */
    public void subscribeToTopic(pt.uminho.di.proto.SubscribeRequest request,
        io.grpc.stub.StreamObserver<pt.uminho.di.proto.ChatMessage> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getSubscribeToTopicMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service ChatService.
   * <pre>
   * The chat service definition
   * </pre>
   */
  public static final class ChatServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<ChatServiceBlockingStub> {
    private ChatServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChatServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ChatServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Join a topic
     * </pre>
     */
    public pt.uminho.di.proto.JoinLeaveResponse joinTopic(pt.uminho.di.proto.JoinLeaveRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getJoinTopicMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Leave a topic
     * </pre>
     */
    public pt.uminho.di.proto.JoinLeaveResponse leaveTopic(pt.uminho.di.proto.JoinLeaveRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getLeaveTopicMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Send a message to a topic
     * </pre>
     */
    public pt.uminho.di.proto.SendMessageResponse sendMessage(pt.uminho.di.proto.SendMessageRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSendMessageMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get list of active users in a topic
     * </pre>
     */
    public pt.uminho.di.proto.GetUsersResponse getActiveUsers(pt.uminho.di.proto.GetUsersRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetActiveUsersMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get chat log for a topic
     * </pre>
     */
    public pt.uminho.di.proto.GetLogResponse getChatLog(pt.uminho.di.proto.GetLogRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetChatLogMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Subscribe to receive messages from a topic in real time
     * </pre>
     */
    public java.util.Iterator<pt.uminho.di.proto.ChatMessage> subscribeToTopic(
        pt.uminho.di.proto.SubscribeRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getSubscribeToTopicMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service ChatService.
   * <pre>
   * The chat service definition
   * </pre>
   */
  public static final class ChatServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<ChatServiceFutureStub> {
    private ChatServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChatServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ChatServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Join a topic
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.uminho.di.proto.JoinLeaveResponse> joinTopic(
        pt.uminho.di.proto.JoinLeaveRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getJoinTopicMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Leave a topic
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.uminho.di.proto.JoinLeaveResponse> leaveTopic(
        pt.uminho.di.proto.JoinLeaveRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getLeaveTopicMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Send a message to a topic
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.uminho.di.proto.SendMessageResponse> sendMessage(
        pt.uminho.di.proto.SendMessageRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSendMessageMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get list of active users in a topic
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.uminho.di.proto.GetUsersResponse> getActiveUsers(
        pt.uminho.di.proto.GetUsersRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetActiveUsersMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get chat log for a topic
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.uminho.di.proto.GetLogResponse> getChatLog(
        pt.uminho.di.proto.GetLogRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetChatLogMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_JOIN_TOPIC = 0;
  private static final int METHODID_LEAVE_TOPIC = 1;
  private static final int METHODID_SEND_MESSAGE = 2;
  private static final int METHODID_GET_ACTIVE_USERS = 3;
  private static final int METHODID_GET_CHAT_LOG = 4;
  private static final int METHODID_SUBSCRIBE_TO_TOPIC = 5;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_JOIN_TOPIC:
          serviceImpl.joinTopic((pt.uminho.di.proto.JoinLeaveRequest) request,
              (io.grpc.stub.StreamObserver<pt.uminho.di.proto.JoinLeaveResponse>) responseObserver);
          break;
        case METHODID_LEAVE_TOPIC:
          serviceImpl.leaveTopic((pt.uminho.di.proto.JoinLeaveRequest) request,
              (io.grpc.stub.StreamObserver<pt.uminho.di.proto.JoinLeaveResponse>) responseObserver);
          break;
        case METHODID_SEND_MESSAGE:
          serviceImpl.sendMessage((pt.uminho.di.proto.SendMessageRequest) request,
              (io.grpc.stub.StreamObserver<pt.uminho.di.proto.SendMessageResponse>) responseObserver);
          break;
        case METHODID_GET_ACTIVE_USERS:
          serviceImpl.getActiveUsers((pt.uminho.di.proto.GetUsersRequest) request,
              (io.grpc.stub.StreamObserver<pt.uminho.di.proto.GetUsersResponse>) responseObserver);
          break;
        case METHODID_GET_CHAT_LOG:
          serviceImpl.getChatLog((pt.uminho.di.proto.GetLogRequest) request,
              (io.grpc.stub.StreamObserver<pt.uminho.di.proto.GetLogResponse>) responseObserver);
          break;
        case METHODID_SUBSCRIBE_TO_TOPIC:
          serviceImpl.subscribeToTopic((pt.uminho.di.proto.SubscribeRequest) request,
              (io.grpc.stub.StreamObserver<pt.uminho.di.proto.ChatMessage>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getJoinTopicMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              pt.uminho.di.proto.JoinLeaveRequest,
              pt.uminho.di.proto.JoinLeaveResponse>(
                service, METHODID_JOIN_TOPIC)))
        .addMethod(
          getLeaveTopicMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              pt.uminho.di.proto.JoinLeaveRequest,
              pt.uminho.di.proto.JoinLeaveResponse>(
                service, METHODID_LEAVE_TOPIC)))
        .addMethod(
          getSendMessageMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              pt.uminho.di.proto.SendMessageRequest,
              pt.uminho.di.proto.SendMessageResponse>(
                service, METHODID_SEND_MESSAGE)))
        .addMethod(
          getGetActiveUsersMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              pt.uminho.di.proto.GetUsersRequest,
              pt.uminho.di.proto.GetUsersResponse>(
                service, METHODID_GET_ACTIVE_USERS)))
        .addMethod(
          getGetChatLogMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              pt.uminho.di.proto.GetLogRequest,
              pt.uminho.di.proto.GetLogResponse>(
                service, METHODID_GET_CHAT_LOG)))
        .addMethod(
          getSubscribeToTopicMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              pt.uminho.di.proto.SubscribeRequest,
              pt.uminho.di.proto.ChatMessage>(
                service, METHODID_SUBSCRIBE_TO_TOPIC)))
        .build();
  }

  private static abstract class ChatServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ChatServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return pt.uminho.di.proto.ChatServiceOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ChatService");
    }
  }

  private static final class ChatServiceFileDescriptorSupplier
      extends ChatServiceBaseDescriptorSupplier {
    ChatServiceFileDescriptorSupplier() {}
  }

  private static final class ChatServiceMethodDescriptorSupplier
      extends ChatServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    ChatServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ChatServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ChatServiceFileDescriptorSupplier())
              .addMethod(getJoinTopicMethod())
              .addMethod(getLeaveTopicMethod())
              .addMethod(getSendMessageMethod())
              .addMethod(getGetActiveUsersMethod())
              .addMethod(getGetChatLogMethod())
              .addMethod(getSubscribeToTopicMethod())
              .build();
        }
      }
    }
    return result;
  }
}
