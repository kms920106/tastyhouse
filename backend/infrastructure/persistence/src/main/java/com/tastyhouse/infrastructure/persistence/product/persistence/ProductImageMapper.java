package com.tastyhouse.infrastructure.persistence.product.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.vo.ProductId;

final class ProductImageMapper {

    private ProductImageMapper() {
    }

    static ProductImage toDomain(ProductImageJpaEntity entity) {
        return ProductImage.reconstitute(
            entity.getId(),
            entity.getProductId() == null ? null : ProductId.of(entity.getProductId()),
            entity.getImageFileId() == null ? null : UploadedFileId.of(entity.getImageFileId()),
            entity.getSort(),
            entity.isVisible()
        );
    }

    static ProductImageJpaEntity toEntity(ProductImage image) {
        return ProductImageJpaEntity.create(
            image.getProductId() == null ? null : image.getProductId().value(),
            image.getImageFileId() == null ? null : image.getImageFileId().value(),
            image.getSort(),
            image.isVisible()
        );
    }

    static void applyChanges(ProductImageJpaEntity entity, ProductImage image) {
        entity.applyChanges(
            image.getSort(),
            image.isVisible()
        );
    }
}
