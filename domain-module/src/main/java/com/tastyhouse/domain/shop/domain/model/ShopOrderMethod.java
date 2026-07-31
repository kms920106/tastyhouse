package com.tastyhouse.domain.shop.domain.model;

import lombok.Getter;

/**
 * 상점-주문방식 배정 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopOrderMethodJpaEntity} + {@code ShopOrderMethodMapper}가 담당한다.
 */
@Getter
public class ShopOrderMethod {

    private final Long id;
    private final Long shopId;
    private final OrderMethod orderMethod;

    private ShopOrderMethod(Long id, Long shopId, OrderMethod orderMethod) {
        this.id = id;
        this.shopId = shopId;
        this.orderMethod = orderMethod;
    }

    public static ShopOrderMethod of(Long shopId, OrderMethod orderMethod) {
        return new ShopOrderMethod(null, shopId, orderMethod);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopOrderMethod reconstitute(Long id, Long shopId, OrderMethod orderMethod) {
        return new ShopOrderMethod(id, shopId, orderMethod);
    }
}
