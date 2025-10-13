package com.unapi.rotaract.rotaract_d4465_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class RotaractD4465ApiApplication {

	public static void main(String[] args) {

		// Cargar variables desde el archivo .env (solo en entorno local)
		Dotenv dotenv = Dotenv.load();

		// Configuración MySQL
		System.setProperty("MYSQL_JDBC_URL", dotenv.get("MYSQL_JDBC_URL"));
		System.setProperty("MYSQL_USER", dotenv.get("MYSQL_USER"));
		System.setProperty("MYSQL_PASSWORD", dotenv.get("MYSQL_PASSWORD"));
		System.setProperty("MYSQL_HOST", dotenv.get("MYSQL_HOST"));
		System.setProperty("MYSQL_PORT", dotenv.get("MYSQL_PORT"));
		System.setProperty("MYSQL_DATABASE", dotenv.get("MYSQL_DATABASE"));
		System.setProperty("DB_DRIVER", "com.mysql.cj.jdbc.Driver");

		// Configuración JWT
		System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
		System.setProperty("JWT_EXPIRATION", dotenv.get("JWT_EXPIRATION"));
		System.setProperty("JWT_REFRESH_EXPIRATION", dotenv.get("JWT_REFRESH_EXPIRATION"));

		SpringApplication.run(RotaractD4465ApiApplication.class, args);
	}
}
