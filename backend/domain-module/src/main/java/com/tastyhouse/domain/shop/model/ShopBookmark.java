package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 상점 북마크 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopBookmarkJpaEntity} + {@code ShopBookmarkMapper}가 담당한다.
 */
public class ShopBookmark {

    private final Long id;
    private final ShopId shopId;
    private final MemberId memberId;

    private ShopBookmark(Long id, ShopId shopId, MemberId memberId) {
        this.id = id;
        this.shopId = shopId;
        this.memberId = memberId;
    }

    public static ShopBookmark of(ShopId shopId, MemberId memberId) {
        return new ShopBookmark(null, shopId, memberId);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopBookmark reconstitute(Long id, ShopId shopId, MemberId memberId) {
        return new ShopBookmark(id, shopId, memberId);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }
}
