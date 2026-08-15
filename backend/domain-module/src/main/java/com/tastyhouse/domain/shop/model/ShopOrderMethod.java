package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 상점-주문방식 배정 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopOrderMethodJpaEntity} + {@code ShopOrderMethodMapper}가 담당한다.
 */
public class ShopOrderMethod {

    private final Long id;
    private final ShopId shopId;
    private final OrderMethod orderMethod;

    private ShopOrderMethod(Long id, ShopId shopId, OrderMethod orderMethod) {
        this.id = id;
        this.shopId = shopId;
        this.orderMethod = orderMethod;
    }

    public static ShopOrderMethod of(ShopId shopId, OrderMethod orderMethod) {
        return new ShopOrderMethod(null, shopId, orderMethod);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopOrderMethod reconstitute(Long id, ShopId shopId, OrderMethod orderMethod) {
        return new ShopOrderMethod(id, shopId, orderMethod);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public OrderMethod getOrderMethod() {
        return this.orderMethod;
    }
}
