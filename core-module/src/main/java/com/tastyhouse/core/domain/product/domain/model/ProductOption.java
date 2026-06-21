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
@Table(name = "PRODUCT_OPTION")
public class ProductOption extends BaseEntity {

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

    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible;

    private ProductOption(
        Long optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        Boolean isSoldOut,
        Boolean isVisible
    ) {
        this.optionGroupId = optionGroupId;
        this.name = name;
        this.additionalPrice = additionalPrice != null ? additionalPrice : 0;
        this.sort = sort;
        this.isSoldOut = isSoldOut != null ? isSoldOut : false;
        this.isVisible = isVisible != null ? isVisible : true;
    }

    public static ProductOption of(
        Long optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        Boolean isSoldOut,
        Boolean isVisible
    ) {
        return new ProductOption(optionGroupId, name, additionalPrice, sort, isSoldOut, isVisible);
    }

    public void update(String name, Integer additionalPrice, Integer sort, Boolean isSoldOut, Boolean isVisible) {
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.sort = sort;
        this.isSoldOut = isSoldOut;
        this.isVisible = isVisible;
    }
}
