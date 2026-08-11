package com.phishware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PhishwareApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhishwareApplication.class, args);
    }
}
