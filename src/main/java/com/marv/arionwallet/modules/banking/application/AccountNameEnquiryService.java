package com.marv.arionwallet.modules.banking.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountNameEnquiryService {

    private final BankingProvider bankingProvider;

    public String resolve(String bankCode, String accountNumber) {

        BankingProvider.NameEnquiryResult result =
                bankingProvider.resolveAccountName(bankCode, accountNumber);

        return result.accountName();
    }
}
