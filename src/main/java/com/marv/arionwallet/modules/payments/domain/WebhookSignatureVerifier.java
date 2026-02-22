package com.marv.arionwallet.modules.payments.domain;

import jakarta.servlet.http.HttpServletRequest;

public interface WebhookSignatureVerifier {

    void verifyOrThrow(HttpServletRequest request, String rawBody);
}
