package com.example.employeeloginpage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class EmployeeloginpageApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmployeeloginpageApplication.class, args);
	}

}
