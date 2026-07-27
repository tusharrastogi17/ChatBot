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
        String timeGreeting = (hour < 12) ? "Good morning ☀️" : (hour < 17) ? "Good afternoon 🌤️" : "Good evening 🌙";

        return switch (trimmedText) {
            // Greeting
            case "hi", "hello", "hey", "start", "welcome" ->
                """
                ╔═════════════════════════════════╗
                ║   🤖 *PARIVAAR ASSISTANT* 🤖   ║
                ╚═════════════════════════════════╝
                %s! 👋 Welcome to our bot.

                > 1️⃣  *About Me*
                >     └─ _Developer Profile & Tech Stack_
                >
                > 2️⃣  *Parivaar Status*
                >     └─ _Live System Health Check_
                >
                > 3️⃣  *Current Time*
                >     └─ _Server Clock & Timestamp_
                >
                > 4️⃣  *Help & Info*
                >     └─ _Bot Navigation & Options_
                >
                > 5️⃣  *Contact Us*
                >     └─ _Email, Web & Support_

                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                💡 *Quick Action:* Reply with *1*, *2*, *3*, *4*, or *5*.
                """.formatted(timeGreeting);

            // Option 1
            case "1", "one", "about", "about me" ->
                """
                ┌─────────────────────────────────┐
                │  👨‍💻 *ABOUT THE DEVELOPER*     │
                └─────────────────────────────────┘
                > Hello! I am a Java Backend Developer specializing in building high-performance APIs and scalable cloud applications.
                >
                > 🚀 *Core Technical Stack:*
                >   🔹 *Java & Core Concepts*
                >   🔹 *Spring Boot Framework*
                >   🔹 *RESTful API Architecture*
                >   🔹 *Microservices & Cloud Services*
                >
                > 🤝 _Open for collaboration and networking!_

                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                💡 _Reply *4* anytime for Main Menu._
                """;

            // Option 2
            case "2", "two", "parivaar", "family" -> {
                try {
                    String healthStatus = restTemplate.getForObject("https://parivaar-1b1m.onrender.com/pariVaar/health", String.class);
                    String statusText = (healthStatus != null ? healthStatus.trim() : "No status response") + " ✅";
                    yield """
                        ┌─────────────────────────────────┐
                        │  👨‍👩‍👧‍👦 *PARIVAAR SYSTEM STATUS*    │
                        └─────────────────────────────────┘
                        > 📡 *Live Status Check:*
                        > `%s`
                        >
                        > 🟢 *Service Health:* Operational

                        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        💡 _Reply *4* anytime for Main Menu._
                        """.formatted(statusText);
                } catch (Exception e) {
                    yield """
                        ┌─────────────────────────────────┐
                        │  👨‍👩‍👧‍👦 *PARIVAAR SYSTEM STATUS*    │
                        └─────────────────────────────────┘
                        > 📡 *Live Status Check:*
                        > ⚠️ *Offline / Unreachable*
                        >
                        > 🔴 *Service Health:* Connection Error

                        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        💡 _Reply *4* anytime for Main Menu._
                        """;
                }
            }

            // Option 3
            case "3", "three", "time", "clock" -> {
                String formattedTime = java.time.LocalTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a"));
                yield """
                    ┌─────────────────────────────────┐
                    │  ⏰ *SERVER CLOCK & TIMESTAMP*  │
                    └─────────────────────────────────┘
                    > 🕐 *Current Server Time:*
                    > `%s`
                    >
                    > 🌍 *Timezone:* System Standard

                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                    💡 _Reply *4* anytime for Main Menu._
                    """.formatted(formattedTime);
            }

            // Option 4
            case "4", "four", "help", "menu", "support" ->
                """
                ┌─────────────────────────────────┐
                │  📋 *NAVIGATION & HELP MENU*    │
                └─────────────────────────────────┘
                > Please reply with a number (*1* - *5*):
                >
                >   1️⃣  *About Me* — Developer Bio & Tech Stack
                >   2️⃣  *Parivaar* — Live System Health Status
                >   3️⃣  *Clock* — Server Time & Timestamp
                >   4️⃣  *Help* — Navigation & Usage Guide
                >   5️⃣  *Contact* — Email & Web Details

                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                💬 _Need assistance? Simply reply with a number._
                """;

            // Option 5
            case "5", "five", "contact" ->
                """
                ┌─────────────────────────────────┐
                │  📞 *CONTACT INFORMATION*       │
                └─────────────────────────────────┘
                > Feel free to connect via:
                >
                >   📧 *Email:* `gmail@email.com`
                >   🌐 *Website:* https://parivaar-5ef19.web.app
                >
                > 💬 _We're happy to answer any questions!_

                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                💡 _Reply *4* anytime for Main Menu._
                """;

            case "bye", "goodbye", "exit", "stop" ->
                """
                ┌─────────────────────────────────┐
                │  👋 *THANK YOU FOR CHATTING*    │
                └─────────────────────────────────┘
                > Thank you for using Parivaar Bot! 🌟
                > Have a wonderful day ahead!
                """;

            default -> {
                if (incomingText.isEmpty()) {
                    yield """
                        ╔═════════════════════════════════╗
                        ║   🤖 *PARIVAAR ASSISTANT* 🤖   ║
                        ╚═════════════════════════════════╝
                        👋 Welcome!

                        > 1️⃣  *About Me*
                        > 2️⃣  *Parivaar Status*
                        > 3️⃣  *Current Time*
                        > 4️⃣  *Help & Main Menu*
                        > 5️⃣  *Contact Us*

                        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        💡 _Reply with a number (*1* to *5*)._
                        """;
                } else {
                    yield """
                        ┌─────────────────────────────────┐
                        │  ⚠️ *UNRECOGNIZED COMMAND*      │
                        └─────────────────────────────────┘
                        > Sorry, I didn't recognize that command.
                        >
                        > Please choose from the valid options below:
                        >
                        >   1️⃣  *About Me*
                        >   2️⃣  *Parivaar Status*
                        >   3️⃣  *Current Time*
                        >   4️⃣  *Help & Main Menu*
                        >   5️⃣  *Contact Us*

                        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        💡 _Reply with a number (*1* to *5*)._
                        """;
                }
            }
        };
    }
}
