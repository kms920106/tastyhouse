package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopHygieneBadge;

/**
 * 가게 위생 인증 뱃지 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ShopHygieneBadgeMapper {

    private ShopHygieneBadgeMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ShopHygieneBadge toDomain(ShopHygieneBadgeJpaEntity entity) {
        return ShopHygieneBadge.reconstitute(
            entity.getId(),
            entity.getShopId(),
            entity.getBadgeType(),
            entity.getCertifiedDate(),
            entity.getLastInspectionMonth(),
            entity.getCreatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ShopHygieneBadgeJpaEntity toEntity(ShopHygieneBadge domain) {
        return ShopHygieneBadgeJpaEntity.create(
            domain.getShopId(),
            domain.getBadgeType(),
            domain.getCertifiedDate(),
            domain.getLastInspectionMonth()
        );
    }
}
