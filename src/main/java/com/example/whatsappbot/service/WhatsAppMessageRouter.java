package com.example.whatsappbot.service;

import org.springframework.stereotype.Service;

@Service
public class WhatsAppMessageRouter {

    public String getResponse(String trimmedText, String incomingText) {
        int hour = java.time.LocalTime.now().getHour();
        String timeGreeting = (hour < 12) ? "Good morning ☀️" : (hour < 17) ? "Good afternoon 🌤️" : "Good evening 🌙";

        return switch (trimmedText) {
            // Greeting
            case "hi", "hello", "hey", "start", "welcome" -> """
                    %s! 👋 Welcome to *My WhatsApp Bot*!

                    Please choose an option by sending the number:

                    1️⃣ About Me
                    2️⃣ Parivaar 👨\u200D👩\u200D👧\u200D👦
                    3️⃣ Current Time ⏰
                    4️⃣ Help ❓
                    5️⃣ Contact 📞

                    Just reply with *1*, *2*, *3*, *4* or *5*.
                    """.formatted(timeGreeting);

            // Option 1
            case "1", "one", "about", "about me" -> """
                    👨\u200D💻 *About Me*

                    Hello!
                    I am a Java Backend Developer with experience in:
                    ✅ Java
                    ✅ Spring Boot
                    ✅ REST APIs
                    ✅ Microservicex

                    Happy to connect with you!
                    """;

            // Option 2
            case "2", "two", "parivaar", "family" -> """
                    👨\u200D👩\u200D👧\u200D👦 *Parivaar*

                    Parivaar is a Family Relationship Management System.

                    Features:
                    ✅ Build your family tree
                    ✅ Find relationships
                    ✅ Add family members
                    ✅ Visualize complete family hierarchy

                    🚀 More exciting features coming soon!
                    """;

            // Option 3
            case "3", "three", "time", "clock" ->
                    "⏰ Current Server Time: " +
                    java.time.LocalTime.now()
                            .format(java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a"));

            // Option 4
            case "4", "four", "help", "menu", "support" -> """
                    📋 *Main Menu*

                    1️⃣ About Me
                    2️⃣ Parivaar
                    3️⃣ Current Time
                    4️⃣ Help
                    5️⃣ Contact

                    Reply with the option number.
                    """;

            // Option 5
            case "5", "five", "contact" -> """
                    📞 *Contact Information*

                    Email: your@email.com
                    Website: https://yourwebsite.com

                    Thank you for contacting us!
                    """;

            case "bye", "goodbye", "exit", "stop" ->
                    "👋 Thank you for chatting with us. Have a wonderful day!";

            default -> {
                if (incomingText.isEmpty()) {
                    yield """
                            👋 Welcome to *My WhatsApp Bot*!

                            Please choose an option by sending the number:

                            1️⃣ About Me
                            2️⃣ Parivaar 👨\u200D👩\u200D👧\u200D👦
                            3️⃣ Current Time ⏰
                            4️⃣ Help ❓
                            5️⃣ Contact 📞

                            Just reply with *1*, *2*, *3*, *4* or *5*.
                            """;
                } else {
                    yield """
                            ❌ Invalid option.

                            Please choose one of the following:

                            1️⃣ About Me
                            2️⃣ Parivaar 👨\u200D👩\u200D👧\u200D👦
                            3️⃣ Current Time ⏰
                            4️⃣ Help ❓
                            5️⃣ Contact 📞
                            """;
                }
            }
        };
    }
}
