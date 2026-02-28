package com.marv.arionwallet.modules.payments.domain;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class FlutterwaveWebhookSignatureVerifier {

    @Value("${flutterwave.webhook.secret-hash}")
    private String secretHash;

    public void verifyOrThrow(HttpServletRequest request, String rawBody) {

        // Preferred simple check (verif-hash) :contentReference[oaicite:8]{index=8}
        String verifHash = request.getHeader("verif-hash");
        if (verifHash != null && !verifHash.isBlank()) {
            if (!verifHash.equals(secretHash)) {
                throw new IllegalArgumentException("Invalid Flutterwave webhook verif-hash");
            }
            return;
        }

        // Some docs show flutterwave-signature HMAC-SHA256 :contentReference[oaicite:9]{index=9}
        String signature = request.getHeader("flutterwave-signature");
        if (signature == null || signature.isBlank()) {
            throw new IllegalArgumentException("Missing Flutterwave webhook signature");
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretHash.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String computed = Base64.getEncoder().encodeToString(digest);

            if (!computed.equals(signature)) {
                throw new IllegalArgumentException("Invalid Flutterwave webhook signature");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not verify Flutterwave webhook signature", e);
        }
    }
}