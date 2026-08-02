package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;

/**
 * 상점 임시 휴무 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ShopTemporaryClosureMapper {

    private ShopTemporaryClosureMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ShopTemporaryClosure toDomain(ShopTemporaryClosureJpaEntity entity) {
        return ShopTemporaryClosure.reconstitute(
            entity.getId(),
            entity.getShopId(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getCreatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ShopTemporaryClosureJpaEntity toEntity(ShopTemporaryClosure domain) {
        return ShopTemporaryClosureJpaEntity.create(
            domain.getShopId(),
            domain.getStartDate(),
            domain.getEndDate()
        );
    }
}
