package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopBannerImage {
    private final Long id;
    private final ShopId shopId;
    private final UploadedFileId imageFileId;
    private final Integer sort;

    private ShopBannerImage(Long id, ShopId shopId, UploadedFileId imageFileId, Integer sort) {
        this.id = id;
        this.shopId = shopId;
        this.imageFileId = imageFileId;
        this.sort = sort;
    }

    public static ShopBannerImage of(ShopId shopId, UploadedFileId imageFileId, Integer sort) {
        return new ShopBannerImage(null, shopId, imageFileId, sort);
    }

    public static ShopBannerImage reconstitute(Long id, ShopId shopId, UploadedFileId imageFileId, Integer sort) {
        return new ShopBannerImage(id, shopId, imageFileId, sort);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public UploadedFileId getImageFileId() {
        return this.imageFileId;
    }

    public Integer getSort() {
        return this.sort;
    }
}
