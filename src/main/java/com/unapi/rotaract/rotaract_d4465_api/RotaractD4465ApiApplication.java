package com.unapi.rotaract.rotaract_d4465_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class RotaractD4465ApiApplication {

	public static void main(String[] args) {

		// Cargar variables del archivo .env
        Dotenv dotenv = Dotenv.load();

        // Asignar propiedades de entorno a spring
        System.setProperty("DB_URL", dotenv.get("MYSQL_URL"));
        System.setProperty("DB_USERNAME", dotenv.get("MYSQL_USER"));
        System.setProperty("DB_PASSWORD", dotenv.get("MYSQL_PASSWORD"));
        System.setProperty("DB_DRIVER", "com.mysql.cj.jdbc.Driver");

		SpringApplication.run(RotaractD4465ApiApplication.class, args);
	}

}
