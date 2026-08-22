package com.campuseatery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class CampusEaterySpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampusEaterySpringApplication.class, args);
	}

}