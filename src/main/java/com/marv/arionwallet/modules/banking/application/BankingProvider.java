package com.marv.arionwallet.modules.banking.application;

public interface BankingProvider {

    NameEnquiryResult resolveAccountName(String bankCode, String accountNumber);

    record NameEnquiryResult(
            String accountName,
            String bankCode,
            String accountNumber
    ) {

    }
}
