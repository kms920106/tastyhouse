package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 가게 배달가능지역(행정동 단위) 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopDeliveryAreaJpaEntity} + {@code ShopDeliveryAreaMapper}가 담당한다.
 *
 * <p>상태전이가 없어 등록(insert)과 삭제(delete)만 존재하므로 전 필드를 {@code final}로 두고 변경
 * 메서드를 두지 않는다({@code MemberFollow} 선례). 가게·행정동 중복 방지는 집합 차원의 불변식이라
 * {@code ShopDeliveryAreaService}가 담당한다.
 */
public class ShopDeliveryArea {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ShopId shopId;
    private final AdminDongId adminDongId;

    private ShopDeliveryArea(Long id, ShopId shopId, AdminDongId adminDongId) {
        this.id = id;
        this.shopId = shopId;
        this.adminDongId = adminDongId;
    }

    /**
     * 신규 배달가능지역을 생성한다. 아직 영속되지 않았으므로 식별자는 없다.
     */
    public static ShopDeliveryArea of(ShopId shopId, AdminDongId adminDongId) {
        return new ShopDeliveryArea(null, shopId, adminDongId);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopDeliveryArea reconstitute(Long id, ShopId shopId, AdminDongId adminDongId) {
        return new ShopDeliveryArea(id, shopId, adminDongId);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public AdminDongId getAdminDongId() {
        return this.adminDongId;
    }
}
