package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ProductShopLink {
    private final Long id;
    private final ProductId productId;
    private final ShopId shopId;

    private ProductCategoryId productCategoryId;

    private Integer sort;

    private ProductShopLink(
        Long id,
        ProductId productId,
        ShopId shopId,
        ProductCategoryId productCategoryId,
        Integer sort
    ) {
        this.id = id;
        this.productId = productId;
        this.shopId = shopId;
        this.productCategoryId = productCategoryId;
        this.sort = sort;
    }

    public static ProductShopLink of(
        ProductId productId,
        ShopId shopId,
        ProductCategoryId productCategoryId,
        Integer sort
    ) {
        return new ProductShopLink(null, productId, shopId, productCategoryId, sort);
    }

    public static ProductShopLink reconstitute(
        Long id,
        ProductId productId,
        ShopId shopId,
        ProductCategoryId productCategoryId,
        Integer sort
    ) {
        return new ProductShopLink(id, productId, shopId, productCategoryId, sort);
    }

    public Long getId() {
        return this.id;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ProductCategoryId getProductCategoryId() {
        return this.productCategoryId;
    }

    public Integer getSort() {
        return this.sort;
    }

    public void relocate(ProductCategoryId productCategoryId, Integer sort) {
        this.productCategoryId = productCategoryId;
        this.sort = sort;
    }
}
