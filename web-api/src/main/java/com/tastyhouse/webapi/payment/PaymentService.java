package com.tastyhouse.webapi.payment;

import com.tastyhouse.core.entity.order.Order;
import com.tastyhouse.core.entity.order.OrderStatus;
import com.tastyhouse.core.entity.payment.Payment;
import com.tastyhouse.core.entity.payment.PaymentMethod;
import com.tastyhouse.core.entity.payment.PaymentRefund;
import com.tastyhouse.core.entity.payment.PaymentStatus;
import com.tastyhouse.core.entity.payment.PgProvider;
import com.tastyhouse.core.entity.point.MemberPoint;
import com.tastyhouse.core.entity.point.MemberPointHistory;
import com.tastyhouse.core.entity.point.PointType;
import com.tastyhouse.core.exception.AccessDeniedException;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.repository.order.OrderJpaRepository;
import com.tastyhouse.core.repository.payment.PaymentJpaRepository;
import com.tastyhouse.core.repository.payment.PaymentRefundJpaRepository;
import com.tastyhouse.core.repository.payment.TossPaymentRecordJpaRepository;
import com.tastyhouse.core.repository.point.MemberPointHistoryJpaRepository;
import com.tastyhouse.core.repository.point.MemberPointJpaRepository;
import com.tastyhouse.external.payment.toss.TossPaymentClient;
import com.tastyhouse.external.payment.toss.TossPaymentUtils;
import com.tastyhouse.external.payment.toss.dto.TossPaymentConfirmResponse;
import com.tastyhouse.external.payment.toss.dto.TossPaymentConfirmResult;
import com.tastyhouse.core.entity.payment.TossPaymentRecord;
import com.tastyhouse.webapi.payment.request.PaymentCancelRequest;
import com.tastyhouse.webapi.payment.request.PaymentConfirmRequest;
import com.tastyhouse.webapi.payment.request.PaymentCreateRequest;
import com.tastyhouse.webapi.payment.request.RefundRequest;
import com.tastyhouse.webapi.payment.request.TossPaymentConfirmApiRequest;
import com.tastyhouse.webapi.payment.response.PaymentRefundResponse;
import com.tastyhouse.webapi.payment.response.PaymentCancelCode;
import com.tastyhouse.webapi.payment.response.PaymentCancelResponse;
import com.tastyhouse.webapi.payment.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentRefundJpaRepository paymentRefundJpaRepository;
    private final TossPaymentRecordJpaRepository tossPaymentRecordJpaRepository;
    private final OrderJpaRepository orderJpaRepository;
    private final MemberPointJpaRepository memberPointJpaRepository;
    private final MemberPointHistoryJpaRepository memberPointHistoryJpaRepository;
    private final TossPaymentClient tossPaymentClient;

    private static final int CASH_POINT_EARN_RATE = 10;

    @Transactional
    public PaymentResponse createPayment(Long memberId, PaymentCreateRequest request) {
        Order order = orderJpaRepository.findById(request.orderId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new AccessDeniedException(ErrorCode.PAYMENT_ORDER_ACCESS_DENIED);
        }

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_INVALID_ORDER_STATUS);
        }

        if (paymentJpaRepository.existsByOrderId(request.orderId())) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_IN_PROGRESS);
        }

        String pgOrderId = generatePgOrderId();

        Payment payment = Payment.builder()
            .orderId(request.orderId())
            .paymentMethod(request.paymentMethod())
            .paymentStatus(PaymentStatus.PENDING)
            .amount(order.getFinalAmount())
            .pgOrderId(pgOrderId)
            .build();

        Payment savedPayment = paymentJpaRepository.save(payment);

        return buildPaymentResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse confirmPayment(PaymentConfirmRequest request) {
        Payment payment = paymentJpaRepository.findById(request.paymentId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "결제를 찾을 수 없습니다."));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PENDING_APPROVAL);
        }

        Order order = orderJpaRepository.findById(payment.getOrderId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        payment.updatePgInfo(request.pgProvider(), request.pgTid(), request.pgOrderId());

        if (request.cardCompany() != null) {
            payment.updateCardInfo(request.cardCompany(), request.cardNumber(), request.installmentMonths());
        }

        payment.complete(request.pgTid(), LocalDateTime.now(), request.receiptUrl());

        order.confirm();

        return buildPaymentResponse(payment);
    }

    @Transactional
    public PaymentResponse confirmTossPayment(Long memberId, TossPaymentConfirmApiRequest request) {
        Payment payment = paymentJpaRepository.findByPgOrderId(request.pgOrderId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "결제를 찾을 수 없습니다."));

        Order order = orderJpaRepository.findById(payment.getOrderId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new AccessDeniedException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PENDING_APPROVAL);
        }

        if (!payment.getAmount().equals(request.amount())) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        TossPaymentConfirmResponse response = tossPaymentClient.confirmPayment(
            request.paymentKey(),
            request.pgOrderId(),
            request.amount()
        );

        if (response.isError()) {
            log.error("Toss payment confirm failed. orderId: {}, errorCode: {}, errorMessage: {}",
                request.pgOrderId(), response.getCode(), response.getMessage());
            payment.fail();

            // 토스 결제 실패 정보 저장
            TossPaymentRecord tossPaymentRecord = buildTossPaymentRecord(payment.getId(), response);
            tossPaymentRecordJpaRepository.save(tossPaymentRecord);

            throw new BusinessException(ErrorCode.PAYMENT_APPROVAL_FAILED,
                response.getMessage() != null ? response.getMessage() : ErrorCode.PAYMENT_APPROVAL_FAILED.getDefaultMessage());
        }

        payment.updatePgInfo(PgProvider.TOSS, response.getPaymentKey(), response.getOrderId());

        if (response.getCard() != null) {
            payment.updateCardInfo(
                TossPaymentUtils.mapIssuerCodeToCardCompany(response.getCard().getIssuerCode()),
                response.getCard().getNumber(),
                response.getCard().getInstallmentPlanMonths()
            );
        }

        LocalDateTime approvedAt = TossPaymentUtils.parseDateTime(response.getApprovedAt());
        String receiptUrl = response.getReceipt() != null ? response.getReceipt().getUrl() : null;

        payment.complete(response.getPaymentKey(), approvedAt, receiptUrl);
        order.confirm();

        // 토스 결제 성공 정보 저장
        TossPaymentRecord tossPaymentRecord = buildTossPaymentRecord(payment.getId(), response);
        tossPaymentRecordJpaRepository.save(tossPaymentRecord);

        log.info("Toss payment confirmed successfully. paymentId: {}, orderId: {}, amount: {}",
            payment.getId(), response.getOrderId(), response.getTotalAmount());

        return buildPaymentResponse(payment);
    }

    @Transactional
    public PaymentCancelResponse cancelPayment(Long memberId, Long paymentId, PaymentCancelRequest request) {
        Payment payment = paymentJpaRepository.findById(paymentId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "결제를 찾을 수 없습니다."));

        Order order = orderJpaRepository.findById(payment.getOrderId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new AccessDeniedException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        PaymentCancelCode cancelCode = validateOrderStatusForCancel(order.getOrderStatus());
        if (cancelCode != PaymentCancelCode.SUCCESS) {
            return PaymentCancelResponse.of(cancelCode);
        }

        // 토스 PG 결제인 경우 토스 취소 API 호출
        if (payment.getPgProvider() == PgProvider.TOSS
                && payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            try {
                TossPaymentConfirmResponse tossResponse = tossPaymentClient.cancelPayment(
                    payment.getPgTid(), request.cancelReason()
                );

                if (tossResponse.isError()) {
                    log.error("Toss payment cancel failed. paymentId: {}, errorCode: {}, errorMessage: {}",
                        paymentId, tossResponse.getCode(), tossResponse.getMessage());
                    return PaymentCancelResponse.of(PaymentCancelCode.CANCEL_FAILED);
                }

                // 토스 취소 응답 기록 저장
                TossPaymentRecord tossPaymentRecord = buildTossPaymentRecord(payment.getId(), tossResponse);
                tossPaymentRecordJpaRepository.save(tossPaymentRecord);
            } catch (Exception e) {
                log.error("Toss payment cancel exception. paymentId: {}", paymentId, e);
                return PaymentCancelResponse.of(PaymentCancelCode.CANCEL_FAILED);
            }
        }

        payment.cancel(request.cancelReason());
        order.cancel();

        restorePoints(memberId, order);

        return PaymentCancelResponse.of(PaymentCancelCode.SUCCESS);
    }

    private void restorePoints(Long memberId, Order order) {
        if (order.getUsedPoint() <= 0 && order.getEarnedPoint() <= 0) return;

        MemberPoint memberPoint = memberPointJpaRepository.findByMemberId(memberId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_NOT_FOUND,
                "포인트 정보를 찾을 수 없습니다. memberId=" + memberId));

        if (order.getUsedPoint() > 0) {
            memberPoint.addPoints(order.getUsedPoint());
            memberPointHistoryJpaRepository.save(MemberPointHistory.builder()
                .memberId(memberId)
                .pointType(PointType.REFUND)
                .pointAmount(order.getUsedPoint())
                .reason("결제 취소 환불")
                .build());
        }

        if (order.getEarnedPoint() > 0) {
            int deductAmount = Math.min(memberPoint.getAvailablePoints(), order.getEarnedPoint());
            memberPoint.deductPoints(deductAmount);
            memberPointHistoryJpaRepository.save(MemberPointHistory.builder()
                .memberId(memberId)
                .pointType(PointType.USE)
                .pointAmount(-deductAmount)
                .reason("결제 취소 적립금 회수")
                .build());
        }
    }

    private PaymentCancelCode validateOrderStatusForCancel(OrderStatus orderStatus) {
        return switch (orderStatus) {
            case PREPARING -> PaymentCancelCode.ALREADY_PREPARING;
            case CANCELLED -> PaymentCancelCode.ALREADY_CANCELLED;
            case COMPLETED -> PaymentCancelCode.ORDER_COMPLETED;
            case PENDING, CONFIRMED -> PaymentCancelCode.SUCCESS;
        };
    }

    @Transactional
    public PaymentRefundResponse requestRefund(Long memberId, Long paymentId, RefundRequest request) {
        Payment payment = paymentJpaRepository.findById(paymentId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "결제를 찾을 수 없습니다."));

        Order order = orderJpaRepository.findById(payment.getOrderId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new AccessDeniedException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_COMPLETED);
        }

        if (request.refundAmount() > payment.getAmount()) {
            throw new BusinessException(ErrorCode.PAYMENT_REFUND_AMOUNT_EXCEEDED);
        }

        PaymentRefund refund = PaymentRefund.builder()
            .paymentId(paymentId)
            .refundAmount(request.refundAmount())
            .refundReason(request.refundReason())
            .build();

        PaymentRefund savedRefund = paymentRefundJpaRepository.save(refund);

        return buildRefundResponse(savedRefund);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long memberId, Long orderId) {
        Order order = orderJpaRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new AccessDeniedException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        Payment payment = paymentJpaRepository.findByOrderId(orderId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "결제 정보를 찾을 수 없습니다."));

        return buildPaymentResponse(payment);
    }

    @Transactional
    public PaymentResponse completeOnSitePayment(Long memberId, Long paymentId) {
        Payment payment = paymentJpaRepository.findById(paymentId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "결제를 찾을 수 없습니다."));

        Order order = orderJpaRepository.findById(payment.getOrderId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new AccessDeniedException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PENDING);
        }

        if (!isOnSitePayment(payment.getPaymentMethod())) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_ON_SITE);
        }

        processOnSitePaymentCompletion(payment, order, memberId);

        return buildPaymentResponse(payment);
    }

    private void processOnSitePaymentCompletion(Payment payment, Order order, Long memberId) {
        payment.complete(null, LocalDateTime.now(), null);
        order.confirm();

        if (isOnSitePayment(payment.getPaymentMethod())) {
            int earnedPoint = (int) (payment.getAmount() * CASH_POINT_EARN_RATE / 100.0);

            MemberPoint memberPoint = memberPointJpaRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    MemberPoint newPoint = MemberPoint.builder()
                        .memberId(memberId)
                        .availablePoints(0)
                        .build();
                    return memberPointJpaRepository.save(newPoint);
                });

            memberPoint.addPoints(earnedPoint);
            order.updateEarnedPoint(earnedPoint);

            MemberPointHistory pointHistory = MemberPointHistory.builder()
                .memberId(memberId)
                .pointType(PointType.EARNED)
                .pointAmount(earnedPoint)
                .reason("현장 현금 결제 적립 (" + CASH_POINT_EARN_RATE + "%)")
                .build();
            memberPointHistoryJpaRepository.save(pointHistory);
        }
    }

    private boolean isOnSitePayment(PaymentMethod method) {
        return method == PaymentMethod.CASH_ON_SITE || method == PaymentMethod.CARD_ON_SITE;
    }

    private String generatePgOrderId() {
        return "PG" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private PaymentResponse buildPaymentResponse(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getOrderId(),
            payment.getPaymentMethod(),
            payment.getPaymentStatus(),
            payment.getAmount(),
            payment.getPgProvider(),
            payment.getPgTid(),
            payment.getPgOrderId(),
            payment.getCardCompany(),
            payment.getCardNumber(),
            payment.getInstallmentMonths(),
            payment.getApprovedAt(),
            payment.getCancelledAt(),
            payment.getCancelReason(),
            payment.getReceiptUrl(),
            payment.getCashReceiptNumber(),
            payment.getCashReceiptType(),
            payment.getCreatedAt()
        );
    }

    private PaymentRefundResponse buildRefundResponse(PaymentRefund refund) {
        return new PaymentRefundResponse(
            refund.getId(),
            refund.getPaymentId(),
            refund.getRefundAmount(),
            refund.getRefundReason(),
            refund.getRefundStatus(),
            refund.getPgRefundId(),
            refund.getRefundedAt(),
            refund.getCreatedAt()
        );
    }

    private TossPaymentRecord buildTossPaymentRecord(Long paymentId, TossPaymentConfirmResponse response) {
        TossPaymentRecord.TossPaymentRecordBuilder builder = TossPaymentRecord.builder()
            .paymentId(paymentId)
            .version(response.getVersion())
            .paymentKey(response.getPaymentKey())
            .type(response.getType())
            .orderId(response.getOrderId())
            .orderName(response.getOrderName())
            .mId(response.getMId())
            .currency(response.getCurrency())
            .method(response.getMethod())
            .totalAmount(response.getTotalAmount())
            .balanceAmount(response.getBalanceAmount())
            .status(response.getStatus())
            .requestedAt(TossPaymentUtils.parseDateTime(response.getRequestedAt()))
            .approvedAt(TossPaymentUtils.parseDateTime(response.getApprovedAt()))
            .useEscrow(response.getUseEscrow())
            .lastTransactionKey(response.getLastTransactionKey())
            .suppliedAmount(response.getSuppliedAmount())
            .vat(response.getVat())
            .cultureExpense(response.getCultureExpense())
            .taxFreeAmount(response.getTaxFreeAmount())
            .taxExemptionAmount(response.getTaxExemptionAmount())
            .isPartialCancelable(response.getIsPartialCancelable())
            .country(response.getCountry());

        // 카드 정보
        if (response.getCard() != null) {
            TossPaymentConfirmResponse.Card card = response.getCard();
            builder.cardAmount(card.getAmount())
                .cardIssuerCode(card.getIssuerCode())
                .cardAcquirerCode(card.getAcquirerCode())
                .cardNumber(card.getNumber())
                .cardInstallmentPlanMonths(card.getInstallmentPlanMonths())
                .cardApproveNo(card.getApproveNo())
                .cardUseCardPoint(card.getUseCardPoint())
                .cardType(card.getCardType())
                .cardOwnerType(card.getOwnerType())
                .cardAcquireStatus(card.getAcquireStatus())
                .cardIsInterestFree(card.getIsInterestFree())
                .cardInterestPayer(card.getInterestPayer());
        }

        // 가상계좌 정보
        if (response.getVirtualAccount() != null) {
            TossPaymentConfirmResponse.VirtualAccount va = response.getVirtualAccount();
            builder.virtualAccountType(va.getAccountType())
                .virtualAccountNumber(va.getAccountNumber())
                .virtualAccountBankCode(va.getBankCode())
                .virtualAccountCustomerName(va.getCustomerName())
                .virtualAccountDueDate(TossPaymentUtils.parseDateTime(va.getDueDate()))
                .virtualAccountRefundStatus(va.getRefundStatus())
                .virtualAccountExpired(va.getExpired())
                .virtualAccountSettlementStatus(va.getSettlementStatus());
        }

        // 휴대폰 정보
        if (response.getMobilePhone() != null) {
            TossPaymentConfirmResponse.MobilePhone mp = response.getMobilePhone();
            builder.mobilePhoneCustomerMobilePhone(mp.getCustomerMobilePhone())
                .mobilePhoneSettlementStatus(mp.getSettlementStatus())
                .mobilePhoneReceiptUrl(mp.getReceiptUrl());
        }

        // 계좌이체 정보
        if (response.getTransfer() != null) {
            TossPaymentConfirmResponse.Transfer transfer = response.getTransfer();
            builder.transferBankCode(transfer.getBankCode())
                .transferSettlementStatus(transfer.getSettlementStatus());
        }

        // 간편결제 정보
        if (response.getEasyPay() != null) {
            TossPaymentConfirmResponse.EasyPay easyPay = response.getEasyPay();
            builder.easyPayProvider(easyPay.getProvider())
                .easyPayAmount(easyPay.getAmount())
                .easyPayDiscountAmount(easyPay.getDiscountAmount());
        }

        // 영수증 정보
        if (response.getReceipt() != null) {
            builder.receiptUrl(response.getReceipt().getUrl());
        }

        // 결제창 정보
        if (response.getCheckout() != null) {
            builder.checkoutUrl(response.getCheckout().getUrl());
        }

        // 실패 정보
        if (response.getFailure() != null) {
            builder.failureCode(response.getFailure().getCode())
                .failureMessage(response.getFailure().getMessage());
        }

        // 에러 정보 (최상위)
        if (response.getCode() != null) {
            builder.failureCode(response.getCode())
                .failureMessage(response.getMessage());
        }

        return builder.build();
    }

}
