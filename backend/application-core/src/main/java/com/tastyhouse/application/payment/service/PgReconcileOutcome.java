package com.tastyhouse.application.payment.service;

enum PgReconcileOutcome {

    COMPLETED,
    ALREADY_COMPLETED,
    AMOUNT_MISMATCH,
    ORDER_NOT_CONFIRMABLE
}
