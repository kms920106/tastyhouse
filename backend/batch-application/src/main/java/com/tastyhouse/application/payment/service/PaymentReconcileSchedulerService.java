package com.tastyhouse.application.payment.service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.application.payment.port.in.ReconcileUnconfirmedPaymentsUseCase;
import com.tastyhouse.application.payment.port.out.PgPaymentGatewayPort;
import com.tastyhouse.application.payment.port.out.PgPaymentLookupResult;
import com.tastyhouse.application.payment.port.out.write.PaymentLoadPort;

@Service
class PaymentReconcileSchedulerService implements ReconcileUnconfirmedPaymentsUseCase {

    private static final Logger log = LoggerFactory.getLogger(PaymentReconcileSchedulerService.class);

    private static final int LOOKBACK_DAYS = 7;

    private static final int SETTLE_MINUTES = 10;

    private static final int BATCH_LIMIT = 100;

    private static final int RUN_INTERVAL_MINUTES = 10;

    private static final int MAX_CONSECUTIVE_CLAIM_FAILURES = 3;

    private final PaymentLoadPort paymentLoadPort;
    private final PgPaymentGatewayPort pgPaymentGatewayPort;
    private final PaymentReconcileExecutor paymentReconcileExecutor;

    public PaymentReconcileSchedulerService(
        PaymentLoadPort paymentLoadPort,
        PgPaymentGatewayPort pgPaymentGatewayPort,
        PaymentReconcileExecutor paymentReconcileExecutor
    ) {
        this.paymentLoadPort = paymentLoadPort;
        this.pgPaymentGatewayPort = pgPaymentGatewayPort;
        this.paymentReconcileExecutor = paymentReconcileExecutor;
    }

    @Override
    public void reconcileUnconfirmedPayments() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusDays(LOOKBACK_DAYS);
        LocalDateTime to = now.minusMinutes(SETTLE_MINUTES);

        List<Payment> payments = paymentLoadPort.findAllPgAttemptedReconcileDueByStatusCreatedBetween(
            PaymentStatus.PENDING, from, to, now, BATCH_LIMIT
        );
        if (payments.isEmpty()) {
            log.debug("PG 미확정 결제 대사 대상 없음: from={}, to={}", from, to);
            return;
        }

        Map<PgReconcileOutcome, Integer> approved = new EnumMap<>(PgReconcileOutcome.class);
        int notApproved = 0;
        int skipped = 0;
        int errors = 0;
        int claimErrors = 0;
        int claimedElsewhere = 0;
        int manualRequired = 0;
        int consecutiveClaimFailures = 0;

        for (int index = 0; index < payments.size(); index++) {
            Payment payment = payments.get(index);

            Optional<PaymentReconcileClaim> claimed;
            try {
                claimed = paymentReconcileExecutor.claimNextAttemptInNewTx(payment.getPaymentId(), now);
                consecutiveClaimFailures = 0;
            } catch (Exception e) {
                claimErrors++;
                consecutiveClaimFailures++;
                log.error("PG 미확정 결제 대사 차례 선점 실패: pgOrderId={}, paymentId={}",
                    payment.getPgOrderId(), payment.getId(), e);
                if (consecutiveClaimFailures >= MAX_CONSECUTIVE_CLAIM_FAILURES) {
                    log.error("PG 미확정 결제 대사 차례 선점이 {} 건 연속 실패해 이번 주기를 중단: 남은 대상 {} 건",
                        consecutiveClaimFailures, payments.size() - index - 1);
                    break;
                }
                continue;
            }
            if (claimed.isEmpty()) {
                claimedElsewhere++;
                log.debug("PG 미확정 결제 대사 차례를 다른 실행이 가져감: pgOrderId={}, paymentId={}",
                    payment.getPgOrderId(), payment.getId());
                continue;
            }
            PaymentReconcileClaim claim = claimed.get();

            boolean resolved = false;
            PgPaymentLookupResult lookup = null;
            try {
                lookup = pgPaymentGatewayPort.lookupPayment(payment.getPgProvider().name(), payment.getPgOrderId());
            } catch (Exception e) {
                errors++;
                log.error("PG 미확정 결제 대사 조회 실패: pgOrderId={}, paymentId={}",
                    payment.getPgOrderId(), payment.getId(), e);
            }

            if (lookup != null) {
                switch (lookup.status()) {
                    case APPROVED -> {
                        try {
                            PgReconcileResult result = paymentReconcileExecutor.reconcileApprovedInNewTx(
                                payment.getPgOrderId(), payment.getPgProvider(), lookup.result()
                            );
                            approved.merge(result.outcome(), 1, Integer::sum);
                            resolved = result.outcome() != PgReconcileOutcome.AMOUNT_MISMATCH;
                        } catch (Exception e) {
                            errors++;
                            log.error(
                                "{} PG 조회로 승인 확인 후 DB 반영 실패 — pgProvider={}, pgOrderId={}, paymentId={}, paymentKey={}, amount={}",
                                PaymentLogMarkers.PG_DB_MISMATCH, payment.getPgProvider(), payment.getPgOrderId(), payment.getId(),
                                lookup.result().paymentKey(), lookup.result().totalAmount(), e
                            );
                        }
                    }
                    case NOT_APPROVED -> {
                        try {
                            paymentReconcileExecutor.failUnapprovedInNewTx(payment.getPgOrderId(), lookup.result());
                            notApproved++;
                            resolved = true;
                        } catch (Exception e) {
                            errors++;
                            log.error("PG 미확정 결제 대사 실패 반영 실패: pgOrderId={}, paymentId={}",
                                payment.getPgOrderId(), payment.getId(), e);
                        }
                    }
                    case UNKNOWN -> {
                        skipped++;
                        log.warn(
                            "{} PG 미확정 결제 대사 건너뜀(PG 조회로도 승인 여부를 확정하지 못함) — pgOrderId={}, paymentId={}, createdAt={}, attempt={}, errorCode={}",
                            PaymentLogMarkers.PG_CONFIRM_UNKNOWN, payment.getPgOrderId(), payment.getId(), payment.getCreatedAt(),
                            claim.attemptCount(), lookup.result().errorCode()
                        );
                    }
                }
            }

            if (!resolved && isLastAttemptInWindow(payment, claim)) {
                manualRequired++;
                log.error(
                    "{} PG 미확정 결제가 마지막 대사 시도에서도 확정되지 않아 대사 대상에서 빠짐(수동 대사 필요) — pgProvider={}, pgOrderId={}, paymentId={}, createdAt={}, attempt={}",
                    PaymentLogMarkers.PG_RECONCILE_MANUAL_REQUIRED, payment.getPgProvider(), payment.getPgOrderId(), payment.getId(),
                    payment.getCreatedAt(), claim.attemptCount()
                );
            }
        }

        log.info("PG 미확정 결제 대사 완료: from={}, to={}, 대상 {} 건, 승인 반영 {}, 미승인 실패 처리 {} 건, 건너뜀 {} 건, 오류 {} 건, 차례 선점 실패 {} 건, 다른 실행이 처리 {} 건, 수동 대사 필요 {} 건",
            from, to, payments.size(), approved, notApproved, skipped, errors, claimErrors, claimedElsewhere, manualRequired);
    }

    private static boolean isLastAttemptInWindow(Payment payment, PaymentReconcileClaim claim) {
        LocalDateTime windowEnd = payment.getCreatedAt().plusDays(LOOKBACK_DAYS);
        return claim.nextAttemptAt().plusMinutes(RUN_INTERVAL_MINUTES).isAfter(windowEnd);
    }
}
