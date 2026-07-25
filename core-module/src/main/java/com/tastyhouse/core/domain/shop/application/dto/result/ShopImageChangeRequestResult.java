package com.tastyhouse.core.domain.shop.application.dto.result;

import com.tastyhouse.core.domain.shop.domain.model.ShopImageChangeRequest;
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

    public static ShopImageChangeRequestResult from(ShopImageChangeRequest shopImageChangeRequest) {
        return new ShopImageChangeRequestResult(
            shopImageChangeRequest.getId(),
            shopImageChangeRequest.getShopId(),
            shopImageChangeRequest.getImageType(),
            shopImageChangeRequest.getImageFileId(),
            shopImageChangeRequest.getStatus(),
            shopImageChangeRequest.getRejectReason()
        );
    }
}
