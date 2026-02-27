package com.marv.arionwallet.modules.payout.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marv.arionwallet.modules.banking.domain.BankAccount;
import com.marv.arionwallet.modules.banking.domain.BankAccountRepository;
import com.marv.arionwallet.modules.payout.application.PayoutProvider;
import com.marv.arionwallet.modules.payout.application.PayoutStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    @Transactional
    public PayoutResult initiatePayout(PayoutRequest request) {

        BankAccount bankAccount = bankAccountRepository.findByIdAndUserId(request.bankAccountId(), null /*optional*/)
                .orElseThrow(() -> new IllegalArgumentException("Bank account not found"));

        // Ensure recipient_code exists
        String recipientCode = bankAccount.getProviderRecipientCode();
        if (recipientCode == null || recipientCode.isBlank()) {
            recipientCode = createRecipient(bankAccount, request.currency());
            bankAccount.setProviderRecipientCodeIfAbsent(recipientCode);
            bankAccountRepository.save(bankAccount);
        }

        // Initiate transfer using recipient_code + our reference
        initiateTransfer(recipientCode, request);

        // Paystack transfer is async → webhook is source of truth
        return new PayoutResult(PayoutStatus.PENDING, request.reference(), "Transfer initiated");
    }

    private String createRecipient(BankAccount bankAccount, String currency) {
        // Paystack transfer recipient endpoint :contentReference[oaicite:6]{index=6}

        String url = baseUrl + "/transferrecipient";

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
                bankAccount.getAccountName(),
                bankAccount.getAccountNumber(),
                bankAccount.getBankCode(),
                currency
        );

        ResponseEntity<String> res = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
            throw new IllegalStateException("Paystack create recipient failed");
        }

        try {
            JsonNode root = objectMapper.readTree(res.getBody());
            if (!root.path("status").asBoolean(false)) {
                throw new IllegalStateException(root.path("message").asText("Recipient create failed"));
            }
            return root.path("data").path("recipient_code").asText();
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse Paystack recipient response", e);
        }
    }

    private void initiateTransfer(String recipientCode, PayoutRequest request) {
        // Paystack initiate transfer endpoint :contentReference[oaicite:7]{index=7}

        String url = baseUrl + "/transfer";

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
