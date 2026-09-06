package com.tastyhouse.domain.product.service;

import java.util.List;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;

public record OrderProductSnapshot(
    ProductId productId,
    ProductPriceId productPriceId,
    String name,
    String priceName,
    UploadedFileId representativeImageFileId,
    int quantity,
    int originalPrice,
    Integer discountPrice,
    List<OrderProductOptionSnapshot> options
) {
    public OrderProductSnapshot {
        options = options == null ? List.of() : List.copyOf(options);
    }

    public int effectivePrice() {
        return this.discountPrice != null ? this.discountPrice : this.originalPrice;
    }
}
