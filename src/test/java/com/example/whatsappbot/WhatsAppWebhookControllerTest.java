package com.example.whatsappbot;

import com.example.whatsappbot.controller.WhatsAppWebhookController;
import com.example.whatsappbot.service.WhatsAppWebhookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link WhatsAppWebhookController} to verify verification and event receipt.
 *
 * @author WhatsApp Chatbot Developer
 * @version 1.0
 */
@WebMvcTest(WhatsAppWebhookController.class)
class WhatsAppWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WhatsAppWebhookService webhookService;

    /**
     * Verifies that GET /webhook returns 200 OK and the challenge string
     * when the correct verification token is supplied.
     */
    @Test
    void testVerifyWebhook_Success() throws Exception {
        String verifyToken = "my_verify_token";
        String challenge = "1158201444";

        when(webhookService.verifyToken(verifyToken)).thenReturn(true);

        mockMvc.perform(get("/webhook")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", verifyToken)
                        .param("hub.challenge", challenge))
                .andExpect(status().isOk())
                .andExpect(content().string(challenge));
    }

    /**
     * Verifies that GET /webhook returns 403 Forbidden
     * when the incorrect verification token is supplied.
     */
    @Test
    void testVerifyWebhook_Failure() throws Exception {
        String verifyToken = "wrong_token";

        when(webhookService.verifyToken(verifyToken)).thenReturn(false);

        mockMvc.perform(get("/webhook")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", verifyToken)
                        .param("hub.challenge", "1158201444"))
                .andExpect(status().isForbidden());
    }

    /**
     * Verifies that POST /webhook returns 200 OK and processes the request body successfully.
     */
    @Test
    void testReceiveWebhookEvent() throws Exception {
        String payload = "{\"object\":\"whatsapp_business_account\",\"entry\":[]}";

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string("EVENT_RECEIVED"));
    }
}
