package com.grace.ticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // 确保这个注解存在
public class TicketManageApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketManageApplication.class, args);
	}

}
