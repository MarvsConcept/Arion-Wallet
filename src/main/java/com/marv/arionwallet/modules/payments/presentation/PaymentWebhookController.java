package com.marv.arionwallet.modules.payments.presentation;

import com.marv.arionwallet.core.dto.ApiResponse;
import com.marv.arionwallet.modules.funding.presentation.CompleteFundingResponseDto;
import com.marv.arionwallet.modules.payments.application.PaymentWebhookService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks/payments")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentWebhookService paymentWebhookService;

    @PostMapping
    public ApiResponse<CompleteFundingResponseDto> handle(
            @Valid @RequestBody PaymentWebhookRequestDto body,
            HttpServletRequest request
    ) {

        CompleteFundingResponseDto response =
                paymentWebhookService.handleFundingWebhook(
                        body.getProviderReference(),
                        body.getStatus(),
                        request,
                        ""
                );
        return ApiResponse.ok("Webhook processed", response);
    }
}
