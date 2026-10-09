package com.tastyhouse.application.product.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;

@Component
class ProductSoldOutUntilValidator {

    private static final long MIN_SOLD_OUT_MINUTES = 30L;

    private static final long MAX_SOLD_OUT_DAYS = 7L;

    public void validate(LocalDateTime soldOutUntil, LocalDateTime now) {
        if (soldOutUntil == null) {
            return;
        }
        if (soldOutUntil.isBefore(now.plusMinutes(MIN_SOLD_OUT_MINUTES))) {
            throw new ApplicationException(CeoErrorCode.PRODUCT_SOLD_OUT_UNTIL_TOO_SOON);
        }
        if (soldOutUntil.isAfter(now.plusDays(MAX_SOLD_OUT_DAYS))) {
            throw new ApplicationException(CeoErrorCode.PRODUCT_SOLD_OUT_UNTIL_TOO_FAR);
        }
    }
}
