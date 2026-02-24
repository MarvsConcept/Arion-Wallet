package com.marv.arionwallet.modules.banking.application;

import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.banking.domain.BankAccount;
import com.marv.arionwallet.modules.banking.domain.BankAccountRepository;
import com.marv.arionwallet.modules.banking.presentation.BankAccountRequestDto;
import com.marv.arionwallet.modules.banking.presentation.BankAccountResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

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
            return toResponse(existing.get());
        }

        String accountName = accountNameEnquiryService.resolve(bankCode, accountNumber);

        boolean firstAccount = !bankAccountRepository.existsByUserId(user.getId());

        BankAccount savedAccount = bankAccountRepository.save(
                BankAccount.builder()
                        .user(user)
                        .bankCode(bankCode)
                        .accountName(accountName)
                        .accountNumber(accountNumber)
                        .isDefault(firstAccount)
                        .build());


        return toResponse(savedAccount);
    }

    @Transactional
    public void setDefaultBankAccount(User user, UUID bankAccountId) {

        BankAccount account = bankAccountRepository.findByIdAndUserId(bankAccountId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Bank account not found"));

        bankAccountRepository.clearDefaultForUser(user.getId());

        account.setDefault(true);
        bankAccountRepository.save(account);
    }


    private BankAccountResponseDto toResponse(BankAccount account) {
        return BankAccountResponseDto.builder()
                .id(account.getId())
                .bankCode(account.getBankCode())
                .accountName(account.getAccountName())
                .accountNumber(account.getAccountNumber())
                .isDefault(account.isDefault())
                .createdAt(account.getCreatedAt())
                .build();
    }
 }
