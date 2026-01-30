package com.marv.arionwallet.modules.withdrawal.application;

public class FakeAccountNameEnquiryService implements AccountNameEnquiryService{

    @Override
    public String resolve(String bankCode, String accountNumber) {
        String last4 = accountNumber.length() >= 4
                ? accountNumber.substring(accountNumber.length() - 4)
                : accountNumber;
        return "Test User";
    }
}
