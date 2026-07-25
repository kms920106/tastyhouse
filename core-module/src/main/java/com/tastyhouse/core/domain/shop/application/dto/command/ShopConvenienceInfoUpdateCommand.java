package com.tastyhouse.core.domain.shop.application.dto.command;

import java.math.BigDecimal;

public record ShopConvenienceInfoUpdateCommand(
    Long shopId,
    boolean parkingAvailable,
    boolean parkingPaid,
    boolean valetAvailable,
    boolean valetPaid,
    String directionsGuide,
    BigDecimal displayLatitude,
    BigDecimal displayLongitude
) {

    public static ShopConvenienceInfoUpdateCommand of(
        Long shopId,
        boolean parkingAvailable,
        boolean parkingPaid,
        boolean valetAvailable,
        boolean valetPaid,
        String directionsGuide,
        BigDecimal displayLatitude,
        BigDecimal displayLongitude
    ) {
        return new ShopConvenienceInfoUpdateCommand(shopId, parkingAvailable, parkingPaid, valetAvailable, valetPaid,
            directionsGuide, displayLatitude, displayLongitude);
    }
}
