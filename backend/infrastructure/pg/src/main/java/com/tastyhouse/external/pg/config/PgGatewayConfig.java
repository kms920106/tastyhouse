package com.tastyhouse.external.pg.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.payment.port.PgProviderGateway;
import com.tastyhouse.domain.payment.service.PgPaymentGatewayRouter;

@Configuration(proxyBeanMethods = false)
public class PgGatewayConfig {

    @Bean
    public PgPaymentGatewayRouter pgPaymentGatewayRouter(List<PgProviderGateway> gateways) {
        return new PgPaymentGatewayRouter(gateways);
    }
}
