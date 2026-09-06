package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

public class ProductOptionGroupLink {
    private final Long id;
    private final ProductId productId;
    private final ProductOptionGroupId optionGroupId;
    private Integer sort;

    private ProductOptionGroupLink(
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

    public static ProductOptionGroupLink of(
        ProductId productId,
        ProductOptionGroupId optionGroupId,
        Integer sort
    ) {
        return new ProductOptionGroupLink(null, productId, optionGroupId, sort);
    }

    public static ProductOptionGroupLink reconstitute(
        Long id,
        ProductId productId,
        ProductOptionGroupId optionGroupId,
        Integer sort
    ) {
        return new ProductOptionGroupLink(id, productId, optionGroupId, sort);
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

    public void changeSort(Integer sort) {
        this.sort = sort;
    }
}
