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

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    private ProductCategory(Long shopId, String name, Integer sort, Boolean isActive) {
        this.shopId = shopId;
        this.name = name;
        this.sort = sort;
        this.isActive = isActive != null ? isActive : true;
    }

    public static ProductCategory of(Long shopId, String name, Integer sort, Boolean isActive) {
        return new ProductCategory(shopId, name, sort, isActive);
    }

    public void update(String displayName, Integer sort, Boolean isActive) {
        this.name = displayName;
        this.sort = sort;
        this.isActive = isActive;
    }
}
