package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 메뉴 가격 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을
 * infrastructure에 둔다.
 */
final class ProductPriceMapper {

    private ProductPriceMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ProductPrice toDomain(ProductPriceJpaEntity entity) {
        return ProductPrice.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            entity.getPriceName(),
            entity.getDeliveryPrice(),
            entity.getStorePrice(),
            entity.getPickupPrice(),
            entity.getSort(),
            entity.getPickupPriceSetAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ProductPriceJpaEntity toEntity(ProductPrice domain) {
        return ProductPriceJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            domain.getPriceName(),
            domain.getDeliveryPrice(),
            domain.getStorePrice(),
            domain.getPickupPrice(),
            domain.getSort(),
            domain.getPickupPriceSetAt()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(ProductPriceJpaEntity entity, ProductPrice domain) {
        entity.applyChanges(
            domain.getPriceName(),
            domain.getDeliveryPrice(),
            domain.getStorePrice(),
            domain.getPickupPrice(),
            domain.getSort(),
            domain.getPickupPriceSetAt()
        );
    }
}
