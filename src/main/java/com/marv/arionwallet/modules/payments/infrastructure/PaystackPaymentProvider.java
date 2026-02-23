package com.marv.arionwallet.modules.payments.infrastructure;

import com.marv.arionwallet.modules.payments.application.PaymentProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class PaystackPaymentProvider implements PaymentProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${paystack.base-url:https://api.paystack.co}")
    private String baseUrl;

    @Value("${paystack.secret-key}")
    private String secretKey;

    @Override
    public InitPaymentResult initializePayment(InitPaymentRequest request) {

        String url = baseUrl + "/transaction/initialize";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(secretKey);

        String jsonBody = """
                {
                  "email": "%s",
                  "amount": %d,
                  "reference": "%s",
                  "currency": "%s"
                }
                """.formatted(
                request.customerEmail(),
                request.amountInKobo(),
                request.reference(),
                request.currency()
        );

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(jsonBody, headers), String.class);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Paystack initialize request failed", ex);
        }

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Paystack initialize failed: non-2xx response");
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());

            boolean ok = root.path("status").asBoolean(false);
            String message = root.path("message").asText("Paystack initialize failed");

            if (!ok) {
                throw new IllegalStateException("Paystack initialize failed: " + message);
            }

            JsonNode data = root.path("data");
            String authorizationUrl = data.path("authorization_url").asText(null);
            String providerRef = data.path("reference").asText(request.reference());

            if (authorizationUrl == null || authorizationUrl.isBlank()) {
                throw new IllegalStateException("Paystack initialize failed: missing authorization_url");
            }

            return new InitPaymentResult(providerRef, authorizationUrl, message);

        } catch (Exception e) {
            throw new IllegalStateException("Paystack initialize failed: could not parse response", e);
        }
    }
}