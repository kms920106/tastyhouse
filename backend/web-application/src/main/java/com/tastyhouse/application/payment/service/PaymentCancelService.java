package com.tastyhouse.application.payment.service;

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
import com.tastyhouse.application.payment.port.out.PgCancelResult;
import com.tastyhouse.application.payment.port.out.PgPaymentGatewayPort;

@Service
class PaymentCancelService implements PaymentCancelUseCase {

    private static final Logger log = LoggerFactory.getLogger(PaymentCancelService.class);

    private static final String PG_DB_MISMATCH = "PG_DB_MISMATCH";

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
        if (pgCancelAttempted && !requestPgCancel(pgProvider, target.pgTid(), cancelReason)) {
            log.error("결제 취소 실패 — paymentId={}, cancelCode={}", id, PaymentCancelCode.CANCEL_FAILED);
            return PaymentCancelCode.CANCEL_FAILED;
        }

        PaymentCancelCode cancelCode;
        try {
            cancelCode = paymentCancellationExecutor.applyInNewTx(memberIdVo, paymentId, cancelReason);
        } catch (RuntimeException e) {
            if (pgCancelAttempted) {
                log.error(
                    "{} PG 취소 성공 후 DB 반영 실패 — pgProvider={}, paymentId={}, pgTid={}",
                    PG_DB_MISMATCH, target.pgProvider(), id, target.pgTid(), e
                );
            }
            throw e;
        }

        if (cancelCode != PaymentCancelCode.SUCCESS) {

            if (pgCancelAttempted) {
                log.error(
                    "{} PG 취소 성공 후 재판정 거절 — pgProvider={}, paymentId={}, pgTid={}, cancelCode={}",
                    PG_DB_MISMATCH, target.pgProvider(), id, target.pgTid(), cancelCode
                );
            }
            log.error("결제 취소 실패 — paymentId={}, cancelCode={}", id, cancelCode);
        }
        return cancelCode;
    }

    private boolean requestPgCancel(String pgProvider, String pgTid, String cancelReason) {
        try {
            PgCancelResult cancelResult = pgPaymentGateway.cancelPayment(pgProvider, pgTid, cancelReason);
            if (!cancelResult.success()) {
                log.error("PG 취소 거절 — pgProvider={}, pgTid={}, errorCode={}, errorMessage={}",
                    pgProvider, pgTid, cancelResult.errorCode(), cancelResult.errorMessage());
            }
            return cancelResult.success();
        } catch (Exception e) {
            log.error("PG 취소 요청 예외 — pgProvider={}, pgTid={}", pgProvider, pgTid, e);
            return false;
        }
    }
}
