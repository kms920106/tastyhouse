package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 에디터 초이스(상점 추천 선택지) 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopChoiceJpaEntity} + {@code ShopChoiceMapper}가 담당한다.
 */
public class ShopChoice {

    private final Long id;
    private final ShopId shopId;
    private String title;
    private String content;

    private ShopChoice(Long id, ShopId shopId, String title, String content) {
        this.id = id;
        this.shopId = shopId;
        this.title = title;
        this.content = content;
    }

    public static ShopChoice of(ShopId shopId, String title, String content) {
        return new ShopChoice(null, shopId, title, content);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopChoice reconstitute(Long id, ShopId shopId, String title, String content) {
        return new ShopChoice(id, shopId, title, content);
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return this.content;
    }
}
