package com.tastyhouse.core.domain.shop.application.dto.result;

import java.math.BigDecimal;

import com.tastyhouse.core.domain.shop.domain.model.ShopConvenienceInfo;

public record ShopConvenienceInfoResult(
    Long id,
    Long shopId,
    boolean parkingAvailable,
    boolean parkingPaid,
    boolean valetAvailable,
    boolean valetPaid,
    String directionsGuide,
    BigDecimal displayLatitude,
    BigDecimal displayLongitude
) {

    public static ShopConvenienceInfoResult from(ShopConvenienceInfo shopConvenienceInfo) {
        return new ShopConvenienceInfoResult(
            shopConvenienceInfo.getId(),
            shopConvenienceInfo.getShopId(),
            shopConvenienceInfo.isParkingAvailable(),
            shopConvenienceInfo.isParkingPaid(),
            shopConvenienceInfo.isValetAvailable(),
            shopConvenienceInfo.isValetPaid(),
            shopConvenienceInfo.getDirectionsGuide(),
            shopConvenienceInfo.getDisplayLatitude(),
            shopConvenienceInfo.getDisplayLongitude()
        );
    }
}
