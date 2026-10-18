package com.tastyhouse.application.payment.service;

import java.time.LocalDateTime;

record PaymentReconcileClaim(int attemptCount, LocalDateTime nextAttemptAt) {

}
