package com.tastyhouse.application.shop.port.in;

import java.math.BigDecimal;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

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
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
