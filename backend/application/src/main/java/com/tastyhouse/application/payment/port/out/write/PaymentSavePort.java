package com.tastyhouse.application.payment.port.out.write;

import com.tastyhouse.domain.payment.model.Payment;

public interface PaymentSavePort {

    Payment save(Payment payment);
}
