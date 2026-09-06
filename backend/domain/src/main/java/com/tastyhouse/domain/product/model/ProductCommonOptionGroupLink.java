package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

public class ProductCommonOptionGroupLink {
    private final Long id;
    private final ProductId productId;
    private final ProductOptionGroupId optionGroupId;
    private final Integer sort;

    private ProductCommonOptionGroupLink(
        Long id,
        ProductId productId,
        ProductOptionGroupId optionGroupId,
        Integer sort
    ) {
        this.id = id;
        this.productId = productId;
        this.optionGroupId = optionGroupId;
        this.sort = sort;
    }

    public static ProductCommonOptionGroupLink of(
        ProductId productId,
        ProductOptionGroupId optionGroupId,
        Integer sort
    ) {
        return new ProductCommonOptionGroupLink(null, productId, optionGroupId, sort);
    }

    public static ProductCommonOptionGroupLink reconstitute(
        Long id,
        ProductId productId,
        ProductOptionGroupId optionGroupId,
        Integer sort
    ) {
        return new ProductCommonOptionGroupLink(id, productId, optionGroupId, sort);
    }

    public Long getId() {
        return this.id;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public ProductOptionGroupId getOptionGroupId() {
        return this.optionGroupId;
    }

    public Integer getSort() {
        return this.sort;
    }
}
