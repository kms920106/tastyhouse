package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.product.vo.BbqCategoryId;
import com.tastyhouse.domain.product.vo.BbqMenuId;
import com.tastyhouse.domain.product.vo.ProductId;

public class ProductBbq {
    private final Long id;
    private final ProductId productId;
    private final BbqMenuId bbqMenuId;
    private final BbqCategoryId bbqCategoryId;
    private boolean optionsSynced;

    private ProductBbq(Long id, ProductId productId, BbqMenuId bbqMenuId, BbqCategoryId bbqCategoryId, boolean optionsSynced) {
        this.id = id;
        this.productId = productId;
        this.bbqMenuId = bbqMenuId;
        this.bbqCategoryId = bbqCategoryId;
        this.optionsSynced = optionsSynced;
    }

    public static ProductBbq of(ProductId productId, BbqMenuId bbqMenuId, BbqCategoryId bbqCategoryId, boolean optionsSynced) {
        return new ProductBbq(null, productId, bbqMenuId, bbqCategoryId, optionsSynced);
    }

    public static ProductBbq reconstitute(
        Long id,
        ProductId productId,
        BbqMenuId bbqMenuId,
        BbqCategoryId bbqCategoryId,
        boolean optionsSynced
    ) {
        return new ProductBbq(id, productId, bbqMenuId, bbqCategoryId, optionsSynced);
    }

    public void markOptionsSynced() {
        this.optionsSynced = true;
    }

    public Long getId() {
        return this.id;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public BbqMenuId getBbqMenuId() {
        return this.bbqMenuId;
    }

    public BbqCategoryId getBbqCategoryId() {
        return this.bbqCategoryId;
    }

    public boolean isOptionsSynced() {
        return this.optionsSynced;
    }
}
