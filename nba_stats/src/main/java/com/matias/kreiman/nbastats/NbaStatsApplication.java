package com.matias.kreiman.nbastats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class NbaStatsApplication {
    public static void main(String[] args) {
        SpringApplication.run(NbaStatsApplication.class, args);
    }
}