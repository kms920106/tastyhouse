package com.tastyhouse.application.payment.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.payment.model.PaymentCancelCode;
import com.tastyhouse.domain.payment.model.PaymentCancellationTarget;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.application.payment.port.in.PaymentCancelCommand;
import com.tastyhouse.application.payment.port.in.PaymentCancelUseCase;
import com.tastyhouse.application.payment.port.out.PaymentCancelResult;
import com.tastyhouse.application.payment.port.out.PgCancelFailure;
import com.tastyhouse.application.payment.port.out.PgCancelLookupResult;
import com.tastyhouse.application.payment.port.out.PgCancelLookupStatus;
import com.tastyhouse.application.payment.port.out.PgCancelResult;
import com.tastyhouse.application.payment.port.out.PgPaymentGatewayPort;

@Service
class PaymentCancelService implements PaymentCancelUseCase {

    private static final Logger log = LoggerFactory.getLogger(PaymentCancelService.class);

    private final PaymentCancellationExecutor paymentCancellationExecutor;
    private final PgPaymentGatewayPort pgPaymentGateway;

    public PaymentCancelService(
        PaymentCancellationExecutor paymentCancellationExecutor,
        PgPaymentGatewayPort pgPaymentGateway
    ) {
        this.paymentCancellationExecutor = paymentCancellationExecutor;
        this.pgPaymentGateway = pgPaymentGateway;
    }

    @Override
    public PaymentCancelResult cancelPayment(PaymentCancelCommand command) {
        PaymentCancelCode cancelCode = doCancelPayment(command.memberId(), command.paymentId(), command.cancelReason());
        return new PaymentCancelResult(cancelCode.name(), cancelCode.getMessage());
    }

    private PaymentCancelCode doCancelPayment(Long memberId, Long id, String cancelReason) {
        MemberId memberIdVo = MemberId.of(memberId);
        PaymentId paymentId = PaymentId.of(id);

        PaymentCancellationTarget target = paymentCancellationExecutor.prepareInNewTx(memberIdVo, paymentId);
        if (target.isRejected()) {
            log.error("결제 취소 실패 — paymentId={}, cancelCode={}", id, target.rejectCode());
            return target.rejectCode();
        }

        String pgProvider = target.pgProvider() == null ? null : target.pgProvider().name();
        boolean pgCancelAttempted = target.pgCancelRequired() && pgPaymentGateway.supports(pgProvider);
        boolean recovered = false;
        if (pgCancelAttempted) {
            Optional<PgCancelResult> cancelResult = requestPgCancel(pgProvider, target.pgTid(), cancelReason);
            if (cancelResult.isEmpty()) {
                log.error("결제 취소 실패 — paymentId={}, cancelCode={}", id, PaymentCancelCode.CANCEL_FAILED);
                return PaymentCancelCode.CANCEL_FAILED;
            }
            if (!cancelResult.get().success()) {
                if (!resolvePgCancelFailure(id, pgProvider, target, cancelResult.get())) {
                    log.error("결제 취소 실패 — paymentId={}, cancelCode={}", id, PaymentCancelCode.CANCEL_FAILED);
                    return PaymentCancelCode.CANCEL_FAILED;
                }
                recovered = true;
            }
        }

        PaymentCancelCode cancelCode;
        try {
            cancelCode = paymentCancellationExecutor.applyInNewTx(
                memberIdVo, paymentId, cancelReason, target.pgCancelRequired()
            );
        } catch (RuntimeException e) {
            if (pgCancelAttempted) {
                log.error(
                    "{} {} 후 DB 반영 실패 — pgProvider={}, paymentId={}, pgTid={}",
                    PaymentLogMarkers.PG_DB_MISMATCH, pgCancelConfirmation(recovered),
                    target.pgProvider(), id, target.pgTid(), e
                );
            }
            throw e;
        }

        if (cancelCode != PaymentCancelCode.SUCCESS) {
            if (pgCancelAttempted && isConsistentlyCancelled(paymentId, cancelCode)) {
                log.warn("결제 취소 — 다른 요청이 이미 취소 반영 — paymentId={}, cancelCode={}", id, cancelCode);
                return cancelCode;
            }
            if (pgCancelAttempted) {
                log.error(
                    "{} {} 후 재판정 거절 — pgProvider={}, paymentId={}, pgTid={}, cancelCode={}",
                    PaymentLogMarkers.PG_DB_MISMATCH, pgCancelConfirmation(recovered),
                    target.pgProvider(), id, target.pgTid(), cancelCode
                );
            }
            log.error("결제 취소 실패 — paymentId={}, cancelCode={}", id, cancelCode);
        }
        return cancelCode;
    }

