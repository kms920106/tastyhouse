package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopNoticeImage;
import com.tastyhouse.application.shop.port.out.write.ShopNoticeImageState;

final class ShopNoticeImageStateMapper {
    private ShopNoticeImageStateMapper() {
    }

    static ShopNoticeImage toDomain(ShopNoticeImageState state) {
        return ShopNoticeImage.reconstitute(
            state.id(),
            state.shopNoticeId(),
            state.imageFileId() == null ? null : UploadedFileId.of(state.imageFileId()),
            state.sortOrder()
        );
    }

    static ShopNoticeImageState toState(ShopNoticeImage shopNoticeImage) {
        return new ShopNoticeImageState(
            shopNoticeImage.getId(),
            shopNoticeImage.getShopNoticeId(),
            shopNoticeImage.getImageFileId() == null ? null : shopNoticeImage.getImageFileId().value(),
            shopNoticeImage.getSortOrder()
        );
    }
}
