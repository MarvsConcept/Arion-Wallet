package com.marv.arionwallet.modules.payments.domain;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class PaystacktWebhookSignatureVerifier implements WebhookSignatureVerifier{

    private final String secretKey;

    public PaystacktWebhookSignatureVerifier(
            @Value("${paystack.secret-key}") String secretKey
    ) {
        this.secretKey = secretKey;
    }

    @Override
    public void verifyOrThrow(HttpServletRequest request, String rawBody) {

        String signature = request.getHeader("X-Paystack-Signature");
        if (signature == null || signature.isBlank()) {
            throw new SecurityException("Missing Paystack signature");
        }

        String computed = hmacSha512Hex(rawBody, secretKey);

        if (!MessageDigest.isEqual(
                computed.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8)
        )) {
            throw new SecurityException("Invalid Paystack signature");
        }
    }

    private static String hmacSha512Hex(String body, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] out = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(out.length * 2);
            for (byte b : out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Could not compute HMAC SHA512", e);
        }
    }
}
