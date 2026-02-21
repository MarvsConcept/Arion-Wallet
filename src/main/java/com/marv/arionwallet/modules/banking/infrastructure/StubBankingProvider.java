package com.marv.arionwallet.modules.banking.infrastructure;

import com.marv.arionwallet.modules.banking.application.BankingProvider;
import com.marv.arionwallet.modules.banking.presentation.BankDto;
import org.springframework.stereotype.Component;

import java.util.List;

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

    @Override
    public List<BankDto> listBanks() {
        return List.of(

                new BankDto("044", "Access Bank"),
                new BankDto("058", "GTBank"),
                new BankDto("011", "First Bank"),
                new BankDto("057", "Zenith Bank"),
                new BankDto("033", "UBA")
        );
    }
}
