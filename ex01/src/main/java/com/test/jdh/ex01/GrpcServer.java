package com.test.jdh.ex01;

import io.grpc.Server;

import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
//public class GrpcServer implements ApplicationRunner {
public class GrpcServer {

    @Value("${grpc.server.ip}")
    private Server server;

    @Value("${grpc.server.port")
    private int port;


    public void start() throws IOException, InterruptedException {
        server = ServerBuilder.forPort(port)
                .addService(new NewdataServiceImpl())
                .build();

        this.server.start();

        log.info("///// [gRPC SERVER START] "+server+":"+port);

        this.server.awaitTermination();
    }

/*
    @Override
    public void run(ApplicationArguments args) throws Exception {
        server = ServerBuilder.forPort(port)
                .addService(new NewdataServiceImpl())
                .build();

        this.server.start();

        log.info("///// [gRPC SERVER START] "+server+":"+port);

        this.server.awaitTermination();
    }
*/

}