    private Optional<PgCancelResult> requestPgCancel(String pgProvider, String pgTid, String cancelReason) {
        try {
            PgCancelResult cancelResult = pgPaymentGateway.cancelPayment(pgProvider, pgTid, cancelReason);
            if (!cancelResult.success() && cancelResult.failure() == PgCancelFailure.REJECTED) {
                log.error("PG 취소 거절 — pgProvider={}, pgTid={}, errorCode={}, errorMessage={}",
                    pgProvider, pgTid, cancelResult.errorCode(), cancelResult.errorMessage());
            } else if (!cancelResult.success()) {
                log.warn("PG 취소 실패 — pgProvider={}, pgTid={}, failure={}, errorCode={}, errorMessage={}",
                    pgProvider, pgTid, cancelResult.failure(), cancelResult.errorCode(), cancelResult.errorMessage());
            }
            return Optional.of(cancelResult);
        } catch (Exception e) {
            log.error("PG 취소 요청 예외 — pgProvider={}, pgTid={}", pgProvider, pgTid, e);
            return Optional.empty();
        }
    }

    private boolean resolvePgCancelFailure(
        Long id,
        String pgProvider,
        PaymentCancellationTarget target,
        PgCancelResult cancelResult
    ) {
        if (cancelResult.failure() == null) {
            return false;
        }
        return switch (cancelResult.failure()) {
            case NOT_FOUND -> {
                log.error(
                    "{} PG에 결제 없음 — pgProvider={}, paymentId={}, pgTid={}, pgOrderId={}, errorCode={}",
                    PaymentLogMarkers.PG_CANCEL_NOT_FOUND, pgProvider, id, target.pgTid(), target.pgOrderId(),
                    cancelResult.errorCode()
                );
                yield false;
            }
            case REJECTED -> false;
            case ALREADY_CANCELED, UNKNOWN -> lookupCanceled(id, pgProvider, target, cancelResult);
        };
    }

    private boolean lookupCanceled(
        Long id,
        String pgProvider,
        PaymentCancellationTarget target,
        PgCancelResult cancelResult
    ) {
        if (target.pgOrderId() == null) {
            log.error("{} PG 취소 확인 조회 불가(주문번호 없음) — pgProvider={}, paymentId={}, pgTid={}, failure={}",
                PaymentLogMarkers.PG_CANCEL_UNKNOWN, pgProvider, id, target.pgTid(), cancelResult.failure());
            return false;
        }

        PgCancelLookupResult lookup;
        try {
            lookup = pgPaymentGateway.lookupCancellation(pgProvider, target.pgOrderId(), target.pgTid());
        } catch (Exception e) {
            log.error("{} PG 취소 확인 조회 예외 — pgProvider={}, paymentId={}, pgTid={}, pgOrderId={}, failure={}",
                PaymentLogMarkers.PG_CANCEL_UNKNOWN, pgProvider, id, target.pgTid(), target.pgOrderId(),
                cancelResult.failure(), e);
            return false;
        }

        if (lookup.status() == PgCancelLookupStatus.CANCELED) {
            log.warn("PG 조회로 취소 확인 — pgProvider={}, paymentId={}, pgOrderId={}, failure={}, pgStatus={}",
                pgProvider, id, target.pgOrderId(), cancelResult.failure(), lookup.pgStatus());
            return true;
        }

        log.error(
            "{} PG 취소 확인 실패 — pgProvider={}, paymentId={}, pgTid={}, pgOrderId={}, failure={}, lookupStatus={}, pgStatus={}",
            PaymentLogMarkers.PG_CANCEL_UNKNOWN, pgProvider, id, target.pgTid(), target.pgOrderId(),
            cancelResult.failure(), lookup.status(), lookup.pgStatus()
        );
        return false;
    }

    private boolean isConsistentlyCancelled(PaymentId paymentId, PaymentCancelCode cancelCode) {
        if (cancelCode != PaymentCancelCode.ALREADY_CANCELLED) {
            return false;
        }
        try {
            return paymentCancellationExecutor.isPaymentCancelledInNewTx(paymentId);
        } catch (RuntimeException e) {
            log.error("결제 상태 확인 실패 — paymentId={}", paymentId.value(), e);
            return false;
        }
    }

    private static String pgCancelConfirmation(boolean recovered) {
        return recovered ? "PG 조회로 취소 확인" : "PG 취소 성공";
    }
}
