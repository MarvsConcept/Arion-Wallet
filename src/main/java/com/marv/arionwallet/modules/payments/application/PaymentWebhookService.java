package com.marv.arionwallet.modules.payments.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marv.arionwallet.modules.funding.application.FundingService;
import com.marv.arionwallet.modules.payments.domain.WebhookSignatureVerifier;
import com.marv.arionwallet.modules.payments.presentation.PaymentWebhookStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentWebhookService {

    private final WebhookSignatureVerifier signatureVerifier;
    private final FundingService fundingService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void handlePaystackFundingWebhook(
            HttpServletRequest request,
            String rawBody
    ) {
        // Verify signature
        signatureVerifier.verifyOrThrow(request, rawBody);

        // Parse Paystack payload
        JsonNode payload;
        try {
            payload = objectMapper.readTree(rawBody);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid webhook JSON");
        }

        String event = payload.path("event").asText("");
        JsonNode data = payload.path("data");

        // Paystack success event is usually "charge.success"
        if (!"charge.success".equalsIgnoreCase(event)) {
            // Ignore others safely
            return;
        }

        String reference = data.path("reference").asText(null);
        String paystackStatus = data.path("status").asText("");
        long amount = data.path("amount").asLong(0L);
        String currency = data.path("currency").asText("NGN");

        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("Missing reference");
        }

        PaymentWebhookStatus status = "success".equalsIgnoreCase(paystackStatus)
                ? PaymentWebhookStatus.SUCCESS
                : PaymentWebhookStatus.FAILED;

        // Settle internally

        fundingService.settleFundingFromProviderEvent(reference, status, amount, currency);

    }
}
