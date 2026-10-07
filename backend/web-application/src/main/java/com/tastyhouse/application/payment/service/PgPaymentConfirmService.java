package com.tastyhouse.application.payment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.application.payment.port.in.PgPaymentConfirmCommand;
import com.tastyhouse.application.payment.port.in.PgPaymentConfirmUseCase;
import com.tastyhouse.application.payment.port.out.PgConfirmResult;
import com.tastyhouse.application.payment.port.out.PgPaymentGateway;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
class PgPaymentConfirmService implements PgPaymentConfirmUseCase {

    private static final Logger log = LoggerFactory.getLogger(PgPaymentConfirmService.class);

    private static final String PG_DB_MISMATCH = "PG_DB_MISMATCH";

    private final PaymentConfirmationExecutor paymentConfirmationExecutor;
    private final PgPaymentGateway pgPaymentGateway;

    public PgPaymentConfirmService(
        PaymentConfirmationExecutor paymentConfirmationExecutor,
        PgPaymentGateway pgPaymentGateway
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

        PgConfirmResult result = pgPaymentGateway.confirmPayment(
            pgProvider.name(),
            target.paymentId(), paymentKey, target.pgOrderId(), target.amount()
        );

        if (!result.success()) {
            paymentConfirmationExecutor.failInNewTx(pgOrderId, result);
            throw new ApplicationException(
                WebErrorCode.PAYMENT_APPROVAL_FAILED,
                result.errorMessage() != null
                    ? result.errorMessage()
                    : WebErrorCode.PAYMENT_APPROVAL_FAILED.getDefaultMessage()
            );
        }

        PaymentId paymentId;
        try {
            paymentId = paymentConfirmationExecutor.applyInNewTx(memberIdVo, pgProvider, pgOrderId, result);
        } catch (RuntimeException e) {

            log.error(
                "{} PG 승인 성공 후 DB 반영 실패 — pgProvider={}, pgOrderId={}, paymentKey={}, amount={}",
                PG_DB_MISMATCH, pgProvider, pgOrderId, result.paymentKey(), amount, e
            );
            throw e;
        }

        log.info("PG 결제 승인 완료 — pgProvider={}, paymentId={}, amount={}", pgProvider, paymentId.value(), amount);
        return paymentId.value();
    }
}
