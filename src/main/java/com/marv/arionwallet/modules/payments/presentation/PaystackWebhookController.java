package com.marv.arionwallet.modules.payments.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marv.arionwallet.modules.payments.application.PaymentWebhookService;
import com.marv.arionwallet.modules.payments.domain.WebhookSignatureVerifier;
import com.marv.arionwallet.modules.payout.application.PayoutWebhookService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks/paystack")
@RequiredArgsConstructor
public class PaystackWebhookController {

    private final WebhookSignatureVerifier signatureVerifier;
    private final PaymentWebhookService paymentWebhookService;
    private final PayoutWebhookService payoutWebhookService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<String> handle(HttpServletRequest request, @RequestBody String rawBody) {

        // Verify once (shared)
        signatureVerifier.verifyOrThrow(request, rawBody);

        JsonNode payload;
        try {
            payload = objectMapper.readTree(rawBody);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid JSON");
        }

        String event = payload.path("event").asText("");

        // Route by event type
        if ("charge.success".equalsIgnoreCase(event)) {
            paymentWebhookService.handlePaystackFundingWebhook(request, rawBody);
            return ResponseEntity.ok("ok");
        }

        if ("transfer.success".equalsIgnoreCase(event) || "transfer.failed".equalsIgnoreCase(event)) {
            payoutWebhookService.handlePaystackWebhook(request, rawBody);
            return ResponseEntity.ok("ok");
        }

        // Ignore everything else
        return ResponseEntity.ok("ignored");
    }
}
