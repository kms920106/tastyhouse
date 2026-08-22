package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopOrderNotice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopOrderNoticeId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 주문안내 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을
 * infrastructure에 둔다.
 */
final class ShopOrderNoticeMapper {

    private ShopOrderNoticeMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ShopOrderNotice toDomain(ShopOrderNoticeJpaEntity entity) {
        return ShopOrderNotice.reconstitute(
            IdMapping.vo(entity.getId(), ShopOrderNoticeId::of),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getContent(),
            entity.isHidden(),
            entity.getHiddenReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ShopOrderNoticeJpaEntity toEntity(ShopOrderNotice domain) {
        return ShopOrderNoticeJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getContent(),
            domain.isHidden(),
            domain.getHiddenReason()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(ShopOrderNoticeJpaEntity entity, ShopOrderNotice domain) {
        entity.applyChanges(
            domain.getContent(),
            domain.isHidden(),
            domain.getHiddenReason()
        );
    }
}
