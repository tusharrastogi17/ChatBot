package com.example.whatsappbot.controller;

import com.example.whatsappbot.service.WhatsAppWebhookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for handling Meta WhatsApp Cloud API webhooks.
 *
 * @author WhatsApp Chatbot Developer
 * @version 1.0
 */
@RestController
@RequestMapping("/webhook")
public class WhatsAppWebhookController {

    private final WhatsAppWebhookService webhookService;

    /**
     * Constructor injection for WhatsAppWebhookService.
     *
     * @param webhookService service class to handle webhook logic
     */
    public WhatsAppWebhookController(WhatsAppWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    /**
     * Verification endpoint GET /webhook used by Meta to verify subscription.
     *
     * @param mode         hub.mode parameter (should be "subscribe")
     * @param verifyToken  hub.verify_token parameter
     * @param challenge    hub.challenge parameter
     * @return challenge plain text if verified, HTTP 403 Forbidden otherwise
     */
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String verifyToken,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {

        if ("subscribe".equals(mode) && webhookService.verifyToken(verifyToken)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Event notification endpoint POST /webhook used by Meta to send WhatsApp updates.
     *
     * @param payload raw JSON body string from Meta
     * @return HTTP 200 OK
     */
    @PostMapping
    public ResponseEntity<Void> receiveWebhookEvent(@RequestBody String payload) {
        webhookService.logIncomingEvent(payload);
        return ResponseEntity.ok().build();
    }
}
