package com.marv.arionwallet.modules.payments.infrastructure;

import com.marv.arionwallet.modules.payments.application.PaymentProvider;
import org.springframework.stereotype.Component;

@Component
public class StubPaymentProvider implements PaymentProvider {


    @Override
    public InitPaymentResult initializePayment(InitPaymentRequest request) {

        if (request.amountInKobo() <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }

        String providerRef = "STUB-FUND-" + request.reference();
        String paymentUrl = "https://stub-payments.local/pay" + providerRef;

        return new InitPaymentResult(providerRef, paymentUrl, "Initialized");
    }
}
