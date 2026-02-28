package com.marv.arionwallet.core.config;

import com.marv.arionwallet.modules.payout.application.PayoutProvider;
import com.marv.arionwallet.modules.payout.infrastructure.PaystackPayoutProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayoutProviderConfig {

    @Bean
    public PayoutProvider payoutProvider(
            @Value("${arionwallet.payouts.provider}") String provider,
            PaystackPayoutProvider paystack,
            FlutterwavePayoutProvider flutterwave
    ) {
        return "flutterwave".equalsIgnoreCase(provider) ? flutterwave : paystack;
    }
}