package com.example.whatsappbot.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller serving a privacy policy page required by Meta to take the app Live.
 */
@RestController
public class PrivacyPolicyController {

    @GetMapping(value = "/privacy-policy", produces = MediaType.TEXT_HTML_VALUE)
    public String privacyPolicy() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Privacy Policy - WhatsApp ChatBot</title>
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 800px; margin: 0 auto; padding: 40px 20px; color: #333; line-height: 1.6; }
                        h1 { color: #1a1a1a; border-bottom: 2px solid #25D366; padding-bottom: 10px; }
                        h2 { color: #2c2c2c; margin-top: 30px; }
                        p { margin: 10px 0; }
                        .updated { color: #666; font-style: italic; }
                    </style>
                </head>
                <body>
                    <h1>Privacy Policy</h1>
                    <p class="updated">Last updated: July 2026</p>

                    <h2>1. Introduction</h2>
                    <p>This WhatsApp ChatBot ("Service") is committed to protecting your privacy. This policy explains how we handle information when you interact with our bot.</p>

                    <h2>2. Information We Collect</h2>
                    <p>When you send a message to our WhatsApp bot, we receive your phone number and message content as provided by the WhatsApp Business API. We do not store or persist any personal data beyond the immediate message processing.</p>

                    <h2>3. How We Use Information</h2>
                    <p>We use the information solely to process your message and send an automated reply. Messages are processed in real-time and are not stored in any database.</p>

                    <h2>4. Data Sharing</h2>
                    <p>We do not sell, trade, or share your personal information with third parties. Communication occurs through Meta's WhatsApp Business Platform.</p>

                    <h2>5. Data Retention</h2>
                    <p>We do not retain any personal data. Messages are processed in memory and discarded immediately after a response is sent.</p>

                    <h2>6. Security</h2>
                    <p>All communication is encrypted via WhatsApp's end-to-end encryption and HTTPS protocols.</p>

                    <h2>7. Contact</h2>
                    <p>If you have questions about this privacy policy, please reach out via WhatsApp.</p>
                </body>
                </html>
                """;
    }
}
