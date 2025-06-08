package com.aslaw_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.aslaw", "com.infracore"})
@EntityScan(basePackages = {"com.aslaw.entity", "com.infracore.entity"})
@EnableJpaRepositories(basePackages = {"com.aslaw.repository", "com.infracore.repository"})
public class AslawBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AslawBackendApplication.class, args);
	}

}
