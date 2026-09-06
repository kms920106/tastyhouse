package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ProductCategory {
    private final Long id;
    private final ShopId shopId;
    private String name;

    private String description;
    private Integer sort;
    private boolean visible;

    private ProductCategory(
        Long id,
        ShopId shopId,
        String name,
        String description,
        Integer sort,
        boolean visible
    ) {
        this.id = id;
        this.shopId = shopId;
        this.name = name;
        this.description = description;
        this.sort = sort;
        this.visible = visible;
    }

    public static ProductCategory of(
        ShopId shopId,
        String name,
        String description,
        Integer sort,
        boolean visible
    ) {
        return new ProductCategory(null, shopId, name, description, sort, visible);
    }

    public static ProductCategory reconstitute(
        Long id,
        ShopId shopId,
        String name,
        String description,
        Integer sort,
        boolean visible
    ) {
        return new ProductCategory(id, shopId, name, description, sort, visible);
    }

    public ProductCategoryId getProductCategoryId() {
        return ProductCategoryId.of(this.id);
    }

    public void update(String displayName, Integer sort, boolean visible) {
        this.name = displayName;
        this.sort = sort;
        this.visible = visible;
    }

    public void changeDetails(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void changeSort(Integer sort) {
        this.sort = sort;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Integer getSort() {
        return this.sort;
    }

    public boolean isVisible() {
        return this.visible;
    }
}
