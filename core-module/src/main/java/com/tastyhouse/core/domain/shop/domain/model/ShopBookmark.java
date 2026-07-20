package com.tastyhouse.core.domain.shop.domain.model;

import lombok.Getter;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

/**
 * 상점 북마크 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopBookmarkJpaEntity} + {@code ShopBookmarkMapper}가 담당한다.
 */
@Getter
public class ShopBookmark {

    private final Long id;
    private final Long shopId;
    private final MemberId memberId;

    private ShopBookmark(Long id, Long shopId, MemberId memberId) {
        this.id = id;
        this.shopId = shopId;
        this.memberId = memberId;
    }

    public static ShopBookmark of(Long shopId, MemberId memberId) {
        return new ShopBookmark(null, shopId, memberId);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopBookmark reconstitute(Long id, Long shopId, MemberId memberId) {
        return new ShopBookmark(id, shopId, memberId);
    }
}
