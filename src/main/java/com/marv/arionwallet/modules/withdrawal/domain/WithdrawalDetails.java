//package com.marv.arionwallet.modules.withdrawal.domain;
//
//import com.marv.arionwallet.modules.transaction.domain.Transaction;
//import jakarta.persistence.Entity;
//import jakarta.persistence.OneToOne;
//import jakarta.persistence.Table;
//import lombok.AccessLevel;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//@Entity
//@Table(name = "withdrawal_details")
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//public class WithdrawalDetails {
//
//    @OneToOne
//    private Transaction transaction;
//
//    private String bankCode;
//
//    private String accountNumber;
//
//    private String accountName;
//}
