package com.marv.arionwallet.modules.user.application;

import com.marv.arionwallet.modules.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountNumberGenerator {

    private static final int ACCOUNT_NUMBER_LENGTH = 10;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_RETRIES = 10;

    private static final String PREFIX = "29";
    private static final int RANDOM_DIGITS = 8;

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public String generateUniqueAccountNumber() {

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            String accountNumber = generateCandidate();

            if (!userRepository.existsByAccountNumber(accountNumber)) {
                return accountNumber;
            }
        }
        throw new IllegalStateException(
                "Failed to generate a unique account number after " + MAX_RETRIES + " attempts"
        );
    }

    private String generateCandidate() {
        long bound = (long) Math.pow(10, RANDOM_DIGITS); // 100_000_000
        long randomPart = RANDOM.nextLong(bound);
        return PREFIX + String.format("%0" + RANDOM_DIGITS + "d", randomPart);
    }
}
