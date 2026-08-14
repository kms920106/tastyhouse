package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.domain.review.model.ShopReviewDisplaySetting;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 가게 리뷰 노출 정렬 설정 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을
 * infrastructure에 둔다.
 */
final class ShopReviewDisplaySettingMapper {

    private ShopReviewDisplaySettingMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ShopReviewDisplaySetting toDomain(ShopReviewDisplaySettingJpaEntity entity) {
        return ShopReviewDisplaySetting.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getSortType(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ShopReviewDisplaySettingJpaEntity toEntity(ShopReviewDisplaySetting domain) {
        return ShopReviewDisplaySettingJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getSortType()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(ShopReviewDisplaySettingJpaEntity entity, ShopReviewDisplaySetting domain) {
        entity.applyChanges(domain.getSortType());
    }
}
