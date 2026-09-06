package com.tastyhouse.infrastructure.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.order.service.OrderTransitionService;
import com.tastyhouse.domain.payment.repository.PaymentRefundRepository;
import com.tastyhouse.domain.payment.repository.PaymentRepository;
import com.tastyhouse.domain.payment.repository.TossPaymentRecordRepository;
import com.tastyhouse.domain.payment.service.PaymentCancellationService;
import com.tastyhouse.domain.payment.service.PaymentConfirmationService;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

@Configuration(proxyBeanMethods = false)
public class PaymentDomainConfig {
    @Bean
    public PaymentConfirmationService paymentConfirmationService(
        PaymentRepository paymentRepository,
        TossPaymentRecordRepository tossPaymentRecordRepository,
        OrderTransitionService orderTransitionService,
        DomainEventPublisher domainEventPublisher
    ) {
        return new PaymentConfirmationService(
            paymentRepository,
            tossPaymentRecordRepository,
            orderTransitionService,
            domainEventPublisher
        );
    }

    @Bean
    public PaymentCancellationService paymentCancellationService(
        PaymentRepository paymentRepository,
        PaymentRefundRepository paymentRefundRepository,
        OrderTransitionService orderTransitionService,
        DomainEventPublisher domainEventPublisher
    ) {
        return new PaymentCancellationService(
            paymentRepository,
            paymentRefundRepository,
            orderTransitionService,
            domainEventPublisher
        );
    }
}
