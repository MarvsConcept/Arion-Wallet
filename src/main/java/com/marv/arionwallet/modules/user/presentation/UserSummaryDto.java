package com.marv.arionwallet.modules.user.presentation;

import com.marv.arionwallet.modules.user.domain.KycLevel;
import com.marv.arionwallet.modules.user.domain.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class UserSummaryDto {

    private UUID userId;
    private String firstName;
    private String lastName;
    private String accountNumber;
    private String email;
    private String phone;
    private UserStatus status;
    private KycLevel kycLevel;
    private String walletCurrency;
    private Long walletBalance;
}
