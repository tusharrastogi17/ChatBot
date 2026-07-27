package com.example.whatsappbot.service;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WhatsAppMessageRouter {

    private final RestTemplate restTemplate;

    public WhatsAppMessageRouter() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(90000); // 90 seconds connect timeout
        factory.setReadTimeout(90000);    // 90 seconds read timeout
        this.restTemplate = new RestTemplate(factory);
    }

    public String getResponse(String trimmedText, String incomingText) {
        int hour = java.time.LocalTime.now().getHour();
        String timeGreeting = (hour < 12) ? "Good morning" : (hour < 17) ? "Good afternoon" : "Good evening";

        return switch (trimmedText) {
            // Greeting
            case "hi", "hello", "hey", "start", "welcome" ->
                """
                %s! Welcome to Parivaar Assistant.

                1. About Me
                2. Parivaar Status
                3. Current Time
                4. Help & Info
                5. Contact Us

                Reply with 1, 2, 3, 4, or 5.
                """.formatted(timeGreeting);

            // Option 1
            case "1", "one", "about", "about me" ->
                """
                ABOUT THE DEVELOPER
                Hello! I am a Java Backend Developer specializing in building high-performance APIs and scalable cloud applications.

                Core Technical Stack:
                - Java & Core Concepts
                - Spring Boot Framework
                - RESTful API Architecture
                - Microservices & Cloud Services

                Reply 4 for Main Menu.
                """;

            // Option 2
            case "2", "two", "parivaar", "family" -> {
                try {
                    String healthStatus = restTemplate.getForObject("https://parivaar-1b1m.onrender.com/pariVaar/health", String.class);
                    String statusText = (healthStatus != null ? healthStatus.trim() : "No status response");
                    yield """
                        PARIVAAR SYSTEM STATUS
                        Status: %s
                        Service Health: Operational

                        Reply 4 for Main Menu.
                        """.formatted(statusText);
                } catch (Exception e) {
                    yield """
                        PARIVAAR SYSTEM STATUS
                        Status: Offline / Unreachable
                        Service Health: Connection Error

                        Reply 4 for Main Menu.
                        """;
                }
            }

            // Option 3
            case "3", "three", "time", "clock" -> {
                String formattedTime = java.time.LocalTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a"));
                yield """
                    SERVER CLOCK & TIMESTAMP
                    Current Server Time: %s
                    Timezone: System Standard

                    Reply 4 for Main Menu.
                    """.formatted(formattedTime);
            }

            // Option 4
            case "4", "four", "help", "menu", "support" ->
                """
                NAVIGATION & HELP MENU
                Please reply with a number (1-5):

                1. About Me - Developer Bio & Tech Stack
                2. Parivaar - Live System Health Status
                3. Clock - Server Time & Timestamp
                4. Help - Navigation & Usage Guide
                5. Contact - Email & Web Details
                """;

            // Option 5
            case "5", "five", "contact" ->
                """
                CONTACT INFORMATION
                Email: gmail@email.com
                Website: https://parivaar-5ef19.web.app

                Reply 4 for Main Menu.
                """;

            case "bye", "goodbye", "exit", "stop" ->
                """
                Thank you for using Parivaar Bot!
                Have a great day ahead!
                """;

            default -> {
                if (incomingText.isEmpty()) {
                    yield """
                        Welcome!

                        1. About Me
                        2. Parivaar Status
                        3. Current Time
                        4. Help & Info
                        5. Contact Us

                        Reply with a number (1 to 5).
                        """;
                } else {
                    yield """
                        Unrecognized command. Please choose from the options below:

                        1. About Me
                        2. Parivaar Status
                        3. Current Time
                        4. Help & Info
                        5. Contact Us

                        Reply with a number (1 to 5).
                        """;
                }
            }
        };
    }
}
