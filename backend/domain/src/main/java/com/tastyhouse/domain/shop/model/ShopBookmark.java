package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shop.vo.ShopId;

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
