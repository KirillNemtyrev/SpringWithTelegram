package com.community.server;

import com.community.server.dto.ServerDto;
import com.community.server.utils.MD5Files;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@SpringBootApplication
@ComponentScan
@EnableJpaAuditing
@EnableAutoConfiguration
public class ServerApplication {

	@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		factory.setMaxFileSize(DataSize.parse("128KB"));
		factory.setMaxRequestSize(DataSize.parse("128KB"));
		return factory.createMultipartConfig();
	}

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

		MD5Files md5Files = new MD5Files();
		ServerDto[] serverDtos = md5Files.getServers();
		for (ServerDto serverDto : serverDtos){
			md5Files.generate("client/" + serverDto.getClient());

			md5Files.input(serverDto.getClient());
		}

		SpringApplication.run(ServerApplication.class, args);
	}

}
