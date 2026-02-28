package com.marv.arionwallet.modules.payments.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marv.arionwallet.modules.payments.application.PaymentProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class FlutterwavePaymentProvider implements PaymentProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${flutterwave.base-url:https://api.flutterwave.com}")
    private String baseUrl;

    @Value("${flutterwave.secret-key}")
    private String secretKey;

    @Value("${flutterwave.redirect-url}")
    private String redirectUrl;

    @Override
    public InitPaymentResult initializePayment(InitPaymentRequest request) {

        String url = baseUrl + "/v3/payments"; // :contentReference[oaicite:2]{index=2}

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(secretKey); // :contentReference[oaicite:3]{index=3}

        // Flutterwave uses tx_ref as your reference
        String jsonBody = """
        {
          "tx_ref": "%s",
          "amount": %d,
          "currency": "%s",
          "redirect_url": "%s",
          "customer": {
            "email": "%s"
          },
          "customizations": {
            "title": "ArionWallet Funding",
            "description": "Wallet top-up"
          }
        }
        """.formatted(
                request.reference(),
                request.amountInKobo(),
                request.currency(),
                redirectUrl,
                request.customerEmail()
        );

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(jsonBody, headers), String.class);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Flutterwave initialize request failed", ex);
        }

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Flutterwave initialize failed: non-2xx response");
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            String status = root.path("status").asText("");
            String message = root.path("message").asText("Flutterwave initialize failed");

            if (!"success".equalsIgnoreCase(status)) {
                throw new IllegalStateException("Flutterwave initialize failed: " + message);
            }

            // Flutterwave returns link in data.link
            JsonNode data = root.path("data");
            String link = data.path("link").asText(null);
            if (link == null || link.isBlank()) {
                throw new IllegalStateException("Flutterwave initialize failed: missing link");
            }

            // providerReference = your tx_ref
            return new InitPaymentResult(request.reference(), link, message);

        } catch (Exception e) {
            throw new IllegalStateException("Flutterwave initialize failed: could not parse response", e);
        }
    }
}