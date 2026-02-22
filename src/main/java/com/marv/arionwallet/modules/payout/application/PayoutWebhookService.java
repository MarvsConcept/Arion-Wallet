package com.marv.arionwallet.modules.payout.application;

import com.marv.arionwallet.modules.payments.domain.WebhookSignatureVerifier;
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

    @Transactional
    public WithdrawalResponseDto handleWebhook(
            String providerReference,
            PayoutWebhookStatus status,
            HttpServletRequest request,
            String rawBody
    ) {
        webhookSignatureVerifier.verifyOrThrow(request, rawBody);

        return withdrawalService.settleWithdrawalFromWebhook(
                providerReference,
                status
        );
    }
}
