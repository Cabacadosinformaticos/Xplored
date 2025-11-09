package com.Xplored.Xplored;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.Xplored.Xplored")

public class XploredApplication {

	public static void main(String[] args) {
        SpringApplication.run(XploredApplication.class, args);
	}

}
