package com.marv.arionwallet.core.config;

import com.marv.arionwallet.modules.payments.application.PaymentProvider;
import com.marv.arionwallet.modules.payments.infrastructure.FlutterwavePaymentProvider;
import com.marv.arionwallet.modules.payments.infrastructure.PaystackPaymentProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentProviderConfig {

    @Bean
    public PaymentProvider paymentProvider(
            @Value(("${arionwallet.payments.provider}") String provider,
            PaystackPaymentProvider paystack,
            FlutterwavePaymentProvider flutterwave
            ) {
                return "flutterwave".equalsIgnoreCase(provider) ? flutterwave : paystack;
    }
}
