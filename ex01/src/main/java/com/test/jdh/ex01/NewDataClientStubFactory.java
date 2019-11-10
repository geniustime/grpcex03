package com.test.jdh.ex01;

import com.example.demo.proto.NewdataServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

// 클라이언트와 서버는 channel을 통해 통신하고, stub는 channel을 통해 서비스를 호출할 수 있는 추상화된 객체로 보면 된다.
// stub의 방식이 또 3가지가 있다. async, blocking, future다.
public class NewDataClientStubFactory {

    private final ManagedChannel managedChannel;
    private final NewdataServiceGrpc.NewdataServiceBlockingStub blockingStub; // blocking
    private final NewdataServiceGrpc.NewdataServiceStub asyncStub; // async
    private final NewdataServiceGrpc.NewdataServiceFutureStub futureStub; // future

    // channel을 만들 때 .usePlainText()를 호출했는데 이게 없으면 tls exception이 난다.
    public NewDataClientStubFactory(String serverIp, int port) {
        this.managedChannel = ManagedChannelBuilder
                    .forAddress(serverIp, port)
                    .usePlaintext()
                    .build();
        this.blockingStub = NewdataServiceGrpc.newBlockingStub(managedChannel);
        this.asyncStub = NewdataServiceGrpc.newStub(managedChannel);
        this.futureStub = NewdataServiceGrpc.newFutureStub(managedChannel);
    }


    public NewdataServiceGrpc.NewdataServiceBlockingStub getBlockingStub() {
        return blockingStub;
    }

    public NewdataServiceGrpc.NewdataServiceStub getAsyncStub() {
        return asyncStub;
    }

    public NewdataServiceGrpc.NewdataServiceFutureStub getFutureStub() {
        return futureStub;
    }
}
