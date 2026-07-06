package com.tastyhouse.core.domain.product.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.domain.product.domain.vo.ProductCategoryId;
import com.tastyhouse.core.shared.entity.BaseEntity;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "PRODUCT_CATEGORY")
public class ProductCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    private ProductCategory(Long shopId, String name, Integer sort, boolean visible) {
        this.shopId = shopId;
        this.name = name;
        this.sort = sort;
        this.visible = visible;
    }

    public static ProductCategory of(Long shopId, String name, Integer sort, boolean visible) {
        return new ProductCategory(shopId, name, sort, visible);
    }

    public ProductCategoryId getProductCategoryId() {
        return ProductCategoryId.of(this.id);
    }

    public void update(String displayName, Integer sort, boolean visible) {
        this.name = displayName;
        this.sort = sort;
        this.visible = visible;
    }
}
