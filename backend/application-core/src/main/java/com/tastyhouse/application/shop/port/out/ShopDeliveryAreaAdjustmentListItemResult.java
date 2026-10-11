package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

public record ShopDeliveryAreaAdjustmentListItemResult(
    Long id,
    Long shopId,
    String shopName,
    String counterpartShopName,
    String counterpartBusinessNumber,
    String franchiseName,
    String reason,
    String consentFileUrl,
    String status,
    String rejectReason,
    LocalDateTime createdAt
) {
}
