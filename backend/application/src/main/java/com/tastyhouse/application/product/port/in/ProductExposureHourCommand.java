package com.tastyhouse.application.product.port.in;

import java.time.LocalTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductExposureHourCommand(
    String dayType,
    LocalTime startTime,
    LocalTime endTime
) {
    public ProductExposureHourCommand {
        if (dayType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
