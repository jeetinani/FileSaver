package com.assessment.FileSaver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FileSaverApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileSaverApplication.class, args);
	}

}
