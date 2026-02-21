package com.marv.arionwallet.modules.banking.application;

import com.marv.arionwallet.modules.banking.presentation.BankDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankingService {

    private final BankingProvider bankingProvider;

    @Transactional(readOnly = true)
    public List<BankDto> listBanks() {
        return bankingProvider.listBanks();
    }
}
