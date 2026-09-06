package com.tastyhouse.application.payment.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.model.PaymentCancelCode;
import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.port.PgPaymentGateway;
import com.tastyhouse.domain.payment.port.dto.PgCancelResult;
import com.tastyhouse.domain.payment.port.dto.PgConfirmResult;
import com.tastyhouse.domain.payment.service.PaymentCancellationService;
import com.tastyhouse.domain.payment.service.PaymentCancellationTarget;
import com.tastyhouse.domain.payment.service.PaymentConfirmationService;
import com.tastyhouse.domain.payment.service.PgConfirmation;
import com.tastyhouse.domain.payment.service.TossConfirmationTarget;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.application.payment.port.out.PaymentCancelResult;
import com.tastyhouse.application.payment.port.in.PaymentCancelCommand;
import com.tastyhouse.application.payment.port.in.PaymentCommandUseCase;
import com.tastyhouse.application.payment.port.in.PaymentConfirmCommand;
import com.tastyhouse.application.payment.port.in.PaymentCreateCommand;
import com.tastyhouse.application.payment.port.in.PaymentOnSiteCompleteCommand;
import com.tastyhouse.application.payment.port.in.PaymentRefundRequestCommand;
import com.tastyhouse.application.payment.port.in.TossPaymentConfirmCommand;

@Service
@WebApp
public class PaymentCommandService implements PaymentCommandUseCase {

    private static final Logger log = LoggerFactory.getLogger(PaymentCommandService.class);

    private static final String PG_DB_MISMATCH = "PG_DB_MISMATCH";

    private final PaymentConfirmationService paymentConfirmationService;
    private final PaymentCancellationService paymentCancellationService;
    private final PaymentConfirmationExecutor paymentConfirmationExecutor;
    private final PaymentCancellationExecutor paymentCancellationExecutor;
    private final PgPaymentGateway pgPaymentGateway;

    public PaymentCommandService(
        PaymentConfirmationService paymentConfirmationService,
        PaymentCancellationService paymentCancellationService,
        PaymentConfirmationExecutor paymentConfirmationExecutor,
        PaymentCancellationExecutor paymentCancellationExecutor,
        PgPaymentGateway pgPaymentGateway
    ) {
        this.paymentConfirmationService = paymentConfirmationService;
        this.paymentCancellationService = paymentCancellationService;
        this.paymentConfirmationExecutor = paymentConfirmationExecutor;
        this.paymentCancellationExecutor = paymentCancellationExecutor;
        this.pgPaymentGateway = pgPaymentGateway;
    }

    @Transactional
    @Override
    public Long createPayment(PaymentCreateCommand command) {
        MemberId memberIdVo = MemberId.of(command.memberId());
        OrderId orderIdVo = OrderId.of(command.orderId());
        PaymentId paymentId = paymentConfirmationService.open(
            memberIdVo, orderIdVo, PaymentMethod.from(command.paymentMethod())
        );
        return paymentId.value();
    }

    @Transactional
    @Override
    public Long confirmPayment(PaymentConfirmCommand command) {
        PaymentId paymentId = PaymentId.of(command.paymentId());
        PgConfirmation confirmation = PgConfirmation.of(
            PgProvider.from(command.pgProvider()),
            command.pgTid(),
            command.pgOrderId(),
            command.cardCompany(),
            command.cardNumber(),
            command.installmentMonths(),
            command.receiptUrl()
        );
        return paymentConfirmationService.confirm(paymentId, confirmation).value();
    }

    @Override
    public Long confirmTossPayment(TossPaymentConfirmCommand command) {
        String paymentKey = command.paymentKey();
        String pgOrderId = command.pgOrderId();
        Integer amount = command.amount();
        MemberId memberIdVo = MemberId.of(command.memberId());

        TossConfirmationTarget target = paymentConfirmationExecutor.prepareInNewTx(memberIdVo, pgOrderId, amount);

        PgConfirmResult result = pgPaymentGateway.confirmPayment(
            target.paymentId(), paymentKey, target.pgOrderId(), target.amount()
        );

        if (!result.success()) {
            paymentConfirmationExecutor.failInNewTx(pgOrderId, result);
            throw new BusinessException(
                ErrorCode.PAYMENT_APPROVAL_FAILED,
                result.errorMessage() != null
                    ? result.errorMessage()
                    : ErrorCode.PAYMENT_APPROVAL_FAILED.getDefaultMessage()
            );
        }

        PaymentId paymentId;
        try {
            paymentId = paymentConfirmationExecutor.applyInNewTx(memberIdVo, pgOrderId, result);
        } catch (RuntimeException e) {

            log.error(
                "{} 토스 승인 성공 후 DB 반영 실패 — pgOrderId={}, paymentKey={}, amount={}",
                PG_DB_MISMATCH, pgOrderId, result.paymentKey(), amount, e
            );
            throw e;
        }

        log.info("토스 결제 승인 완료 — paymentId={}, amount={}", paymentId.value(), amount);
        return paymentId.value();
    }

    @Transactional
    @Override
    public Long completeOnSitePayment(PaymentOnSiteCompleteCommand command) {
        MemberId memberIdVo = MemberId.of(command.memberId());
        PaymentId paymentId = PaymentId.of(command.paymentId());
        return paymentConfirmationService.completeOnSitePayment(memberIdVo, paymentId).value();
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

        if (target.pgCancelRequired() && !requestPgCancel(target.pgTid(), cancelReason)) {
            log.error("결제 취소 실패 — paymentId={}, cancelCode={}", id, PaymentCancelCode.CANCEL_FAILED);
            return PaymentCancelCode.CANCEL_FAILED;
        }

        PaymentCancelCode cancelCode;
        try {
            cancelCode = paymentCancellationExecutor.applyInNewTx(memberIdVo, paymentId, cancelReason);
        } catch (RuntimeException e) {
            if (target.pgCancelRequired()) {
                log.error(
                    "{} PG 취소 성공 후 DB 반영 실패 — paymentId={}, pgTid={}",
                    PG_DB_MISMATCH, id, target.pgTid(), e
                );
            }
            throw e;
        }

        if (cancelCode != PaymentCancelCode.SUCCESS) {

            if (target.pgCancelRequired()) {
                log.error(
                    "{} PG 취소 성공 후 재판정 거절 — paymentId={}, pgTid={}, cancelCode={}",
                    PG_DB_MISMATCH, id, target.pgTid(), cancelCode
                );
            }
            log.error("결제 취소 실패 — paymentId={}, cancelCode={}", id, cancelCode);
        }
        return cancelCode;
    }

    @Transactional
    @Override
    public Long requestRefund(PaymentRefundRequestCommand command) {
        MemberId memberIdVo = MemberId.of(command.memberId());
        PaymentId paymentId = PaymentId.of(command.paymentId());
        return paymentCancellationService
            .requestRefund(memberIdVo, paymentId, command.refundAmount(), command.refundReason())
            .value();
    }

    private boolean requestPgCancel(String pgTid, String cancelReason) {
        try {
            PgCancelResult cancelResult = pgPaymentGateway.cancelPayment(pgTid, cancelReason);
            if (!cancelResult.success()) {
                log.error("PG 취소 거절 — pgTid={}, errorCode={}, errorMessage={}",
                    pgTid, cancelResult.errorCode(), cancelResult.errorMessage());
            }
            return cancelResult.success();
        } catch (Exception e) {
            log.error("PG 취소 요청 예외 — pgTid={}", pgTid, e);
            return false;
        }
    }
}
