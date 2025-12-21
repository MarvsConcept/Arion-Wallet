package com.marv.arionwallet.modules.risk.domain.TransferLimit;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TransferLimit {

    private final long maxBalanceInKobo;
    private final long maxDailyInKobo;
    private final long maxSingleInKobo;
}
