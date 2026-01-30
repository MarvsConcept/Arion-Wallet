package com.marv.arionwallet.modules.withdrawal.application;

public interface AccountNameEnquiryService {
    String resolve(String bankCode, String accountNumber);
}
