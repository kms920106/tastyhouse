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
@Table(name = "PRODUCT_COMMON_OPTION")
public class ProductCommonOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "option_group_id", nullable = false)
    private Long optionGroupId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "additional_price", nullable = false)
    private Integer additionalPrice;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_sold_out", nullable = false)
    private Boolean isSoldOut;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    private ProductCommonOption(
        Long optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        Boolean isSoldOut,
        Boolean isActive
    ) {
        this.optionGroupId = optionGroupId;
        this.name = name;
        this.additionalPrice = additionalPrice != null ? additionalPrice : 0;
        this.sort = sort;
        this.isSoldOut = isSoldOut != null ? isSoldOut : false;
        this.isActive = isActive != null ? isActive : true;
    }

    public static ProductCommonOption of(
        Long optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        Boolean isSoldOut,
        Boolean isActive
    ) {
        return new ProductCommonOption(optionGroupId, name, additionalPrice, sort, isSoldOut, isActive);
    }

    public void update(String name, Integer additionalPrice, Integer sort, Boolean isSoldOut, Boolean isActive) {
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.sort = sort;
        this.isSoldOut = isSoldOut;
        this.isActive = isActive;
    }
}
