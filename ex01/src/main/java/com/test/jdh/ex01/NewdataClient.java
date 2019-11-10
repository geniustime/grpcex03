package com.test.jdh.ex01;

import com.example.demo.proto.EventRequest;
import com.example.demo.proto.EventResponse;
import com.example.demo.proto.NewdataServiceGrpc;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

@Slf4j
public class NewdataClient {
    private final NewdataServiceGrpc.NewdataServiceBlockingStub blockingStub;
    private final NewdataServiceGrpc.NewdataServiceStub asyncStub;
    private final NewdataServiceGrpc.NewdataServiceFutureStub futureStub;

    public NewdataClient(NewdataServiceGrpc.NewdataServiceBlockingStub blockingStub, NewdataServiceGrpc.NewdataServiceStub asyncStub, NewdataServiceGrpc.NewdataServiceFutureStub futureStub) {
        this.blockingStub = blockingStub;
        this.asyncStub = asyncStub;
        this.futureStub = futureStub;
    }


    // Unary ---------------------------------

    public void sendBlockingUnaryMessage(EventRequest eventRequest){
        log.info("///// [NewdataClient-sendBlockingUnaryMessage / eventRequest] "+eventRequest);
        EventResponse eventResponse = blockingStub.unaryEvent(eventRequest);
        log.info("///// [NewdataClient-sendBlockingUnaryMessage / eventResponse] "+eventResponse);
    }

    public void sendAsyncUnaryMessage(EventRequest eventRequest){
        log.info("///// [NewdataClient-sendAsyncUnaryMessage / eventRequest] "+eventRequest);

        // 비동기로 응답받기 위해 서버로 보내는 콜백 객체(StreamObserver)도 같이 보낸다.
        asyncStub.unaryEvent(eventRequest, new StreamObserver<EventResponse>() {
            @Override
            public void onNext(EventResponse eventResponse) {
                log.info("///// [NewdataClient-sendAsyncUnaryMessage / eventRequest] "+eventResponse.getResult());
            }

            @Override
            public void onError(Throwable throwable) {
                log.error(throwable.getMessage(),throwable);
            }

            @Override
            public void onCompleted() {
                log.info("///// [NewdataClient-onCompleted] DONE!!");
            }
        });
    }

    public void sendFutureUnaryMessage(EventRequest eventRequest){
        log.info("///// [NewdataClient-sendFutureUnaryMessage / eventRequest] "+eventRequest);
        EventResponse eventResponse = null;
        ListenableFuture<EventResponse> listenableFuture = futureStub.unaryEvent(eventRequest);
        try {
            eventResponse = listenableFuture.get(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        log.info("///// [NewdataClient-sendFutureUnaryMessage / eventResponse ] "+eventResponse);
    }


    // ServerStreaming ----------------------------

    public void sendBlockingServerStreamingMessage(EventRequest eventRequest){
        log.info("///// [NewdataClient-sendBlockingServerStreamingMessage / eventRequest] "+eventRequest);
        Iterator<EventResponse> responseIterator;

        // streaming이 완료될 때까지 대기?
        responseIterator = blockingStub.serverStreamingEvent(eventRequest);
        responseIterator.forEachRemaining((eventResponse -> {
            log.info("///// [NewdataClient-sendBlockingServerStreamingMessage / eventResponse ] "+eventResponse);
        }));
    }

    public void sendAsyncServerStreamingMessage(EventRequest eventRequest){
        log.info("///// [NewdataClient-sendAsyncServerStreamingMessage / eventRequest] "+eventRequest);
            asyncStub.serverStreamingEvent(eventRequest, new StreamObserver<EventResponse>() {
                @Override
                public void onNext(EventResponse eventResponse) {
                    log.info("///// [NewdataClient-sendAsyncServerStreamingMessage-onNext / eventResponse ] "+eventResponse.getResult());
                }

                @Override
                public void onError(Throwable throwable) {
                    log.error(throwable.getMessage(), throwable);
                }

                @Override
                public void onCompleted() {
                    log.info("///// [NewdataClient-sendAsyncServerStreamingMessage-onCompleted] DONE!!");
                }
            });
        log.info("서버 응답과 상관없이 다른 작업중...");
    }

    // clientStreaming (async만 가능) ---------------------------

    public void sendAsyncClientStreamingMessage(List<EventRequest> eventRequests){
        log.info("///// [NewdataClient-sendAsyncClientStreamingMessage / eventRequests] "+eventRequests);

        StreamObserver<EventResponse> responseStreamObserver = new StreamObserver<EventResponse>() {
            @Override
            public void onNext(EventResponse eventResponse) {
                log.info("///// [NewdataClient-sendAsyncClientStreamingMessage-onNext / eventResponse ] "+eventResponse.getResult());
            }

            @Override
            public void onError(Throwable throwable) {
                log.error(throwable.getMessage(), throwable);
            }

            @Override
            public void onCompleted() {
                log.info("///// [NewdataClient-sendAsyncClientStreamingMessage-onCompleted] DONE!!");
            }
        };

        // 비동기로 요청을 보낼 객체 생성
        // 응답받을 객체와 연결
        StreamObserver<EventRequest> requestStreamObserver = asyncStub.clientStreamingEvent(responseStreamObserver);

        // 원하는 만큼 데이터 전송하기
        for (EventRequest eventRequest : eventRequests) {
                requestStreamObserver.onNext(eventRequest);
        }

        // 데이터 전송 완료 시그널 보내기
        requestStreamObserver.onCompleted();

//        log.info("///// [NewdataClient-sendAsyncClientStreamingMessage / eventResponse ] "+eventResponse);
    }


    // bidirection streaming ----------------------------
    public void sendBiDirectionStreamingMessage(List<EventRequest> eventRequests){
        log.info("///// [NewdataClient-sendBiDirectionStreamingMessage / eventRequests] "+eventRequests);

        StreamObserver<EventResponse> responseStreamObserver = new StreamObserver<EventResponse>() {
            @Override
            public void onNext(EventResponse eventResponse) {
                log.info("///// [NewdataClient-sendBiDirectionStreamingMessage-onNext / eventResponse ] "+eventResponse.getResult());
            }

            @Override
            public void onError(Throwable throwable) {
                log.error(throwable.getMessage(), throwable);
            }

            @Override
            public void onCompleted() {
                log.info("///// [NewdataClient-sendBiDirectionStreamingMessage-onCompleted] DONE!!");
            }
        };

        // 요청 보낼 객체 생성 및 응답객체와 연결
        StreamObserver<EventRequest> requestStreamObserver = asyncStub.biStreamingEvent(responseStreamObserver);
        for(EventRequest eventRequest : eventRequests){
            requestStreamObserver.onNext(eventRequest);
        }

        log.info("///// [NewdataClient-sendBiDirectionStreamingMessage] async니까 바로 로그 찍음");
        requestStreamObserver.onCompleted();

    }
}
