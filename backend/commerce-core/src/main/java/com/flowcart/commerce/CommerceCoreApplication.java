package com.flowcart.commerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CommerceCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommerceCoreApplication.class, args);
	}

}
