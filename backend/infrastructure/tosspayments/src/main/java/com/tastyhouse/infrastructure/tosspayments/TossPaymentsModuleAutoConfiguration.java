package com.tastyhouse.infrastructure.tosspayments;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan("com.tastyhouse.infrastructure.tosspayments")
@EnableConfigurationProperties(TossPaymentProperties.class)
public class TossPaymentsModuleAutoConfiguration {
}
