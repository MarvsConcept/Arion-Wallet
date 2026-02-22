package com.marv.arionwallet.modules.payments.domain;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class NoopWebhookSignatureVerifier implements WebhookSignatureVerifier {

    @Override
    public void verifyOrThrow(HttpServletRequest request, String rawBody) {

    }
}
