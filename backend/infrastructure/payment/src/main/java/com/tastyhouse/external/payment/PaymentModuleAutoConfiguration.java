package com.tastyhouse.external.payment;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.tastyhouse.external.payment.toss.TossPaymentProperties;

@AutoConfiguration
@ComponentScan("com.tastyhouse.external.payment")
@EnableConfigurationProperties(TossPaymentProperties.class)
public class PaymentModuleAutoConfiguration {
}
