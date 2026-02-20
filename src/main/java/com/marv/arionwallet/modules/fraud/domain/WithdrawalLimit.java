package com.marv.arionwallet.modules.fraud.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WithdrawalLimit {

    private final long singleWithdrawalInKobo;
    private final long dailyWithdrawalInKobo;
}
