package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 가게 배달지역 도형(가게당 1건) 순수 도메인 모델.
 *
 * <p>이 도형은 <b>편집·표현의 원본</b>이며 주문 배달가능 판정에 직접 참여하지 않는다 — 판정의 유일한
 * 소스는 도형을 환산해 얻은 행정동 집합({@code SHOP_DELIVERY_AREA})이다. 도형을 따로 보관하는 이유는,
 * 행정동 집합만으로는 점주가 그린 모양을 되살릴 수 없어 <b>다시 편집할 수 없기</b> 때문이다.
 *
 * <p><b>{@code center}가 저장 시점 가게 좌표의 스냅샷인 이유</b>: 7km 상한은 가게 주소를 기준점으로 재는데,
 * 가게가 이전하면 기존 도형이 근거로 삼았던 기준점이 사라진다. 스냅샷이 있어야 "주소가 이만큼 이동했으니
 * 배달지역을 다시 설정하라"를 점주에게 알릴 수 있다. 스냅샷이 없다면 이전 후에도 낡은 도형이 조용히
 * 유효한 것처럼 보인다.
 *
 * <p>{@code maxRadiusMeters}·{@code ringCount}·{@code vertexCount}는 도형에서 파생되는 값이지만 컬럼으로
 * 비정규화해 둔다 — 목록·상태 표시에서 수천 정점을 파싱하지 않고 읽기 위해서다.
 */
public class ShopDeliveryAreaPolygon {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ShopId shopId;
    private GeoPolygon polygon;
    private GeoPoint center;
    private int maxRadiusMeters;

    private ShopDeliveryAreaPolygon(Long id, ShopId shopId, GeoPolygon polygon, GeoPoint center, int maxRadiusMeters) {
        this.id = id;
        this.shopId = shopId;
        this.polygon = polygon;
        this.center = center;
        this.maxRadiusMeters = maxRadiusMeters;
    }

    /**
     * 신규 도형을 생성한다. 최원거리 정점까지의 거리는 기준점으로부터 <b>도출</b>하므로 인자로 받지 않는다
     * — 호출자가 직접 계산해 넘기면 도형과 어긋난 값이 저장될 수 있다.
     */
    public static ShopDeliveryAreaPolygon of(ShopId shopId, GeoPolygon polygon, GeoPoint center) {
        requireArguments(shopId, polygon, center);
        return new ShopDeliveryAreaPolygon(null, shopId, polygon, center, computeMaxRadiusMeters(polygon, center));
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     *
     * <p>{@code maxRadiusMeters}를 저장값 그대로 받는다 — 재계산하면 저장 후 좌표 정밀도가 잘리면서
     * 조회 때마다 미세하게 다른 값이 나올 수 있다.
     */
    public static ShopDeliveryAreaPolygon reconstitute(
        Long id,
        ShopId shopId,
        GeoPolygon polygon,
        GeoPoint center,
        int maxRadiusMeters
    ) {
        requireArguments(shopId, polygon, center);
        return new ShopDeliveryAreaPolygon(id, shopId, polygon, center, maxRadiusMeters);
    }

    /**
     * 도형을 통째로 교체하고 기준점 스냅샷을 다시 찍는다.
     *
     * <p>부분 수정(정점 하나 이동 등)을 두지 않는 것은 의도된 것이다 — 도형이 조금이라도 바뀌면 환산
     * 결과가 달라지므로 어차피 전체를 다시 계산해야 하고, 부분 수정 API는 "환산은 안 했는데 도형은 바뀐"
     * 중간 상태를 만들 여지를 준다.
     */
    public void replace(GeoPolygon newPolygon, GeoPoint newCenter) {
        requireArguments(this.shopId, newPolygon, newCenter);
        this.polygon = newPolygon;
        this.center = newCenter;
        this.maxRadiusMeters = computeMaxRadiusMeters(newPolygon, newCenter);
    }

    private static int computeMaxRadiusMeters(GeoPolygon polygon, GeoPoint center) {
        return (int) Math.ceil(polygon.maxDistanceMetersFrom(center));
    }

    private static void requireArguments(ShopId shopId, GeoPolygon polygon, GeoPoint center) {
        if (shopId == null) {
            throw new IllegalArgumentException("배달지역 도형의 가게 식별자는 필수입니다.");
        }
        if (polygon == null) {
            throw new IllegalArgumentException("배달지역 도형은 필수입니다.");
        }
        if (center == null) {
            throw new IllegalArgumentException("배달지역 도형의 기준점은 필수입니다.");
        }
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public GeoPolygon getPolygon() {
        return this.polygon;
    }

    /** 저장 시점 가게 좌표 스냅샷(7km 상한의 기준점). */
    public GeoPoint getCenter() {
        return this.center;
    }

    /** 기준점에서 최원거리 정점까지의 거리(m). */
    public int getMaxRadiusMeters() {
        return this.maxRadiusMeters;
    }

    public int getRingCount() {
        return this.polygon.ringCount();
    }

    public int getVertexCount() {
        return this.polygon.vertexCount();
    }
}
