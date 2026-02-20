package com.marv.arionwallet.modules.fraud.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TransferLimit {

    private final long maxBalanceInKobo;
    private final long maxDailyInKobo;
    private final long maxSingleInKobo;
}
