package com.tastyhouse.application.shop.store;

import com.tastyhouse.application.shop.port.out.write.ShopPhotoCategoryImageState;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopPhotoCategoryImage;
import com.tastyhouse.domain.shop.vo.ShopPhotoCategoryId;

final class ShopPhotoCategoryImageStateMapper {
    private ShopPhotoCategoryImageStateMapper() {
    }

    static ShopPhotoCategoryImage toDomain(ShopPhotoCategoryImageState state) {
        return ShopPhotoCategoryImage.reconstitute(
            state.id(),
            state.shopPhotoCategoryId() == null ? null : ShopPhotoCategoryId.of(state.shopPhotoCategoryId()),
            state.imageFileId() == null ? null : UploadedFileId.of(state.imageFileId()),
            state.sort(),
            state.visible()
        );
    }

    static ShopPhotoCategoryImageState toState(ShopPhotoCategoryImage shopPhotoCategoryImage) {
        return new ShopPhotoCategoryImageState(
            shopPhotoCategoryImage.getId(),
            shopPhotoCategoryImage.getShopPhotoCategoryId() == null ? null : shopPhotoCategoryImage.getShopPhotoCategoryId().value(),
            shopPhotoCategoryImage.getImageFileId() == null ? null : shopPhotoCategoryImage.getImageFileId().value(),
            shopPhotoCategoryImage.getSort(),
            shopPhotoCategoryImage.isVisible()
        );
    }
}
