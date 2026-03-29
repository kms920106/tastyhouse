package com.tastyhouse.core.repository.payment;

import com.tastyhouse.core.entity.payment.Payment;
import com.tastyhouse.core.entity.payment.PaymentRefund;
import com.tastyhouse.core.entity.payment.PaymentStatus;
import com.tastyhouse.core.entity.payment.RefundStatus;
import com.tastyhouse.core.entity.payment.TossPaymentRecord;
import com.tastyhouse.core.entity.payment.dto.OrderListItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Optional<Payment> findById(Long paymentId);

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByPgTid(String pgTid);

    Optional<Payment> findByPgOrderId(String pgOrderId);

    List<Payment> findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus paymentStatus);

    boolean existsByOrderId(Long orderId);

    Payment save(Payment payment);

    List<PaymentRefund> findRefundsByPaymentIdOrderByCreatedAtDesc(Long paymentId);

    Optional<PaymentRefund> findRefundByPgRefundId(String pgRefundId);

    List<PaymentRefund> findRefundsByRefundStatusOrderByCreatedAtDesc(RefundStatus refundStatus);

    PaymentRefund saveRefund(PaymentRefund paymentRefund);

    Optional<TossPaymentRecord> findTossRecordByPaymentId(Long paymentId);

    Optional<TossPaymentRecord> findTossRecordByPaymentKey(String paymentKey);

    Optional<TossPaymentRecord> findTossRecordByOrderId(String orderId);

    TossPaymentRecord saveTossRecord(TossPaymentRecord tossPaymentRecord);

    Page<OrderListItemDto> findOrderListByMemberId(Long memberId, Pageable pageable);
}
