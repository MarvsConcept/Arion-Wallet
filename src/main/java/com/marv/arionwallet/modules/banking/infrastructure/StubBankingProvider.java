package com.marv.arionwallet.modules.banking.infrastructure;

import com.marv.arionwallet.modules.banking.application.BankingProvider;
import org.springframework.stereotype.Component;

@Component
public class StubBankingProvider implements BankingProvider {

    @Override
    public NameEnquiryResult resolveAccountName(String bankCode, String accountNumber) {

        // basic validation
        if (bankCode == null || bankCode.isBlank()) {
            throw new IllegalArgumentException("Bank code is required");
        }
        if (accountNumber == null || !accountNumber.matches("\\d{10}")) {
            throw new IllegalArgumentException("Account number must be 10 digits");
        }

        // deterministic fake name(so tests are stable)
        String last4 = accountNumber.substring(6);
        String name = "Test User " + last4;

        return new NameEnquiryResult(name, bankCode, accountNumber);
    }
}
