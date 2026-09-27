package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.ShopMenuCollectionImage;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopMenuCollectionImageState;

final class ShopMenuCollectionImageStateMapper {
    private ShopMenuCollectionImageStateMapper() {
    }

    static ShopMenuCollectionImage toDomain(ShopMenuCollectionImageState state) {
        return ShopMenuCollectionImage.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.imageFileId() == null ? null : UploadedFileId.of(state.imageFileId()),
            state.sort(),
            state.status() == null ? null : ApprovalStatus.valueOf(state.status()),
            state.rejectReason(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ShopMenuCollectionImageState toState(ShopMenuCollectionImage shopMenuCollectionImage) {
        return new ShopMenuCollectionImageState(
            shopMenuCollectionImage.getId(),
            shopMenuCollectionImage.getShopId() == null ? null : shopMenuCollectionImage.getShopId().value(),
            shopMenuCollectionImage.getImageFileId() == null ? null : shopMenuCollectionImage.getImageFileId().value(),
            shopMenuCollectionImage.getSort(),
            shopMenuCollectionImage.getStatus() == null ? null : shopMenuCollectionImage.getStatus().name(),
            shopMenuCollectionImage.getRejectReason(),
            shopMenuCollectionImage.getCreatedAt(),
            shopMenuCollectionImage.getUpdatedAt()
        );
    }
}
