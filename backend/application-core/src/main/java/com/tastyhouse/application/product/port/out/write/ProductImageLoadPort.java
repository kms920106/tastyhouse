package com.tastyhouse.application.product.port.out.write;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.vo.ProductId;

public interface ProductImageLoadPort {

    UploadedFileId findRepresentativeImageFileId(ProductId productId);

    Optional<ProductImage> findById(Long id);

    List<ProductImage> findAllByProductId(ProductId productId);
}
