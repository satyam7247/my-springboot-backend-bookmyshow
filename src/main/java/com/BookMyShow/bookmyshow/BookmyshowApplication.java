package com.BookMyShow.bookmyshow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@SpringBootApplication
public class BookmyshowApplication {

	public static void main(String[] args) {
		loadDotEnv();
		SpringApplication.run(BookmyshowApplication.class, args);
	}

	private static void loadDotEnv() {
		Path envPath = Path.of(".env");
		if (!Files.isRegularFile(envPath)) {
			return;
		}

		Properties properties = new Properties();
		try (InputStream inputStream = Files.newInputStream(envPath)) {
			properties.load(inputStream);
		} catch (IOException e) {
			System.out.println("Could not read .env file: " + e.getMessage());
			return;
		}

		properties.forEach((key, value) -> {
			String propertyName = String.valueOf(key).trim();
			String propertyValue = String.valueOf(value).trim();
			if (propertyName.isEmpty() || propertyValue.isEmpty()) {
				return;
			}

			System.setProperty(propertyName, propertyValue);
			System.setProperty(toEnvStyleKey(propertyName), propertyValue);
		});
	}

	private static String toEnvStyleKey(String propertyName) {
		return propertyName.replace('.', '_').replace('-', '_').toUpperCase();
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
}
