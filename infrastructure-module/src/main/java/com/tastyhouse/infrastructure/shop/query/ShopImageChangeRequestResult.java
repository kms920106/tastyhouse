package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.domain.shop.domain.model.ShopImageType;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

public record ShopImageChangeRequestResult(
    Long id,
    Long shopId,
    ShopImageType imageType,
    String imageUrl,
    ApprovalStatus status,
    String rejectReason
) {

}
