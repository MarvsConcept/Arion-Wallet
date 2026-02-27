package com.marv.arionwallet.modules.payout.infrastructure;

import com.marv.arionwallet.modules.banking.domain.BankAccount;
import com.marv.arionwallet.modules.banking.domain.BankAccountRepository;
import com.marv.arionwallet.modules.payout.application.PayoutProvider;
import com.marv.arionwallet.modules.payout.presentation.PayoutStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class PaystackPayoutProvider implements PayoutProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final BankAccountRepository bankAccountRepository;

    @Value("${paystack.base-url:https://api.paystack.co}")
    private String baseUrl;

    @Value("${paystack.secret-key}")
    private String secretKey;

    @Override
    public PayoutResult initiatePayout(PayoutRequest request) {

        BankAccount bankAccount = bankAccountRepository.findById(request.bankAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Bank account not found"));

        // 1) Ensure recipient exists
        String recipientCode = bankAccount.getProviderRecipientCode();
        if (recipientCode == null || recipientCode.isBlank()) {
            recipientCode = createRecipient(request);
            bankAccount.setProviderRecipientCodeIfAbsent(recipientCode);
            bankAccountRepository.save(bankAccount);
        }

        // 2) Initiate transfer
        initiateTransfer(recipientCode, request);

        return new PayoutResult(PayoutStatus.PENDING, request.reference(), "Transfer initiated");
    }

    private String createRecipient(PayoutRequest request) {

        String url = baseUrl + "/transferrecipient"; // Paystack transfer recipient

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(secretKey);

        String body = """
        {
          "type": "nuban",
          "name": "%s",
          "account_number": "%s",
          "bank_code": "%s",
          "currency": "%s"
        }
        """.formatted(
                request.accountName(),
                request.accountNumber(),
                request.bankCode(),
                request.currency()
        );

        ResponseEntity<String> res = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);

        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
            throw new IllegalStateException("Paystack recipient creation failed");
        }

        try {
            JsonNode root = objectMapper.readTree(res.getBody());
            if (!root.path("status").asBoolean(false)) {
                throw new IllegalStateException(root.path("message").asText("Recipient creation failed"));
            }
            String code = root.path("data").path("recipient_code").asText(null);
            if (code == null || code.isBlank()) {
                throw new IllegalStateException("Recipient creation returned empty recipient_code");
            }
            return code;
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse Paystack recipient response", e);
        }
    }

    private void initiateTransfer(String recipientCode, PayoutRequest request) {

        String url = baseUrl + "/transfer"; // Paystack initiate transfer

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(secretKey);

        String body = """
        {
          "source": "balance",
          "amount": %d,
          "recipient": "%s",
          "reference": "%s",
          "currency": "%s",
          "reason": "ArionWallet Withdrawal"
        }
        """.formatted(
                request.amountInKobo(),
                recipientCode,
                request.reference(),
                request.currency()
        );

        ResponseEntity<String> res = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);

        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
            throw new IllegalStateException("Paystack transfer initiation failed");
        }

        try {
            JsonNode root = objectMapper.readTree(res.getBody());
            if (!root.path("status").asBoolean(false)) {
                throw new IllegalStateException(root.path("message").asText("Transfer initiation failed"));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse Paystack transfer response", e);
        }
    }
}