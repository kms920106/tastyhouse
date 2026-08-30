package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaPolygon;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.GeoPolygonTextCodec;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 가게 배달지역 도형 도메인 모델 ↔ JPA 엔티티 변환기.
 *
 * <p>좌표 인코딩·디코딩은 {@link GeoPolygonTextCodec}에 위임한다 — 도메인이 저장 형식을 알지 않도록
 * 형식 지식을 영속 계층에 가둔다.
 */
final class ShopDeliveryAreaPolygonMapper {

    private ShopDeliveryAreaPolygonMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ShopDeliveryAreaPolygon toDomain(ShopDeliveryAreaPolygonJpaEntity entity) {
        return ShopDeliveryAreaPolygon.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            GeoPolygonTextCodec.decode(entity.getRings()),
            GeoPoint.of(entity.getCenterLatitude(), entity.getCenterLongitude()),
            entity.getMaxRadiusMeters()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ShopDeliveryAreaPolygonJpaEntity toEntity(ShopDeliveryAreaPolygon domain) {
        return ShopDeliveryAreaPolygonJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            GeoPolygonTextCodec.encode(domain.getPolygon()),
            domain.getCenter().latitude(),
            domain.getCenter().longitude(),
            domain.getMaxRadiusMeters(),
            domain.getRingCount(),
            domain.getVertexCount()
        );
    }

    /**
     * managed 엔티티에 도메인 변경분을 복사한다(load-copy-save).
     */
    static void applyChanges(ShopDeliveryAreaPolygonJpaEntity entity, ShopDeliveryAreaPolygon domain) {
        entity.applyChanges(
            GeoPolygonTextCodec.encode(domain.getPolygon()),
            domain.getCenter().latitude(),
            domain.getCenter().longitude(),
            domain.getMaxRadiusMeters(),
            domain.getRingCount(),
            domain.getVertexCount()
        );
    }
}
