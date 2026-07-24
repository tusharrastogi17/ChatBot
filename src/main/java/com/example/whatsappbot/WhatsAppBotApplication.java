package com.example.whatsappbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entrypoint for the WhatsApp Chatbot Spring Boot Webhook Application.
 *
 * @author WhatsApp Chatbot Developer
 * @version 1.0
 */
@SpringBootApplication
public class WhatsAppBotApplication {

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(WhatsAppBotApplication.class, args);
    }
}
