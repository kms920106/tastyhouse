package com.tastyhouse.application.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.order.service.OrderTransitionService;
import com.tastyhouse.application.payment.port.out.write.PaymentPersistencePort;
import com.tastyhouse.application.payment.port.out.write.PaymentRefundPersistencePort;
import com.tastyhouse.application.payment.port.out.write.TossPaymentRecordPersistencePort;
import com.tastyhouse.application.payment.service.PaymentCancellationService;
import com.tastyhouse.application.payment.service.PaymentConfirmationService;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class PaymentServiceConfig {

    @Bean
    public PaymentConfirmationService paymentConfirmationService(
        PaymentPersistencePort paymentPersistencePort,
        TossPaymentRecordPersistencePort tossPaymentRecordPersistencePort,
        OrderTransitionService orderTransitionService,
        DomainEventPublisher domainEventPublisher
    ) {
        return new PaymentConfirmationService(
            paymentPersistencePort,
            tossPaymentRecordPersistencePort,
            orderTransitionService,
            domainEventPublisher
        );
    }

    @Bean
    public PaymentCancellationService paymentCancellationService(
        PaymentPersistencePort paymentPersistencePort,
        PaymentRefundPersistencePort paymentRefundPersistencePort,
        OrderTransitionService orderTransitionService,
        DomainEventPublisher domainEventPublisher
    ) {
        return new PaymentCancellationService(
            paymentPersistencePort,
            paymentRefundPersistencePort,
            orderTransitionService,
            domainEventPublisher
        );
    }
}
