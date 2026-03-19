package com.example.audiobackend;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;


@MapperScan("com.example.audiobackend.mapper")
@SpringBootApplication
public class AudiobackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AudiobackendApplication.class, args);
	}

}
