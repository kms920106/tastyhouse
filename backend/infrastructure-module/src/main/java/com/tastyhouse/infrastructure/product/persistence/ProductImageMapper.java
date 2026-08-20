package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 상품 이미지 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ProductImageMapper {

    private ProductImageMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ProductImage toDomain(ProductImageJpaEntity entity) {
        return ProductImage.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            IdMapping.vo(entity.getImageFileId(), UploadedFileId::of),
            entity.getSort(),
            entity.isVisible()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ProductImageJpaEntity toEntity(ProductImage domain) {
        return ProductImageJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getSort(),
            domain.isVisible()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로).
     *
     * <p>{@code productId}·{@code imageFileId}는 복사하지 않는다 — 이미지의 소속과 파일은 바뀌지
     * 않고, 바꿔야 하면 새 이미지를 등록하는 것이 맞다.
     */
    static void applyChanges(ProductImageJpaEntity entity, ProductImage domain) {
        entity.applyChanges(domain.getSort(), domain.isVisible());
    }
}
