package com.tastyhouse.application.product.port.in;

import java.time.LocalTime;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductExposureHourCommand(
    String dayType,
    LocalTime startTime,
    LocalTime endTime
) {

    public ProductExposureHourCommand {
        if (dayType == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
