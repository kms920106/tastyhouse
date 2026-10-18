package com.tastyhouse.application.payment.port.out.write;

import java.time.LocalDateTime;

import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.vo.PaymentId;

public interface PaymentSavePort {

    Payment save(Payment payment);

    void saveReconcileSchedule(PaymentId paymentId, int attemptCount, LocalDateTime nextAttemptAt);

    boolean saveReconcileScheduleIfDue(
        PaymentId paymentId, int expectedAttemptCount, LocalDateTime dueAt, LocalDateTime nextAttemptAt
    );
}
