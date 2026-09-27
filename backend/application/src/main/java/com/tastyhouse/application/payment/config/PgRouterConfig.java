package com.tastyhouse.application.payment.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.payment.port.out.PgProviderGateway;
import com.tastyhouse.application.payment.service.PgPaymentGatewayRouter;
import com.tastyhouse.application.shared.marker.WebApp;

@Configuration(proxyBeanMethods = false)
@WebApp
public class PgRouterConfig {

    @Bean
    public PgPaymentGatewayRouter pgPaymentGatewayRouter(List<PgProviderGateway> gateways) {
        return new PgPaymentGatewayRouter(gateways);
    }
}
