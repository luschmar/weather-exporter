package ch.luschmar.weatherexporter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class WeatherExporterApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeatherExporterApplication.class, args);
	}

}
