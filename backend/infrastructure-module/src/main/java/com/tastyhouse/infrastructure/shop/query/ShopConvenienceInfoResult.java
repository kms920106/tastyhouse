package com.tastyhouse.infrastructure.shop.query;

import java.math.BigDecimal;


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

}
