package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "PRODUCT_CATEGORY")
public class ProductCategoryJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    protected ProductCategoryJpaEntity() {
    }

    private ProductCategoryJpaEntity(
        Long shopId,
        String name,
        String description,
        Integer sort,
        boolean visible
    ) {
        this.shopId = shopId;
        this.name = name;
        this.description = description;
        this.sort = sort;
        this.visible = visible;
    }

    static ProductCategoryJpaEntity create(
        Long shopId,
        String name,
        String description,
        Integer sort,
        boolean visible
    ) {
        return new ProductCategoryJpaEntity(shopId, name, description, sort, visible);
    }

    void applyChanges(String name, String description, Integer sort, boolean visible) {
        this.name = name;
        this.description = description;
        this.sort = sort;
        this.visible = visible;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
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
