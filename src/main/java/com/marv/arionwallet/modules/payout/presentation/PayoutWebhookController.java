package com.marv.arionwallet.modules.payout.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marv.arionwallet.core.dto.ApiResponse;
import com.marv.arionwallet.modules.payments.application.PaymentWebhookService;
import com.marv.arionwallet.modules.payout.application.PayoutWebhookService;
import com.marv.arionwallet.modules.withdrawal.presentation.WithdrawalResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks/payouts")
@RequiredArgsConstructor
public class PayoutWebhookController {

    private final PayoutWebhookService payoutWebhookService;
    private final ObjectMapper objectMapper; // better than new ObjectMapper()

    @PostMapping
    public ApiResponse<WithdrawalResponseDto> webhook(
            HttpServletRequest request,
            @RequestBody String rawBody
    ) throws Exception {

        // parse AFTER receiving raw body
        PayoutWebhookRequestDto dto =
                objectMapper.readValue(rawBody, PayoutWebhookRequestDto.class);

        WithdrawalResponseDto response =
                payoutWebhookService.handleWebhook(
                        dto.getProviderReference(),
                        dto.getStatus(),
                        request,
                        rawBody
                );

        return ApiResponse.ok("Payout webhook processed", response);
    }
}
