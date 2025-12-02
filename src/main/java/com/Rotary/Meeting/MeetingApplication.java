package com.Rotary.Meeting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
//@EnableCaching
public class MeetingApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeetingApplication.class, args);
	}

}
