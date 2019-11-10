package com.test.jdh.ex01;

import com.example.demo.proto.EventRequest;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClientService {

    @PostConstruct
    private void init(){
        String serverIp = "localhost";
        int port = 9999;

        NewDataClientStubFactory newDataClientStubFactory = new NewDataClientStubFactory(serverIp, port);
        NewdataClient newdataClient = new NewdataClient(newDataClientStubFactory.getBlockingStub(), newDataClientStubFactory.getAsyncStub(), newDataClientStubFactory.getFutureStub());

        // Requst 생성
        EventRequest eventRequest1 = EventRequest.newBuilder()
                .setSourceId("sourceId1")
                .setEventId("eventId1")
                .build();
        EventRequest eventRequest2 = EventRequest.newBuilder()
                .setSourceId("sourceId2")
                .setEventId("eventId2")
                .build();
        EventRequest eventRequest3 = EventRequest.newBuilder()
                .setSourceId("sourceId3")
                .setEventId("eventId3")
                .build();

        List<EventRequest> eventRequests = new ArrayList<>();
        eventRequests.add(eventRequest1);
        eventRequests.add(eventRequest2);
        eventRequests.add(eventRequest3);

        newdataClient.sendBiDirectionStreamingMessage(eventRequests);
    }
}
