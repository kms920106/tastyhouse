package com.tastyhouse.application.shop.port.out;

import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;

public record ShopRequestAdjustmentDetailResult(
    String counterpartShopName,
    String counterpartBusinessNumber,
    String franchiseName,
    String reason,
    String consentFileUrl,
    DeliveryAreaAdjustmentStatus status,
    String rejectReason
) {
}
