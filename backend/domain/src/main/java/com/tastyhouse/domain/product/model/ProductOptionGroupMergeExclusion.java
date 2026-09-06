package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ProductOptionGroupMergeExclusion {
    private final Long id;
    private final ShopId shopId;
    private final String groupSignature;
    private final CeoId actorCeoId;

    private ProductOptionGroupMergeExclusion(
        Long id,
        ShopId shopId,
        String groupSignature,
        CeoId actorCeoId
    ) {
        this.id = id;
        this.shopId = shopId;
        this.groupSignature = groupSignature;
        this.actorCeoId = actorCeoId;
    }

    public static ProductOptionGroupMergeExclusion of(
        ShopId shopId,
        String groupSignature,
        CeoId actorCeoId
    ) {
        return new ProductOptionGroupMergeExclusion(null, shopId, groupSignature, actorCeoId);
    }

    public static ProductOptionGroupMergeExclusion reconstitute(
        Long id,
        ShopId shopId,
        String groupSignature,
        CeoId actorCeoId
    ) {
        return new ProductOptionGroupMergeExclusion(id, shopId, groupSignature, actorCeoId);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public String getGroupSignature() {
        return this.groupSignature;
    }

    public CeoId getActorCeoId() {
        return this.actorCeoId;
    }
}
