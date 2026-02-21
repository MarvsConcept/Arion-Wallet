package com.marv.arionwallet.modules.payout.infrastructure;

import com.marv.arionwallet.modules.payout.application.PayoutProvider;
import com.marv.arionwallet.modules.payout.application.PayoutStatus;
import org.springframework.stereotype.Component;

@Component
public class StubPayoutProvider implements PayoutProvider {

    @Override
    public PayoutResult initiatePayout(PayoutRequest request) {

        // deterministic behavior
        if (request.amountInKobo() <= 0) {
            return new PayoutResult(PayoutStatus.FAILED, null, "Invalid amount");
        }

        // example rule
        // <= ₦100,000 succeeds, > ₦100,000 stays pending (simulate provider delays)
        if (request.amountInKobo() <= 100_000 * 100L) {
            return new PayoutResult(
                    PayoutStatus.SUCCESS,
                    "STUB-PAYOUT-" + request.reference(),
                    "Processed"
            );
        }
        return new PayoutResult(
                PayoutStatus.PENDING,
                "STUB-PAYOUT-" + request.reference(),
                "Queued"
        );
    }
}
