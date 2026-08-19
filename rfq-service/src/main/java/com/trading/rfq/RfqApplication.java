package com.trading.rfq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the RFQ (Request for Quote) Microservice.
 * 
 * In Spring Boot:
 * - @SpringBootApplication is a convenience annotation that encompasses:
 *   1. @Configuration: Tags the class as a source of bean definitions for the application context.
 *   2. @EnableAutoConfiguration: Tells Spring Boot to automatically configure beans based on classpath dependencies.
 *   3. @ComponentScan: Automatically scans packages for @Component, @Service, @Repository, @RestController.
 */
@SpringBootApplication
public class RfqApplication {

    public static void main(String[] args) {
        // SpringApplication.run() boots up the application:
        // 1. Sets up the Spring ApplicationContext.
        // 2. Starts the embedded Tomcat servlet web server (listening on port 8081).
        // 3. Initializes all Spring beans and registers REST endpoints.
        SpringApplication.run(RfqApplication.class, args);
    }
}
