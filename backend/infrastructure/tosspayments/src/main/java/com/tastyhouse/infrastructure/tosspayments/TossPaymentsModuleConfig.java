package com.tastyhouse.infrastructure.tosspayments;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(TossPaymentProperties.class)
public class TossPaymentsModuleConfig {
}
