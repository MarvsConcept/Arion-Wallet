package com.marv.arionwallet.modules.banking.application;

import com.marv.arionwallet.modules.banking.presentation.BankDto;

import java.util.List;

public interface BankingProvider {

    NameEnquiryResult resolveAccountName(String bankCode, String accountNumber);

    record NameEnquiryResult(
            String accountName,
            String bankCode,
            String accountNumber
    ) {}

    List<BankDto> listBanks();
}
