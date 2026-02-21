package com.marv.arionwallet.modules.payments.application;

import java.util.UUID;

public interface PaymentProvider {

    InitPaymentResult initializePayment(InitPaymentRequest request);

    record InitPaymentRequest(
            String reference,
            UUID userId,
            long amountInKobo,
            String currency,
            String customerEmail
    ) { }

    record InitPaymentResult(
            String providerReference,
            String paymentUrl,
            String message
    ) { }
}
