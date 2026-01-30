package com.marv.arionwallet.modules.withdrawal.application;

import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.withdrawal.domain.BankAccount;
import com.marv.arionwallet.modules.withdrawal.domain.BankAccountRepository;
import com.marv.arionwallet.modules.withdrawal.presentation.BankAccountRequestDto;
import com.marv.arionwallet.modules.withdrawal.presentation.BankAccountResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final AccountNameEnquiryService accountNameEnquiryService;

    @Transactional
    public BankAccountResponseDto addBankAccount(User user, BankAccountRequestDto request) {

        String bankCode = request.getBankCode().trim();
        String accountNumber = request.getAccountNumber().trim();

        // return existing if already saved
        Optional<BankAccount> existing =
                bankAccountRepository.findByUserIdAndBankCodeAndAccountNumber(user.getId(), bankCode, accountNumber);

        if (existing.isPresent()) {
            BankAccount account = existing.get();
            return BankAccountResponseDto.builder()
                    .id(account.getId())
                    .bankCode(account.getBankCode())
                    .accountName(account.getAccountName())
                    .accountNumber(account.getAccountNumber())
                    .createdAt(account.getCreatedAt())
                    .build();
        }

        String accountName = accountNameEnquiryService.resolve(bankCode, accountNumber);

        BankAccount savedAccount = bankAccountRepository.save(
                new BankAccount(
                        null,
                        user,
                        bankCode,
                        accountNumber,
                        accountName,
                        null
                ));

        return BankAccountResponseDto.builder()
                .id(savedAccount.getId())
                .bankCode(savedAccount.getBankCode())
                .accountName(savedAccount.getAccountName())
                .accountNumber(savedAccount.getAccountNumber())
                .createdAt(savedAccount.getCreatedAt())
                .build();

    }
}
