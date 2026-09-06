package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ProductImageMapper {
    private ProductImageMapper() {
    }

    static ProductImage toDomain(ProductImageJpaEntity entity) {
        return ProductImage.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            IdMapping.vo(entity.getImageFileId(), UploadedFileId::of),
            entity.getSort(),
            entity.isVisible()
        );
    }

    static ProductImageJpaEntity toEntity(ProductImage domain) {
        return ProductImageJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getSort(),
            domain.isVisible()
        );
    }

    static void applyChanges(ProductImageJpaEntity entity, ProductImage domain) {
        entity.applyChanges(domain.getSort(), domain.isVisible());
    }
}
