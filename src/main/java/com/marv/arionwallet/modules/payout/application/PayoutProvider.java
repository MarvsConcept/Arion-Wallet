package com.marv.arionwallet.modules.payout.application;

public interface PayoutProvider {

    PayoutResult initiatePayout(PayoutRequest request);

    record PayoutRequest(
            String reference,
            String bankCode,
            String accountNumber,
            long amountInKobo,
            String currency
    ) {
    }

    record PayoutResult(
            PayoutStatus status,
            String providerReference,
            String message
    ) {
    }
}
