package com.marv.arionwallet.modules.transaction.domain;

public enum TransactionType {
    FUNDING, // wallet topup
    TRANSFER, // user to user
    WITHDRAWAL, // wallet to bank
    ADJUSTMENT // Admin correction(later)
}
