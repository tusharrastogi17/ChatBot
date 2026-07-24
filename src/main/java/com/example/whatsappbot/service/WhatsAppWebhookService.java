package com.example.whatsappbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service class handling verification and payload logging for WhatsApp webhooks.
 *
 * @author WhatsApp Chatbot Developer
 * @version 1.0
 */
@Service
public class WhatsAppWebhookService {

    private final String verifyToken;

    /**
     * Constructor injection for configuration values.
     *
     * @param verifyToken the configured WhatsApp verification token
     */
    public WhatsAppWebhookService(@Value("${whatsapp.verify-token}") String verifyToken) {
        this.verifyToken = verifyToken;
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
