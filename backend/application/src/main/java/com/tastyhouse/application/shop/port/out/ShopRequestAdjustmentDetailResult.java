package com.tastyhouse.application.shop.port.out;

public record ShopRequestAdjustmentDetailResult(
    String counterpartShopName,
    String counterpartBusinessNumber,
    String franchiseName,
    String reason,
    String consentFileUrl,
    String status,
    String rejectReason
) {
}
