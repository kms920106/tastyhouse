package com.tastyhouse.ceoapi.shop.application.port.in;

import java.math.BigDecimal;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 편의정보(주차·발렛·찾아오는 길·표시 좌표) 등록·수정 command.
 *
 * <p>{@code directionsGuide}·표시 좌표는 미설정을 허용하므로 null 가드를 걸지 않는다.
 */
public record ShopConvenienceInfoUpdateCommand(
    Long ceoId,
    Long shopId,
    Boolean parkingAvailable,
    Boolean parkingPaid,
    Boolean valetAvailable,
    Boolean valetPaid,
    String directionsGuide,
    BigDecimal displayLatitude,
    BigDecimal displayLongitude
) {
    public ShopConvenienceInfoUpdateCommand {
        if (ceoId == null || shopId == null || parkingAvailable == null || parkingPaid == null
            || valetAvailable == null || valetPaid == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
