package com.marv.arionwallet.modules.payout.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marv.arionwallet.modules.payments.domain.WebhookSignatureVerifier;
import com.marv.arionwallet.modules.payments.presentation.PaymentWebhookStatus;
import com.marv.arionwallet.modules.payout.presentation.PayoutWebhookRequestDto;
import com.marv.arionwallet.modules.payout.presentation.PayoutWebhookStatus;
import com.marv.arionwallet.modules.withdrawal.application.WithdrawalService;
import com.marv.arionwallet.modules.withdrawal.presentation.WithdrawalResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PayoutWebhookService {

    private final WebhookSignatureVerifier webhookSignatureVerifier;
    private final WithdrawalService withdrawalService;
    private final ObjectMapper objectMapper;

//    @Transactional
//    public WithdrawalResponseDto handleWebhook(
//            String providerReference,
//            PayoutWebhookStatus status,
//            HttpServletRequest request,
//            String rawBody
//    ) {
//        webhookSignatureVerifier.verifyOrThrow(request, rawBody);
//
//        return withdrawalService.settleWithdrawalFromWebhook(
//                providerReference,
//                status
//        );
//    }

    @Transactional
    public WithdrawalResponseDto handlePaystackWebhook (HttpServletRequest request, String rawBody) {

        webhookSignatureVerifier.verifyOrThrow(request, rawBody);

        JsonNode payload;
        try {
            payload = objectMapper.readTree(rawBody);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid webhook Json");
        }

        String event = payload.path("event").asText("");
        JsonNode data = payload.path("data");

        String reference = data.path("reference").asText(null);
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("Missing transfer reference");
        }

        if ("transfer.success".equalsIgnoreCase(event)) {
            return withdrawalService.settleWithdrawalFromWebhook(reference, PayoutWebhookStatus.SUCCESS);
        }
        if ("transfer.failed".equalsIgnoreCase(event)) {
            return withdrawalService.settleWithdrawalFromWebhook(reference, PayoutWebhookStatus.FAILED);
        }

        return withdrawalService.settleWithdrawalFromWebhook(reference, PayoutWebhookStatus.FAILED);

    }

}
