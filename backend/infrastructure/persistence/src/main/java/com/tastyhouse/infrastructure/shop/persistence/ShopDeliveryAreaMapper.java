package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 가게 배달가능지역 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을
 * infrastructure에 둔다.
 */
final class ShopDeliveryAreaMapper {

    private ShopDeliveryAreaMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     *
     * <p>{@code source}가 null인 행은 {@code MANUAL}로 본다 — 컬럼이 {@code DEFAULT 'MANUAL'}이라 정상
     * 경로에서는 null이 나올 수 없지만, 구버전 백엔드가 쓴 행이 섞이는 배포 전환 구간을 안전하게 넘긴다.
     */
    static ShopDeliveryArea toDomain(ShopDeliveryAreaJpaEntity entity) {
        return ShopDeliveryArea.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getAdminDongId(), AdminDongId::of),
            entity.getSource() == null ? DeliveryAreaSource.MANUAL : entity.getSource()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ShopDeliveryAreaJpaEntity toEntity(ShopDeliveryArea domain) {
        return ShopDeliveryAreaJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getAdminDongId(), AdminDongId::value),
            domain.getSource()
        );
    }
}
