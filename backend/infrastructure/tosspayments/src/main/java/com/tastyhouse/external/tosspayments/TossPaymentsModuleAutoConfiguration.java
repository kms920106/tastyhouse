package com.tastyhouse.external.tosspayments;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan("com.tastyhouse.external.tosspayments")
@EnableConfigurationProperties(TossPaymentProperties.class)
public class TossPaymentsModuleAutoConfiguration {
}
