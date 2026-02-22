package com.marv.arionwallet.modules.payments.application;

import com.marv.arionwallet.modules.funding.application.FundingService;
import com.marv.arionwallet.modules.funding.presentation.CompleteFundingResponseDto;
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

    @Transactional
    public CompleteFundingResponseDto handleFundingWebhook(
            String providerReference,
            PaymentWebhookStatus status,
            HttpServletRequest request,
            String rawBody
    ) {
        signatureVerifier.verifyOrThrow(request, rawBody);

        return fundingService.settleFundingFromProviderEvent(providerReference, status);

    }
}
