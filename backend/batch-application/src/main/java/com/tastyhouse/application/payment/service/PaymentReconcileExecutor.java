package com.tastyhouse.application.payment.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.application.payment.port.out.PgConfirmResult;
import com.tastyhouse.application.payment.port.out.write.PaymentLoadPort;
import com.tastyhouse.application.payment.port.out.write.PaymentSavePort;

@Component
class PaymentReconcileExecutor {

    private final PaymentConfirmationService paymentConfirmationService;
    private final PaymentLoadPort paymentLoadPort;
    private final PaymentSavePort paymentSavePort;

    public PaymentReconcileExecutor(
        PaymentConfirmationService paymentConfirmationService,
        PaymentLoadPort paymentLoadPort,
        PaymentSavePort paymentSavePort
    ) {
        this.paymentConfirmationService = paymentConfirmationService;
        this.paymentLoadPort = paymentLoadPort;
        this.paymentSavePort = paymentSavePort;
    }

    @Transactional
    public Optional<PaymentReconcileClaim> claimNextAttemptInNewTx(PaymentId paymentId, LocalDateTime dueAt) {
        int attemptsSoFar = paymentLoadPort.findReconcileAttemptCount(paymentId);
        LocalDateTime nextAttemptAt = PaymentReconcileBackoffPolicy.nextAttemptAt(attemptsSoFar, dueAt, LocalDateTime.now());
        if (!paymentSavePort.saveReconcileScheduleIfDue(paymentId, attemptsSoFar, dueAt, nextAttemptAt)) {
            return Optional.empty();
        }
        return Optional.of(new PaymentReconcileClaim(attemptsSoFar + 1, nextAttemptAt));
    }

    @Transactional
    public PgReconcileResult reconcileApprovedInNewTx(String pgOrderId, PgProvider pgProvider, PgConfirmResult approval) {
        return paymentConfirmationService.reconcileApproved(pgOrderId, pgProvider, approval);
    }

    @Transactional
    public void failUnapprovedInNewTx(String pgOrderId, PgConfirmResult result) {
        paymentConfirmationService.failPgConfirmation(pgOrderId, result);
    }
}
