package com.tastyhouse.core.domain.payment.domain.repository;

import com.tastyhouse.core.domain.payment.domain.model.PaymentRefund;
import com.tastyhouse.core.domain.payment.domain.model.RefundStatus;
import com.tastyhouse.core.domain.payment.domain.vo.PaymentId;

import java.util.List;
import java.util.Optional;

public interface PaymentRefundRepository {

    List<PaymentRefund> findByPaymentIdOrderByCreatedAtDesc(PaymentId paymentId);

    Optional<PaymentRefund> findByPgRefundId(String pgRefundId);

    List<PaymentRefund> findByRefundStatusOrderByCreatedAtDesc(RefundStatus refundStatus);

    PaymentRefund save(PaymentRefund paymentRefund);
}
