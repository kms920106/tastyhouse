package com.tastyhouse.application.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.order.service.OrderTransitionService;
import com.tastyhouse.application.payment.port.out.write.PaymentRefundRepository;
import com.tastyhouse.application.payment.port.out.write.PaymentRepository;
import com.tastyhouse.application.payment.port.out.write.TossPaymentRecordRepository;
import com.tastyhouse.application.payment.service.PaymentCancellationService;
import com.tastyhouse.application.payment.service.PaymentConfirmationService;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class PaymentServiceConfig {
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
