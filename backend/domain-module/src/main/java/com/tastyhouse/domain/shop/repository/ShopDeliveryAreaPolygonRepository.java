package com.tastyhouse.domain.shop.repository;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopDeliveryAreaPolygon;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 가게 배달지역 도형 write 포트.
 *
 * <p>가게당 1건({@code uk_shop_delivery_area_polygon_shop_id})이라 조회는 {@code shopId} 단건뿐이다.
 * 편집 화면이 도형 원본을 그대로 필요로 하므로 표현용 투영을 따로 두지 않고 이 포트로 읽는다 — 도형은
 * 조인이나 이름 조립이 필요 없는 단일 값이라 query DAO로 분리할 이득이 없다.
 */
public interface ShopDeliveryAreaPolygonRepository {

    Optional<ShopDeliveryAreaPolygon> findByShopId(ShopId shopId);

    ShopDeliveryAreaPolygon save(ShopDeliveryAreaPolygon shopDeliveryAreaPolygon);

    void deleteByShopId(ShopId shopId);
}
