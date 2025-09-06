package com.multibank.notification_routing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableRetry
public class NotificationRoutingApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationRoutingApplication.class, args);
	}

}
