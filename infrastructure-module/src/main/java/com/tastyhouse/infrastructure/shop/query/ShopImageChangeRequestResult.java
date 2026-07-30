package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.core.domain.shop.domain.model.ShopImageType;
import com.tastyhouse.core.shared.model.ApprovalStatus;

public record ShopImageChangeRequestResult(
    Long id,
    Long shopId,
    ShopImageType imageType,
    Long imageFileId,
    ApprovalStatus status,
    String rejectReason
) {

}
