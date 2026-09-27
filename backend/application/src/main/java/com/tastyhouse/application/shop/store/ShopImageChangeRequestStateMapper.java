package com.tastyhouse.application.shop.store;

import com.tastyhouse.application.shop.port.out.write.ShopImageChangeRequestState;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.ShopImageChangeRequest;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopImageChangeRequestStateMapper {
    private ShopImageChangeRequestStateMapper() {
    }

    static ShopImageChangeRequest toDomain(ShopImageChangeRequestState state) {
        return ShopImageChangeRequest.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.imageType() == null ? null : ShopImageType.valueOf(state.imageType()),
            state.imageFileId() == null ? null : UploadedFileId.of(state.imageFileId()),
            state.status() == null ? null : ApprovalStatus.valueOf(state.status()),
            state.rejectReason(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ShopImageChangeRequestState toState(ShopImageChangeRequest shopImageChangeRequest) {
        return new ShopImageChangeRequestState(
            shopImageChangeRequest.getId(),
            shopImageChangeRequest.getShopId() == null ? null : shopImageChangeRequest.getShopId().value(),
            shopImageChangeRequest.getImageType() == null ? null : shopImageChangeRequest.getImageType().name(),
            shopImageChangeRequest.getImageFileId() == null ? null : shopImageChangeRequest.getImageFileId().value(),
            shopImageChangeRequest.getStatus() == null ? null : shopImageChangeRequest.getStatus().name(),
            shopImageChangeRequest.getRejectReason(),
            shopImageChangeRequest.getCreatedAt(),
            shopImageChangeRequest.getUpdatedAt()
        );
    }
}
