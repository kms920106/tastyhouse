package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;
import java.util.List;

public record ShopDetailViewResult(
    Long id,
    String name,
    BigDecimal latitude,
    BigDecimal longitude,
    Double rating,
    String roadAddress,
    String lotAddress,
    String phoneNumber,
    List<ShopPhoneNumberResult> phoneNumbers,
    String trademarkImageUrl,
    String operatingStatus,
    String unavailableReason,
    String unavailableReasonName,
    int minOrderAmount,
    int minDeliveryTip,
    int maxDeliveryTip,
    boolean scheduledOrderEnabled
) {
}
