package com.marv.arionwallet.modules.payout.application;

import java.util.UUID;

public interface PayoutProvider {

    PayoutResult initiatePayout(PayoutRequest request);

    record PayoutRequest(
            UUID bankAccountId,
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
