package com.tastyhouse.webapi.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.payment.domain.model.PaymentMethod;
import com.tastyhouse.core.domain.payment.domain.model.PgProvider;
import com.tastyhouse.core.domain.payment.application.PaymentCommandService;
import com.tastyhouse.core.domain.payment.application.PaymentQueryService;
import com.tastyhouse.core.domain.payment.application.dto.command.CancelPaymentCommand;
import com.tastyhouse.core.domain.payment.application.dto.command.ConfirmPaymentCommand;
import com.tastyhouse.core.domain.payment.application.dto.command.PaymentCreateCommand;
import com.tastyhouse.core.domain.payment.application.dto.command.RequestRefundCommand;
import com.tastyhouse.core.domain.payment.application.dto.command.TossConfirmCommand;
import com.tastyhouse.core.domain.payment.application.dto.result.PaymentCancelResult;
import com.tastyhouse.core.domain.payment.application.dto.result.PaymentRefundResult;
import com.tastyhouse.core.domain.payment.application.dto.result.PaymentResult;
import com.tastyhouse.webapi.payment.response.PaymentCancelResponse;
import com.tastyhouse.webapi.payment.response.PaymentRefundResponse;
import com.tastyhouse.webapi.payment.response.PaymentResponse;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentCommandService paymentCommandService;
    private final PaymentQueryService paymentQueryService;

    public PaymentResponse createPayment(Long memberId, Long orderId, String paymentMethod) {
        MemberId targetMemberId = MemberId.of(memberId);
        PaymentResult result = paymentCommandService.createPayment(
            targetMemberId, PaymentCreateCommand.of(orderId, PaymentMethod.from(paymentMethod)));
        return PaymentResponse.from(result);
    }

    public PaymentResponse confirmPayment(
        Long paymentId,
        String pgProvider,
        String pgTid,
        String pgOrderId,
        String cardCompany,
        String cardNumber,
        Integer installmentMonths,
        String receiptUrl
    ) {
        PaymentResult result = paymentCommandService.confirmPayment(
            ConfirmPaymentCommand.of(paymentId, PgProvider.from(pgProvider), pgTid, pgOrderId, cardCompany, cardNumber, installmentMonths, receiptUrl)
        );
        return PaymentResponse.from(result);
    }

    public PaymentResponse confirmTossPayment(Long memberId, String paymentKey, String pgOrderId, Integer amount) {
        MemberId targetMemberId = MemberId.of(memberId);
        PaymentResult result = paymentCommandService.confirmTossPayment(
            targetMemberId, TossConfirmCommand.of(paymentKey, pgOrderId, amount));
        return PaymentResponse.from(result);
    }

    public PaymentResponse getPaymentByOrderId(Long memberId, Long orderId) {
        PaymentResult result = paymentQueryService.getPaymentByOrderId(MemberId.of(memberId), orderId);
        return PaymentResponse.from(result);
    }

    public PaymentCancelResponse cancelPayment(Long memberId, Long paymentId, String cancelReason) {
        MemberId targetMemberId = MemberId.of(memberId);
        PaymentCancelResult result = paymentCommandService.cancelPayment(
            targetMemberId, paymentId, CancelPaymentCommand.of(cancelReason));
        return PaymentCancelResponse.of(result);
    }

    public PaymentResponse completeOnSitePayment(Long memberId, Long paymentId) {
        MemberId targetMemberId = MemberId.of(memberId);
        PaymentResult result = paymentCommandService.completeOnSitePayment(targetMemberId, paymentId);
        return PaymentResponse.from(result);
    }

    public PaymentRefundResponse requestRefund(Long memberId, Long paymentId, Integer refundAmount, String refundReason) {
        MemberId targetMemberId = MemberId.of(memberId);
        PaymentRefundResult result = paymentCommandService.requestRefund(
            targetMemberId, paymentId, RequestRefundCommand.of(refundAmount, refundReason));
        return PaymentRefundResponse.from(result);
    }
}
