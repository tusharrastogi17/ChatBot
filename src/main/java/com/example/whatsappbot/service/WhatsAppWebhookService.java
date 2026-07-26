package com.example.whatsappbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class WhatsAppWebhookService {

    @Value("${whatsapp.phone.number.id}")
    private String phoneNumberId;

    @Value("${whatsapp.access.token}")
    private String accessToken;

    private final RestTemplate restTemplate = new RestTemplate();

    public void processIncomingWebhook(Map<String, Object> payload) {
        try {
            System.out.println("========================");
            System.out.println("Incoming WhatsApp Event");
            System.out.println("========================");
            System.out.println(payload);

            List<Map<String, Object>> entryList = (List<Map<String, Object>>) payload.get("entry");
            if (entryList == null || entryList.isEmpty()) {
                return;
            }

            Map<String, Object> entry = entryList.get(0);
            List<Map<String, Object>> changes = (List<Map<String, Object>>) entry.get("changes");
            if (changes == null || changes.isEmpty()) {
                return;
            }

            Map<String, Object> value = (Map<String, Object>) changes.get(0).get("value");
            if (value == null) {
                return;
            }

            // Check if this payload contains user messages (ignores account_alerts, statuses, etc.)
            if (value.containsKey("messages")) {
                List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");
                if (messages != null && !messages.isEmpty()) {
                    for (Map<String, Object> message : messages) {
                        String senderPhone = (String) message.get("from");

                        Map<String, Object> textObj = (Map<String, Object>) message.get("text");
                        String incomingText = textObj != null ? (String) textObj.get("body") : "";

                        String trimmedText = incomingText.trim().toLowerCase();

                        int hour = java.time.LocalTime.now().getHour();
                        String timeGreeting = (hour < 12) ? "Good morning ☀️" : (hour < 17) ? "Good afternoon 🌤️" : "Good evening 🌙";

                        String reply = switch (trimmedText) {
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
                                    ✅ Microservices

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

                        sendWhatsAppMessage(senderPhone, reply);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing incoming webhook payload: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendWhatsAppMessage(String to, String textResponse) {
        String url = "https://graph.facebook.com/v25.0/" + phoneNumberId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String escapedResponse = textResponse == null ? "" : textResponse
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");

        String body = """
            {
              "messaging_product": "whatsapp",
              "recipient_type": "individual",
              "to": "%s",
              "type": "text",
              "text": { "body": "%s" }
            }
            """.formatted(to, escapedResponse);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            System.out.println("WhatsApp API Response: " + response.getStatusCode());
        } catch (HttpClientErrorException e) {
            System.err.println("META API REJECTED REQUEST: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("FAILED TO SEND MESSAGE: " + e.getMessage());
        }
    }
}
