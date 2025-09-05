package com.example.petapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PetappApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetappApplication.class, args);
    }

}
