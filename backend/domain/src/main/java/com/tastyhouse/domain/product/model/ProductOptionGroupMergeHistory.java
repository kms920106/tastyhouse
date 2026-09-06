package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ProductOptionGroupMergeHistory {
    private final Long id;
    private final ShopId shopId;
    private final ProductOptionGroupId baseOptionGroupId;
    private final ProductOptionGroupId mergedOptionGroupId;
    private final String mergedGroupName;
    private final ProductOptionGroupMergeEntryType entryType;
    private final CeoId actorCeoId;

    private ProductOptionGroupMergeHistory(
        Long id,
        ShopId shopId,
        ProductOptionGroupId baseOptionGroupId,
        ProductOptionGroupId mergedOptionGroupId,
        String mergedGroupName,
        ProductOptionGroupMergeEntryType entryType,
        CeoId actorCeoId
    ) {
        this.id = id;
        this.shopId = shopId;
        this.baseOptionGroupId = baseOptionGroupId;
        this.mergedOptionGroupId = mergedOptionGroupId;
        this.mergedGroupName = mergedGroupName;
        this.entryType = entryType;
        this.actorCeoId = actorCeoId;
    }

    public static ProductOptionGroupMergeHistory of(
        ShopId shopId,
        ProductOptionGroupId baseOptionGroupId,
        ProductOptionGroupId mergedOptionGroupId,
        String mergedGroupName,
        ProductOptionGroupMergeEntryType entryType,
        CeoId actorCeoId
    ) {
        return new ProductOptionGroupMergeHistory(
            null, shopId, baseOptionGroupId, mergedOptionGroupId, mergedGroupName, entryType, actorCeoId
        );
    }

    public static ProductOptionGroupMergeHistory reconstitute(
        Long id,
        ShopId shopId,
        ProductOptionGroupId baseOptionGroupId,
        ProductOptionGroupId mergedOptionGroupId,
        String mergedGroupName,
        ProductOptionGroupMergeEntryType entryType,
        CeoId actorCeoId
    ) {
        return new ProductOptionGroupMergeHistory(
            id, shopId, baseOptionGroupId, mergedOptionGroupId, mergedGroupName, entryType, actorCeoId
        );
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ProductOptionGroupId getBaseOptionGroupId() {
        return this.baseOptionGroupId;
    }

    public ProductOptionGroupId getMergedOptionGroupId() {
        return this.mergedOptionGroupId;
    }

    public String getMergedGroupName() {
        return this.mergedGroupName;
    }

    public ProductOptionGroupMergeEntryType getEntryType() {
        return this.entryType;
    }

    public CeoId getActorCeoId() {
        return this.actorCeoId;
    }
}
