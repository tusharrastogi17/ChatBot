package com.example.whatsappbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Service class handling verification, payload parsing, and reply sending for WhatsApp webhooks.
 *
 * @author WhatsApp Chatbot Developer
 * @version 1.1
 */
@Service
public class WhatsAppWebhookService {

    private final String verifyToken;
    private final String accessToken;
    private final String phoneNumberId;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructor injection for configuration values and client builders.
     *
     * @param verifyToken       the configured WhatsApp verification token
     * @param accessToken      the configured Meta WhatsApp Cloud API access token
     * @param phoneNumberId    the configured sender Phone Number ID
     * @param restClientBuilder Builder to create RestClient instance
     * @param objectMapper      JSON ObjectMapper instance
     */
    public WhatsAppWebhookService(
            @Value("${whatsapp.verify-token}") String verifyToken,
            @Value("${whatsapp.access-token}") String accessToken,
            @Value("${whatsapp.phone-number-id}") String phoneNumberId,
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper) {
        this.verifyToken = verifyToken;
        this.accessToken = accessToken;
        this.phoneNumberId = phoneNumberId;
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder
                .baseUrl("https://graph.facebook.com/v20.0")
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * Verifies the incoming subscription request from Meta against the configured token.
     *
     * @param token the verification token sent by Meta
     * @return true if tokens match, false otherwise
     */
    public boolean verifyToken(String token) {
        return this.verifyToken.equals(token);
    }

    /**
     * Processes incoming webhook event payload and sends replies to text messages.
     *
     * @param payload raw JSON body received from Meta
     */
    public void processIncomingWebhook(String payload) {
        logIncomingEvent(payload);
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode entryNode = root.path("entry");
            if (entryNode.isArray()) {
                for (JsonNode entry : entryNode) {
                    JsonNode changesNode = entry.path("changes");
                    if (changesNode.isArray()) {
                        for (JsonNode change : changesNode) {
                            JsonNode valueNode = change.path("value");
                            JsonNode messagesNode = valueNode.path("messages");
                            if (messagesNode.isArray() && !messagesNode.isEmpty()) {
                                for (JsonNode message : messagesNode) {
                                    String from = message.path("from").asText();
                                    String type = message.path("type").asText();
                                    if ("text".equals(type)) {
                                        String body = message.path("text").path("body").asText();
                                        // Send echo reply back to sender
                                        sendWhatsAppMessage(from, "Hello! I received your message: \"" + body + "\"");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse incoming webhook payload: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends a text message to a specific phone number using Meta WhatsApp Cloud API.
     *
     * @param toPhoneNumber recipient phone number (with country code, no + sign)
     * @param textMessage   the text content of the message
     */
    public void sendWhatsAppMessage(String toPhoneNumber, String textMessage) {
        if (accessToken == null || accessToken.isEmpty() || phoneNumberId == null || phoneNumberId.isEmpty()) {
            System.out.println("WhatsApp API credentials are not configured. Message send skipped.");
            return;
        }

        Map<String, Object> requestBody = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", toPhoneNumber,
                "type", "text",
                "text", Map.of(
                        "preview_url", false,
                        "body", textMessage
                )
        );

        try {
            String response = restClient.post()
                    .uri("/{phoneNumberId}/messages", phoneNumberId)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
            System.out.println("Reply sent successfully. API Response: " + response);
        } catch (Exception e) {
            System.err.println("Failed to send WhatsApp message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Logs the incoming webhook JSON event payload.
     *
     * @param payload raw JSON body received from Meta
     */
    public void logIncomingEvent(String payload) {
        System.out.println("========================");
        System.out.println("Incoming WhatsApp Event");
        System.out.println("========================");
        System.out.println(payload);
    }
}

