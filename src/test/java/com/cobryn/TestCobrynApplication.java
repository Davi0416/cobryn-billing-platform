package com.cobryn;

import org.springframework.boot.SpringApplication;

public class TestCobrynApplication {

	public static void main(String[] args) {
		SpringApplication.from(CobrynApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
