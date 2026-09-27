package com.tastyhouse.application.product.store;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.out.write.ProductImageState;

final class ProductImageStateMapper {
    private ProductImageStateMapper() {
    }

    static ProductImage toDomain(ProductImageState state) {
        return ProductImage.reconstitute(
            state.id(),
            state.productId() == null ? null : ProductId.of(state.productId()),
            state.imageFileId() == null ? null : UploadedFileId.of(state.imageFileId()),
            state.sort(),
            state.visible()
        );
    }

    static ProductImageState toState(ProductImage image) {
        return new ProductImageState(
            image.getId(),
            image.getProductId() == null ? null : image.getProductId().value(),
            image.getImageFileId() == null ? null : image.getImageFileId().value(),
            image.getSort(),
            image.isVisible()
        );
    }
}
