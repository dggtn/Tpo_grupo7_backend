package com.example.g7_back_mobile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Habilita las tareas programadas (@Scheduled)
public class G7BackMobileApplication {

	public static void main(String[] args) {
		SpringApplication.run(G7BackMobileApplication.class, args);
	}

}
