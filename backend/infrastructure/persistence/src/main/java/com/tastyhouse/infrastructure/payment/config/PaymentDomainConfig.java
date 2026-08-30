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

/**
 * payment 컨텍스트의 도메인 서비스(POJO) 빈 등록 설정.
 *
 * <p>도메인 서비스는 {@code @Service} 없는 순수 POJO라 Spring이 스캔할 수 없으므로,
 * 이 컨텍스트에 새 POJO 도메인 서비스를 추가하면 여기에 {@code @Bean}을 추가한다.
 */
@Configuration(proxyBeanMethods = false)
public class PaymentDomainConfig {

    /**
     * 결제 개시·승인 — 결제 상태 전이와 주문 확정 전이를 한 트랜잭션에서 원자로 묶는 오케스트레이션.
     * 승인 경로(PG 콜백·토스 승인·현장결제 완료)가 여러 개여도 "결제 완료와 주문 확정은 항상 함께"라는
     * 규칙이 이 한 곳에만 존재한다. 주문 전이는 {@link OrderTransitionService}에 위임한다.
     */
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

    /**
     * 결제 취소·환불 — 결제 취소 전이와 주문 취소 전이를 한 트랜잭션에서 원자로 묶고, PG 취소 요청과
     * 포인트 원복(이벤트 경유)까지 함께 조율하는 오케스트레이션.
     */
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
