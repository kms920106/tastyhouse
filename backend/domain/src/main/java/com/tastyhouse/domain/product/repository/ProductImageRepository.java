package com.tastyhouse.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.vo.ProductId;

public interface ProductImageRepository {
    UploadedFileId findRepresentativeImageFileId(ProductId productId);

    ProductImage save(ProductImage productImage);

    Optional<ProductImage> findById(Long id);

    List<ProductImage> findAllByProductId(ProductId productId);

    void delete(ProductImage productImage);
}
