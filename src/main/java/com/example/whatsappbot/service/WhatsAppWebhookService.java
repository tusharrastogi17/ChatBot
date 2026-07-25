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
                        String reply;

                        switch (trimmedText) {
                            case "hi":
                            case "hello":
                            case "hey":
                                reply = "Hello! 👋 Welcome to WhatsApp Bot. How can I help you today?";
                                break;
                            case "help":
                            case "menu":
                                reply = """
                                    🤖 *WhatsApp Bot Menu*:
                                    • Type *hi* or *hello* - Greet the bot
                                    • Type *time* - Get current server time
                                    • Type *info* - Get bot details
                                    • Or send any message and I will assist you!
                                    """;
                                break;
                            case "time":
                                reply = "⏰ Current time: " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a"));
                                break;
                            case "info":
                                reply = "ℹ️ I am a Spring Boot WhatsApp Bot running on Java 17!";
                                break;
                            case "bye":
                            case "goodbye":
                                reply = "Goodbye! 👋 Have a wonderful day!";
                                break;
                            default:
                                if (!incomingText.isEmpty()) {
                                    reply = "🤖 I received: \"" + incomingText + "\". Type *help* to see what I can do!";
                                } else {
                                    reply = "Hello! How can I assist you?";
                                }
                                break;
                        }

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
