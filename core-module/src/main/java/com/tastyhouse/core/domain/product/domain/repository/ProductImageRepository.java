package com.tastyhouse.core.domain.product.domain.repository;

import com.tastyhouse.core.domain.product.domain.model.ProductImage;

import java.util.List;

public interface ProductImageRepository {

    List<ProductImage> findActiveByProductIdOrderBySort(Long productId);

    String findFilePathByImageFileId(Long imageFileId);

    ProductImage save(ProductImage productImage);
}
