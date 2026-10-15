package com.tastyhouse.application.payment.port.out.write;

import java.util.Set;

import com.tastyhouse.domain.payment.model.RefundStatus;
import com.tastyhouse.domain.payment.vo.PaymentId;

public interface PaymentRefundLoadPort {

    long findRefundAmountSum(PaymentId paymentId, Set<RefundStatus> statuses);
}
