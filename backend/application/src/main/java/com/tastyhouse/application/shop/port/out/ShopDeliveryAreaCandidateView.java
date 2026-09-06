package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;

public record ShopDeliveryAreaCandidateView(
    long adminDongId,
    String regionName,
    BigDecimal centerLatitude,
    BigDecimal centerLongitude,
    boolean alreadyRegistered
) {
}
