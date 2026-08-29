package com.tastyhouse.ceoapi.product.application.port.in;

import java.time.LocalTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 노출 요일·시간대 command. {@code dayType}은 경계 타입인 문자열이고 enum 승격은 서비스가 한다.
 */
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
