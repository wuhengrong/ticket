package com.grace.ticket;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling  // 确保这个注解存在
public class TicketManageApplication {
	@PostConstruct
	void started() {
		// 设置应用默认时区为北京时间
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
	}
	public static void main(String[] args) {
		SpringApplication.run(TicketManageApplication.class, args);
	}

}
