package com.tastyhouse.application.payment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.application.payment.port.in.PgPaymentConfirmCommand;
import com.tastyhouse.application.payment.port.in.PgPaymentConfirmUseCase;
import com.tastyhouse.application.payment.port.out.PgConfirmFailure;
import com.tastyhouse.application.payment.port.out.PgConfirmResult;
import com.tastyhouse.application.payment.port.out.PgPaymentGatewayPort;
import com.tastyhouse.application.payment.port.out.PgPaymentLookupResult;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
class PgPaymentConfirmService implements PgPaymentConfirmUseCase {

    private static final Logger log = LoggerFactory.getLogger(PgPaymentConfirmService.class);

    private final PaymentConfirmationExecutor paymentConfirmationExecutor;
    private final PgPaymentGatewayPort pgPaymentGateway;

    public PgPaymentConfirmService(
        PaymentConfirmationExecutor paymentConfirmationExecutor,
        PgPaymentGatewayPort pgPaymentGateway
    ) {
        this.paymentConfirmationExecutor = paymentConfirmationExecutor;
        this.pgPaymentGateway = pgPaymentGateway;
    }

    @Override
    public Long confirmPgPayment(PgPaymentConfirmCommand command) {
        PgProvider pgProvider = PgProvider.from(command.pgProvider());
        String paymentKey = command.paymentKey();
        String pgOrderId = command.pgOrderId();
        Integer amount = command.amount();
        MemberId memberIdVo = MemberId.of(command.memberId());

        PgConfirmationTarget target = paymentConfirmationExecutor.prepareInNewTx(memberIdVo, pgOrderId, amount);
        if (!pgPaymentGateway.supports(pgProvider.name())) {
            throw new ApplicationException(ApplicationErrorCode.PG_PROVIDER_UNSUPPORTED,
                ApplicationErrorCode.PG_PROVIDER_UNSUPPORTED.getDefaultMessage() + ": " + pgProvider.name());
        }
        paymentConfirmationExecutor.recordPgAttemptInNewTx(pgOrderId, pgProvider);

        PgConfirmResult result = pgPaymentGateway.confirmPayment(
            pgProvider.name(),
            target.paymentId(), paymentKey, target.pgOrderId(), target.amount()
        );

        if (result.success()) {
            return applyApproval(memberIdVo, pgProvider, pgOrderId, result, amount);
        }

        if (!requiresLookup(result.failure())) {
            paymentConfirmationExecutor.failInNewTx(pgOrderId, result);
            throw approvalFailed(result);
        }

        PgPaymentLookupResult lookup = pgPaymentGateway.lookupPayment(pgProvider.name(), pgOrderId);
        return switch (lookup.status()) {
            case APPROVED -> reconcileApproval(pgProvider, pgOrderId, lookup.result(), amount);
            case NOT_APPROVED -> {
                paymentConfirmationExecutor.failInNewTx(pgOrderId, lookup.result());
                throw result.failure() == PgConfirmFailure.UNKNOWN
                    ? new ApplicationException(WebErrorCode.PAYMENT_APPROVAL_FAILED)
                    : approvalFailed(result);
            }
            case UNKNOWN -> {
                paymentConfirmationExecutor.failInNewTx(pgOrderId, lookup.result());
                throw new ApplicationException(WebErrorCode.PAYMENT_APPROVAL_UNCONFIRMED);
            }
        };
    }

    private Long applyApproval(MemberId memberId, PgProvider pgProvider, String pgOrderId, PgConfirmResult result, Integer amount) {
        PaymentId paymentId;
        try {
            paymentId = paymentConfirmationExecutor.applyInNewTx(memberId, pgProvider, pgOrderId, result);
        } catch (RuntimeException e) {
            log.error(
                "{} PG 승인 성공 후 DB 반영 실패 — pgProvider={}, pgOrderId={}, paymentKey={}, amount={}",
                PaymentLogMarkers.PG_DB_MISMATCH, pgProvider, pgOrderId, result.paymentKey(), amount, e
            );
            throw e;
        }

        log.info("PG 결제 승인 완료 — pgProvider={}, paymentId={}, amount={}", pgProvider, paymentId.value(), amount);
        return paymentId.value();
    }

    private Long reconcileApproval(PgProvider pgProvider, String pgOrderId, PgConfirmResult approval, Integer amount) {
        PgReconcileResult reconciled;
        try {
            reconciled = paymentConfirmationExecutor.reconcileApprovedInNewTx(pgOrderId, pgProvider, approval);
        } catch (RuntimeException e) {
            log.error(
                "{} PG 조회로 승인 확인 후 DB 반영 실패 — pgProvider={}, pgOrderId={}, paymentKey={}, amount={}",
                PaymentLogMarkers.PG_DB_MISMATCH, pgProvider, pgOrderId, approval.paymentKey(), amount, e
            );
            throw e;
        }

        return switch (reconciled.outcome()) {
            case COMPLETED, ALREADY_COMPLETED -> {
                log.info(
                    "PG 결제 조회로 승인 확인 — pgProvider={}, paymentId={}, outcome={}",
                    pgProvider, reconciled.paymentId().value(), reconciled.outcome()
                );
                yield reconciled.paymentId().value();
            }
            case AMOUNT_MISMATCH, ORDER_NOT_CONFIRMABLE ->
                throw new ApplicationException(WebErrorCode.PAYMENT_APPROVAL_UNCONFIRMED);
        };
    }

    private static boolean requiresLookup(PgConfirmFailure failure) {
        return failure == PgConfirmFailure.UNKNOWN || failure == PgConfirmFailure.ALREADY_PROCESSED;
    }

    private static ApplicationException approvalFailed(PgConfirmResult result) {
        return new ApplicationException(
            WebErrorCode.PAYMENT_APPROVAL_FAILED,
            result.errorMessage() != null
                ? result.errorMessage()
                : WebErrorCode.PAYMENT_APPROVAL_FAILED.getDefaultMessage()
        );
    }
}
