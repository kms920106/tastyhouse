package com.tastyhouse.batch.payment.adapter.in.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tastyhouse.application.payment.port.in.ReconcileUnconfirmedPaymentsUseCase;

@Component
class PaymentReconcileScheduler {

    private static final Logger log = LoggerFactory.getLogger(PaymentReconcileScheduler.class);

    private final ReconcileUnconfirmedPaymentsUseCase reconcileUnconfirmedPaymentsUseCase;

    public PaymentReconcileScheduler(
        ReconcileUnconfirmedPaymentsUseCase reconcileUnconfirmedPaymentsUseCase
    ) {
        this.reconcileUnconfirmedPaymentsUseCase = reconcileUnconfirmedPaymentsUseCase;
    }

    @Scheduled(cron = "${payment.reconcile.cron:0 */10 * * * *}")
    public void reconcileUnconfirmedPayments() {
        log.info("=== PG 미확정 결제 대사 스케줄러 시작 ===");

        try {
            reconcileUnconfirmedPaymentsUseCase.reconcileUnconfirmedPayments();
            log.info("=== PG 미확정 결제 대사 스케줄러 완료 ===");
        } catch (Exception e) {
            log.error("PG 미확정 결제 대사 중 오류 발생", e);
        }
    }
}
