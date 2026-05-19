package com.PracticalAssignment.assignP;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AssignPApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(AssignPApplication.class);
		application.setAdditionalProfiles("local");
		application.run(args);
		System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::");
		System.out.println("GETTING STARTED WITH SOFTWARE AS A SERVICE (SaaS)");
		System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::");
	}

}
