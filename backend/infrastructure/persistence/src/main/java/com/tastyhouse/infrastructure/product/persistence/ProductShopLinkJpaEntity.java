package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "PRODUCT_SHOP_LINK")
public class ProductShopLinkJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "product_category_id")
    private Long productCategoryId;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    protected ProductShopLinkJpaEntity() {
    }

    private ProductShopLinkJpaEntity(Long productId, Long shopId, Long productCategoryId, Integer sort) {
        this.productId = productId;
        this.shopId = shopId;
        this.productCategoryId = productCategoryId;
        this.sort = sort;
    }

    static ProductShopLinkJpaEntity create(Long productId, Long shopId, Long productCategoryId, Integer sort) {
        return new ProductShopLinkJpaEntity(productId, shopId, productCategoryId, sort);
    }

    void applyChanges(Long productCategoryId, Integer sort) {
        this.productCategoryId = productCategoryId;
        this.sort = sort;
    }

    public Long getId() {
        return this.id;
    }

    public Long getProductId() {
        return this.productId;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getProductCategoryId() {
        return this.productCategoryId;
    }

    public Integer getSort() {
        return this.sort;
    }
}
