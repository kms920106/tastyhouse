package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopBannerImage;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopBannerImageState;

final class ShopBannerImageStateMapper {
    private ShopBannerImageStateMapper() {
    }

    static ShopBannerImage toDomain(ShopBannerImageState state) {
        return ShopBannerImage.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.imageFileId() == null ? null : UploadedFileId.of(state.imageFileId()),
            state.sort()
        );
    }

    static ShopBannerImageState toState(ShopBannerImage shopBannerImage) {
        return new ShopBannerImageState(
            shopBannerImage.getId(),
            shopBannerImage.getShopId() == null ? null : shopBannerImage.getShopId().value(),
            shopBannerImage.getImageFileId() == null ? null : shopBannerImage.getImageFileId().value(),
            shopBannerImage.getSort()
        );
    }
}
