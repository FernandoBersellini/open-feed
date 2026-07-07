package com.senhorcafe.openfeed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OpenFeedApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenFeedApplication.class, args);
    }

}
