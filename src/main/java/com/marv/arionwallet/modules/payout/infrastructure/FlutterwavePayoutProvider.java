package com.marv.arionwallet.modules.payout.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marv.arionwallet.modules.payout.application.PayoutProvider;
import com.marv.arionwallet.modules.payout.presentation.PayoutStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@Component
@RequiredArgsConstructor
public class FlutterwavePayoutProvider implements PayoutProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${flutterwave.base-url:https://api.flutterwave.com}")
    private String baseUrl;

    @Value("${flutterwave.secret-key}")
    private String secretKey;

    @Override
    public PayoutResult initiatePayout(PayoutRequest request) {

        String url = baseUrl + "/v3/transfers"; // :contentReference[oaicite:5]{index=5}

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(secretKey); // :contentReference[oaicite:6]{index=6}

        String jsonBody = """
        {
          "account_bank": "%s",
          "account_number": "%s",
          "amount": %d,
          "currency": "%s",
          "reference": "%s",
          "narration": "ArionWallet Withdrawal"
        }
        """.formatted(
                request.bankCode(),
                request.accountNumber(),
                request.amountInKobo(),
                request.currency(),
                request.reference()
        );

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(jsonBody, headers), String.class);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Flutterwave transfer initiation failed", ex);
        }

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Flutterwave transfer initiation failed: non-2xx response");
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            String status = root.path("status").asText("");
            String message = root.path("message").asText("Transfer initiation failed");

            if (!"success".equalsIgnoreCase(status)) {
                throw new IllegalStateException("Flutterwave transfer initiation failed: " + message);
            }

            // We use our reference as providerReference (easy reconciliation)
            return new PayoutProvider.PayoutResult(PayoutStatus.PENDING, request.reference(), message);

        } catch (Exception e) {
            throw new IllegalStateException("Flutterwave transfer initiation failed: could not parse response", e);
        }
    }
}