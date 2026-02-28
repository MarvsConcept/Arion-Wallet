package com.marv.arionwallet.modules.payments.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marv.arionwallet.modules.funding.application.FundingService;
import com.marv.arionwallet.modules.payments.domain.FlutterwaveWebhookSignatureVerifier;
import com.marv.arionwallet.modules.payout.presentation.PayoutWebhookStatus;
import com.marv.arionwallet.modules.withdrawal.application.WithdrawalService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks/flutterwave")
@RequiredArgsConstructor
public class FlutterwaveWebhookController {

    private final FlutterwaveWebhookSignatureVerifier verifier;
    private final FundingService fundingService;
    private final WithdrawalService withdrawalService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<String> handle(HttpServletRequest request, @RequestBody String rawBody) {

        verifier.verifyOrThrow(request, rawBody);

        JsonNode payload;
        try {
            payload = objectMapper.readTree(rawBody);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid JSON");
        }

        // Flutterwave event format differs by product.
        // Practical approach: read tx_ref / reference and status from payload.
        String status = payload.path("data").path("status").asText("");
        String txRef = payload.path("data").path("tx_ref").asText(null);
        String transferRef = payload.path("data").path("reference").asText(null);

        // FUNDING: tx_ref
        if (txRef != null && !txRef.isBlank()) {
            PaymentWebhookStatus mapped =
                    "successful".equalsIgnoreCase(status) || "success".equalsIgnoreCase(status)
                            ? PaymentWebhookStatus.SUCCESS
                            : PaymentWebhookStatus.FAILED;

            long amount = payload.path("data").path("amount").asLong(0);
            String currency = payload.path("data").path("currency").asText("NGN");

            fundingService.settleFundingFromProviderEvent(txRef, mapped, amount, currency);
            return ResponseEntity.ok("ok");
        }

        // PAYOUT: reference
        if (transferRef != null && !transferRef.isBlank()) {
            PayoutWebhookStatus mapped =
                    "successful".equalsIgnoreCase(status) || "success".equalsIgnoreCase(status)
                            ? PayoutWebhookStatus.SUCCESS
                            : PayoutWebhookStatus.FAILED;

            withdrawalService.settleWithdrawalFromWebhook(transferRef, mapped);
            return ResponseEntity.ok("ok");
        }

        return ResponseEntity.ok("ignored");
    }
}