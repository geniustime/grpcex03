package com.test.jdh.ex01;

import com.example.demo.proto.EventRequest;
import com.example.demo.proto.EventResponse;
import com.example.demo.proto.NewdataServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.ILoggerFactory;

@Slf4j
// server
public class NewdataServiceImpl extends NewdataServiceGrpc.NewdataServiceImplBase {

    //  client sends a single request and gets back a single response.
    // 1:1 이기때문에 onNext는 1회만 호출할 수 있고, 그 후 잘 보냈다고 onCompleted 메서드를 호출하는 식으로 처리하면 된다
    @Override
    public void unaryEvent(EventRequest request, StreamObserver<EventResponse> responseObserver) {

        // 보낼 데이터 설정하므로 response에다가 request 받은 데이터에서 get하여  result에 넣음
        EventResponse eventResponse = EventResponse.newBuilder()
                .setResult(request.getSourceId() + request.getEventId())
                .build();

        // unary는 onNext() 1회만 호출 가능
        // 클라이언트에 다음 스트림을 요청함
        responseObserver.onNext(eventResponse);

        // 클라이언트에 스트림 완료임을 보냄
        responseObserver.onCompleted();
    }


    // 클라이언트의 1회 요청에 대해 stream으로 응답이 가능하기 때문에 달라진것은 onNext를 여러번 호출해서 여러번 데이터를 보내는 것 뿐이다.

    // 서버가 클라이언트의 요청 메시지를 받은 후 응답 스트림을 다시 전송한다
    // 모든 응답을 보낸 후 서버의 상태 세부 정보 (상태 코드 및 선택적 상태 메시지)와 선택적 후행 메타 데이터가 서버 측에서 완료되도록 다시 전송됩니다.
    // 클라이언트는 모든 서버의 응답을 받으면 완료됩니다.
    @Override
    public void serverStreamingEvent(EventRequest request, StreamObserver<EventResponse> responseObserver) {

        EventResponse eventResponse = EventResponse.newBuilder()
                .setResult( request.getSourceId() + request.getEventId() )
                .build();

        // 클라이언트로부터 데이터는 1회만 들어오지만 serverStreaming은 여러번 onNext()를 호출 가능
        responseObserver.onNext(eventResponse);
        responseObserver.onNext(eventResponse);
        responseObserver.onNext(eventResponse);
        responseObserver.onNext(eventResponse);

        // 클라이언트에 완료임을 보냄
        responseObserver.onCompleted();
    }


    // 클라이언트에서 N개의 데이터를 보내줄 것인데 그 때 클라이언트도 onNext를 통해서 전달할 것이다.
    //onNext로 N개의 데이터 전달이 끝난다면, onCompleted메서드를 호출해서 다 보냈다고 알려줄 것이다.
    //그러면 서버도 onCompleted를 받으면 응답 1개를 해줘야한다.

    // 클라이언트가 단일 요청 대신 요청 스트림을 서버로 전송한다
    // 서버는 단일 응답을 보낸다.
    // 일반적으로 모든 클라이언트의 요청을받은 후 반드시 상태 정보 및 선택적 후행 메타 데이터와 함께 모든 클라이언트의 요청을받은 것은 아닙니다.
    @Override
    public StreamObserver<EventRequest> clientStreamingEvent(StreamObserver<EventResponse> responseObserver) {

        StreamObserver<EventRequest> streamObserver = new StreamObserver<EventRequest>() {
            @Override
            public void onNext(EventRequest eventRequest) {
                // 클라이언트가 여러번의 onNext()를 호출할 예정???
                log.info("/////[clientStreamingEvent-onNext] "+eventRequest.getSourceId()+", "+eventRequest.getEventId());
            }

            @Override
            public void onError(Throwable throwable) {
                log.info("///// [clientStreamingEvent-onError] onError 호출");
                log.error(throwable.getMessage(), throwable);

            }

            @Override
            public void onCompleted() {
                // 클라이언트가 스트림 완료 응답을 보내고, 서버가 요청을 받으면 1회만 onNext()를 호출할 수 있다.
                log.info("///// [clientStreamingEvent-onCompleted] onCompleted");
                responseObserver.onNext(EventResponse.newBuilder().setResult("///// [clientStreamingEvent-onCompleted] RESPONSE!!!").build());
                responseObserver.onCompleted();
            }
        };

        return streamObserver;
    }

    //양방향 스트리밍이다. 따라서 서버도 onNext를 여러번 호출 가능하고, 클라이언트도 onNext를 통해서 여러번 데이터가 들어올 것이다.
    //서버도 onNext를 통해서 여러번 데이터 송신을 마쳤다면, onCompleted로 데이터 송신이 끝났다고 클라이언트에 알려줘야 한다.

    // 클라이언트가 메소드를 호출하고 서버가 클라이언트 메타 데이터, 메소드 이름 및 최종 기한을 수신함으로써 호출이 다시 시작됩니다.
    // 다시 서버는 초기 메타 데이터를 다시 보내거나 클라이언트가 요청을 보내기 시작할 때까지 기다릴 수 있습니다.
    // 클라이언트와 서버는 어떤 순서로도 읽고 쓸 수 있으므로 스트림은 완전히 독립적으로 작동합니다.
    // 예를 들어, 서버는 응답을 작성하기 전에 모든 클라이언트의 메시지를 수신 할 때까지 기다리거나 서버와 클라이언트가 "핑퐁"할 수 있습니다.
    // 서버는 요청을 받고 응답을 다시 보냅니다.
    // 그런 다음 클라이언트는 응답 등에 따라 다른 요청을 보냅니다.
    @Override
    public StreamObserver<EventRequest> biStreamingEvent(StreamObserver<EventResponse> responseObserver) {

        StreamObserver<EventRequest> streamObserver = new StreamObserver<EventRequest>() {
            @Override
            public void onNext(EventRequest eventRequest) {
                log.info("///// [biStreamingEvent-onNext] Bidirection "+ eventRequest.getSourceId()+", "+eventRequest.getEventId());
                responseObserver.onNext(EventResponse.newBuilder().setResult("///// [biStreamingEvent-onNext] RESPONSE!!!").build());
                responseObserver.onNext(EventResponse.newBuilder().setResult("///// [biStreamingEvent-onNext] RESPONSE!!!").build());
                responseObserver.onNext(EventResponse.newBuilder().setResult("///// [biStreamingEvent-onNext] RESPONSE!!!").build());
            }

            @Override
            public void onError(Throwable throwable) {
                log.error(throwable.getMessage(), throwable);
            }

            @Override
            public void onCompleted() {
                log.info("///// [biStreamingEvent-onCompleted] Completed!");
                responseObserver.onCompleted();
            }

        };
        return streamObserver;
    }
}
