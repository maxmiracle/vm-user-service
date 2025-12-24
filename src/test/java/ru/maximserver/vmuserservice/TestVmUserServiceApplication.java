package ru.maximserver.vmuserservice;

import org.springframework.boot.SpringApplication;

public class TestVmUserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(VmUserServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
