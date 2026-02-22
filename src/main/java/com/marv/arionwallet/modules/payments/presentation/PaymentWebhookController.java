package com.marv.arionwallet.modules.payments.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    @PostMapping
    public ApiResponse<CompleteFundingResponseDto> handle(
            HttpServletRequest request,
            @RequestBody String rawBody
    ) throws Exception {

        PaymentWebhookRequestDto body =
                objectMapper.readValue(rawBody, PaymentWebhookRequestDto.class);

        CompleteFundingResponseDto response =
                paymentWebhookService.handleFundingWebhook(
                        body.getProviderReference(),
                        body.getStatus(),
                        request,
                        rawBody
                );

        return ApiResponse.ok("Webhook processed", response);
    }
}
