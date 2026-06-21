package com.tastyhouse.core.domain.product.domain.model;

import com.tastyhouse.core.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private Boolean isVisible;

    private ProductCategory(Long shopId, String name, Integer sort, Boolean isVisible) {
        this.shopId = shopId;
        this.name = name;
        this.sort = sort;
        this.isVisible = isVisible != null ? isVisible : true;
    }

    public static ProductCategory of(Long shopId, String name, Integer sort, Boolean isVisible) {
        return new ProductCategory(shopId, name, sort, isVisible);
    }

    public void update(String displayName, Integer sort, Boolean isVisible) {
        this.name = displayName;
        this.sort = sort;
        this.isVisible = isVisible;
    }
}
