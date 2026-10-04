package com.tastyhouse.application.shop.port.in;

import java.math.BigDecimal;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record GeoPointCommand(
    BigDecimal latitude,
    BigDecimal longitude
) {

    public GeoPointCommand {
        if (latitude == null || longitude == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
