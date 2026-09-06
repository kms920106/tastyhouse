package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.vo.ShopPhotoCategoryId;

public class ShopPhotoCategoryImage {
    private final Long id;
    private final ShopPhotoCategoryId shopPhotoCategoryId;
    private UploadedFileId imageFileId;
    private Integer sort;
    private boolean visible;

    private ShopPhotoCategoryImage(
        Long id,
        ShopPhotoCategoryId shopPhotoCategoryId,
        UploadedFileId imageFileId,
        Integer sort,
        boolean visible
    ) {
        this.id = id;
        this.shopPhotoCategoryId = shopPhotoCategoryId;
        this.imageFileId = imageFileId;
        this.sort = sort;
        this.visible = visible;
    }

    public static ShopPhotoCategoryImage of(
        ShopPhotoCategoryId shopPhotoCategoryId,
        UploadedFileId imageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopPhotoCategoryImage(null, shopPhotoCategoryId, imageFileId, sort, visible);
    }

    public static ShopPhotoCategoryImage reconstitute(
        Long id,
        ShopPhotoCategoryId shopPhotoCategoryId,
        UploadedFileId imageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopPhotoCategoryImage(id, shopPhotoCategoryId, imageFileId, sort, visible);
    }

    public void update(UploadedFileId imageFileId, Integer sort, boolean visible) {
        this.imageFileId = imageFileId;
        this.sort = sort;
        this.visible = visible;
    }

    public Long getId() {
        return this.id;
    }

    public ShopPhotoCategoryId getShopPhotoCategoryId() {
        return this.shopPhotoCategoryId;
    }

    public UploadedFileId getImageFileId() {
        return this.imageFileId;
    }

    public Integer getSort() {
        return this.sort;
    }

    public boolean isVisible() {
        return this.visible;
    }
}
