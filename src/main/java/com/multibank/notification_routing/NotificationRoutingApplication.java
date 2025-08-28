package com.multibank.notification_routing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NotificationRoutingApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationRoutingApplication.class, args);
	}

}
