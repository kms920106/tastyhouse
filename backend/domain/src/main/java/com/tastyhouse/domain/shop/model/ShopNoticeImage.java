package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.file.vo.UploadedFileId;

public class ShopNoticeImage {
    private final Long id;
    private final Long shopNoticeId;
    private final UploadedFileId imageFileId;
    private final int sortOrder;

    private ShopNoticeImage(Long id, Long shopNoticeId, UploadedFileId imageFileId, int sortOrder) {
        this.id = id;
        this.shopNoticeId = shopNoticeId;
        this.imageFileId = imageFileId;
        this.sortOrder = sortOrder;
    }

    public static ShopNoticeImage of(Long shopNoticeId, UploadedFileId imageFileId, int sortOrder) {
        return new ShopNoticeImage(null, shopNoticeId, imageFileId, sortOrder);
    }

    public static ShopNoticeImage reconstitute(Long id, Long shopNoticeId, UploadedFileId imageFileId, int sortOrder) {
        return new ShopNoticeImage(id, shopNoticeId, imageFileId, sortOrder);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopNoticeId() {
        return this.shopNoticeId;
    }

    public UploadedFileId getImageFileId() {
        return this.imageFileId;
    }

    public int getSortOrder() {
        return this.sortOrder;
    }
}
