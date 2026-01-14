package com.qdc.lims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the LIMS Spring Boot application.
 */
@SpringBootApplication
public class QdcLimsApplication {

	/**
	 * Starts the QDC-LIMS Spring Boot application.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(QdcLimsApplication.class, args);
	}

}
