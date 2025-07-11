package com.MediHubAPI;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javax.crypto.SecretKey;

@SpringBootApplication
public class MediHubApiApplication {

	public static void main(String[] args) {
		// To generate key: Uncomment below line, run, then comment it back
		//generateJwtSecretKey();

		SpringApplication.run(MediHubApiApplication.class, args);
	}

	private static void generateJwtSecretKey() {
		try {
			// 1. Generate secure key
			SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

			// 2. Base64 encode the key
			String base64Key = Encoders.BASE64.encode(key.getEncoded());

			// 3. Print the key with clear formatting
			System.out.println("\n=============================================");
			System.out.println("COPY THIS JWT SECRET KEY TO application.properties:");
			System.out.println("app.jwt-secret=" + base64Key);
			System.out.println("=============================================\n");

			// 4. Exit after generation (don't start Spring Boot)
			System.exit(0);

		} catch (Exception e) {
			System.err.println("❌ Error generating JWT key: " + e.getMessage());
			System.exit(1);
		}
	}
}