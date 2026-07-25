package com.example.whatsappbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookService.class);

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
                .baseUrl("https://graph.facebook.com/v25.0")
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .defaultHeader("Content-Type", "application/json")
                .build();

        // Log config at startup to verify credentials are loaded
        log.info("=== WhatsApp Bot Config ===");
        log.info("Phone Number ID: {}", phoneNumberId);
        log.info("Access Token loaded: {} (length: {})",
                accessToken != null && !accessToken.isEmpty() ? "YES" : "NO",
                accessToken != null ? accessToken.length() : 0);
        log.info("Verify Token loaded: {}", verifyToken != null && !verifyToken.isEmpty() ? "YES" : "NO");
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
                                    log.info("Message received - from: {}, type: {}", from, type);
                                    if ("text".equals(type)) {
                                        String body = message.path("text").path("body").asText().trim();
                                        log.info("Text message body: '{}' - Sending reply to {}", body, from);
                                        // Reply logic: "hi" → "hello", anything else → echo
                                        String reply = "hi".equalsIgnoreCase(body)
                                                ? "hello"
                                                : "Hello! I received your message: \"" + body + "\"";
                                        sendWhatsAppMessage(from, reply);
                                    } else {
                                        log.info("Ignoring non-text message of type: {}", type);
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
            log.error("!!! WhatsApp API credentials NOT configured - accessToken empty: {}, phoneNumberId empty: {} - SKIPPING message send !!!",
                    accessToken == null || accessToken.isEmpty(),
                    phoneNumberId == null || phoneNumberId.isEmpty());
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
            log.info("Sending message to {} via API endpoint /{}/messages", toPhoneNumber, phoneNumberId);
            String response = restClient.post()
                    .uri("/{phoneNumberId}/messages", phoneNumberId)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
            log.info("Reply sent successfully! API Response: {}", response);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("META API ERROR (ClientError): {}", e.getResponseBodyAsString(), e);
            System.out.println("META API ERROR: " + e.getResponseBodyAsString());
        } catch (org.springframework.web.client.RestClientResponseException e) {
            log.error("META API ERROR (ResponseError): {}", e.getResponseBodyAsString(), e);
            System.out.println("META API ERROR: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("FAILED to send WhatsApp message to {}: {}", toPhoneNumber, e.getMessage(), e);
            System.out.println("GENERAL ERROR: " + e.getMessage());
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

