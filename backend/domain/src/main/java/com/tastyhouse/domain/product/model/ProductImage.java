package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.vo.ProductId;

public class ProductImage {
    private final Long id;
    private final ProductId productId;
    private final UploadedFileId imageFileId;
    private final Integer sort;
    private final boolean visible;

    private ProductImage(Long id, ProductId productId, UploadedFileId imageFileId, Integer sort, boolean visible) {
        this.id = id;
        this.productId = productId;
        this.imageFileId = imageFileId;
        this.sort = sort;
        this.visible = visible;
    }

    public static ProductImage of(ProductId productId, UploadedFileId imageFileId, Integer sort, boolean visible) {
        return new ProductImage(null, productId, imageFileId, sort, visible);
    }

    public static ProductImage reconstitute(
        Long id,
        ProductId productId,
        UploadedFileId imageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ProductImage(id, productId, imageFileId, sort, visible);
    }

    public Long getId() {
        return this.id;
    }

    public ProductId getProductId() {
        return this.productId;
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
