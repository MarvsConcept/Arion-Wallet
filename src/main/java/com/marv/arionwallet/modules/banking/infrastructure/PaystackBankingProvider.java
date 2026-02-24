package com.marv.arionwallet.modules.banking.infrastructure;

import com.marv.arionwallet.modules.banking.presentation.BankDto;
import com.marv.arionwallet.modules.banking.application.BankingProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PaystackBankingProvider implements BankingProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${paystack.base-url:https://api.paystack.co}")
    private String baseUrl;

    @Value("${paystack.secret-key}")
    private String secretKey;

    @Override
    public NameEnquiryResult resolveAccountName(String bankCode, String accountNumber) {

        if (bankCode == null || bankCode.isBlank()) {
            throw new IllegalArgumentException("Bank code is required");
        }
        if (accountNumber == null || !accountNumber.matches("\\d{10}")) {
            throw new IllegalArgumentException("Account number must be 10 digits");
        }

        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/bank/resolve") //
                .queryParam("account_number", accountNumber)
                .queryParam("bank_code", bankCode)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(secretKey);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Paystack account resolve failed");
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            if (!root.path("status").asBoolean(false)) {
                throw new IllegalStateException(root.path("message").asText("Account resolve failed"));
            }

            JsonNode data = root.path("data");
            String name = data.path("account_name").asText(null);

            if (name == null || name.isBlank()) {
                throw new IllegalStateException("Paystack returned empty account name");
            }

            return new NameEnquiryResult(name, bankCode, accountNumber);

        } catch (Exception e) {
            throw new IllegalStateException("Could not parse Paystack account resolve response", e);
        }
    }

    @Override
    public List<BankDto> listBanks() {

        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/bank") //
                .queryParam("country", "nigeria")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(secretKey);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Paystack list banks failed");
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            if (!root.path("status").asBoolean(false)) {
                throw new IllegalStateException(root.path("message").asText("List banks failed"));
            }

            List<BankDto> banks = new ArrayList<>();
            for (JsonNode b : root.path("data")) {
                String code = b.path("code").asText(null);
                String name = b.path("name").asText(null);

                if (code != null && name != null) {
                    banks.add(new BankDto(code, name));
                }
            }

            return banks;

        } catch (Exception e) {
            throw new IllegalStateException("Could not parse Paystack bank list response", e);
        }
    }
}