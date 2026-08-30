package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductRepresentativeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 사장님 추천(대표 메뉴) 지정 요청 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를
 * 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ProductRepresentativeRequestMapper {

    private ProductRepresentativeRequestMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ProductRepresentativeRequest toDomain(ProductRepresentativeRequestJpaEntity entity) {
        return ProductRepresentativeRequest.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getStatus(),
            entity.getRejectReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ProductRepresentativeRequestJpaEntity toEntity(ProductRepresentativeRequest domain) {
        return ProductRepresentativeRequestJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getStatus(),
            domain.getRejectReason()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(
        ProductRepresentativeRequestJpaEntity entity,
        ProductRepresentativeRequest domain
    ) {
        entity.applyChanges(
            domain.getStatus(),
            domain.getRejectReason()
        );
    }
}
