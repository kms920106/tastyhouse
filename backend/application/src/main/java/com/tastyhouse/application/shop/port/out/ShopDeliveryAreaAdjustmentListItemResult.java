package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;

public record ShopDeliveryAreaAdjustmentListItemResult(
    Long id,
    Long shopId,
    String shopName,
    String counterpartShopName,
    String counterpartBusinessNumber,
    String franchiseName,
    String reason,
    String consentFileUrl,
    DeliveryAreaAdjustmentStatus status,
    String rejectReason,
    LocalDateTime createdAt
) {
}
