package com.test.jdh.ex01;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Ex01Application implements CommandLineRunner {

    @Autowired
    private GrpcServer grpcServer;

    public static void main(String[] args) {
        SpringApplication.run(Ex01Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        grpcServer.start();
    }
}
