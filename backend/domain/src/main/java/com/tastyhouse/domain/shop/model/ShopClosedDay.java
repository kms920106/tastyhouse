package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 상점 정기 휴무 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopClosedDayJpaEntity} + {@code ShopClosedDayMapper}가 담당한다.
 */
public class ShopClosedDay {

    private final Long id;
    private final ShopId shopId;
    private final ClosedDayType closedDayType;

    private ShopClosedDay(Long id, ShopId shopId, ClosedDayType closedDayType) {
        this.id = id;
        this.shopId = shopId;
        this.closedDayType = closedDayType;
    }

    public static ShopClosedDay of(ShopId shopId, ClosedDayType closedDayType) {
        return new ShopClosedDay(null, shopId, closedDayType);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopClosedDay reconstitute(Long id, ShopId shopId, ClosedDayType closedDayType) {
        return new ShopClosedDay(id, shopId, closedDayType);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ClosedDayType getClosedDayType() {
        return this.closedDayType;
    }
}
