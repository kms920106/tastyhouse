package com.tastyhouse.external.payment.toss;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "payment.toss")
public record TossPaymentProperties(
    String secretKey,
    @DefaultValue("https://api.tosspayments.com") String baseUrl,
    @DefaultValue("/v1/payments/confirm") String confirmPath,
    @DefaultValue("/v1/payments/{paymentKey}/cancel") String cancelPath
) {
}
