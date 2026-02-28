//package com.marv.arionwallet.modules.payout.presentation;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.marv.arionwallet.core.dto.ApiResponse;
//import com.marv.arionwallet.modules.payments.application.PaymentWebhookService;
//import com.marv.arionwallet.modules.payout.application.PayoutWebhookService;
//import com.marv.arionwallet.modules.withdrawal.presentation.WithdrawalResponseDto;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/v1/webhooks/payouts")
//@RequiredArgsConstructor
//public class PayoutWebhookController {
//
//    private final PayoutWebhookService payoutWebhookService;
//    private final ObjectMapper objectMapper;
//
//    @PostMapping
//    public ApiResponse<WithdrawalResponseDto> webhook(
//            HttpServletRequest request,
//            @RequestBody String rawBody
//    ) {
//
//        WithdrawalResponseDto response = payoutWebhookService.handlePaystackWebhook(request, rawBody);
//
//        return ApiResponse.ok("Payout webhook processed", response);
//    }
//}
