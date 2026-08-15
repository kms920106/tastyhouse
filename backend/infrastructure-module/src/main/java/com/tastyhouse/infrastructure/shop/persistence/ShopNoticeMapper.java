package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 점주 공지 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을
 * infrastructure에 둔다.
 */
final class ShopNoticeMapper {

    private ShopNoticeMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ShopNotice toDomain(ShopNoticeJpaEntity entity) {
        return ShopNotice.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getContent(),
            entity.isExposed(),
            entity.isHidden(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ShopNoticeJpaEntity toEntity(ShopNotice domain) {
        return ShopNoticeJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getContent(),
            domain.isExposed(),
            domain.isHidden()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(ShopNoticeJpaEntity entity, ShopNotice domain) {
        entity.applyChanges(
            domain.getContent(),
            domain.isExposed(),
            domain.isHidden()
        );
    }
}
