package com.tastyhouse.application.shop.port.out.write;

import java.time.LocalDateTime;

public record ShopDeliveryAreaAdjustmentRequestState(
    Long id,
    Long shopId,
    String counterpartShopName,
    String counterpartBusinessNumber,
    String franchiseName,
    String reason,
    Long consentFileId,
    String status,
    String rejectReason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
