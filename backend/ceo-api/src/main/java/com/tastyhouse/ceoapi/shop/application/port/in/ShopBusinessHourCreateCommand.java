package com.tastyhouse.ceoapi.shop.application.port.in;

import java.time.LocalTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 운영시간 등록 command. 경로 변수 {@code shopId}와 principal의 {@code ceoId}는 컨트롤러가 주입한다.
 */
public record ShopBusinessHourCreateCommand(
    Long ceoId,
    Long shopId,
    String dayType,
    LocalTime openTime,
    LocalTime closeTime,
    Boolean isClosed,
    Boolean is24Hours
) {
    public ShopBusinessHourCreateCommand {
        if (ceoId == null || shopId == null || dayType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
