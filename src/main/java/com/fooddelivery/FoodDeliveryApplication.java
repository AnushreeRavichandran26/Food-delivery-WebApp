package com.fooddelivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = "com.fooddelivery")
public class FoodDeliveryApplication {
	public static void main(String[] args) {
		SpringApplication.run(FoodDeliveryApplication.class, args);
	}
}