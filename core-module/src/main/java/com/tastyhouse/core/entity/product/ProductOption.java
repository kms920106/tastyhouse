package com.tastyhouse.core.entity.product;

import com.tastyhouse.core.entity.BaseEntity;
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
    private Long id; // PK

    @Column(name = "option_group_id", nullable = false)
    private Long optionGroupId; // 옵션 그룹 ID (PRODUCT_OPTION_GROUP.id 참조)

    @Column(name = "name", nullable = false, length = 100)
    private String name; // 옵션명

    @Column(name = "additional_price", nullable = false)
    private Integer additionalPrice; // 추가 금액 (원)

    @Column(name = "sort", nullable = false)
    private Integer sort; // 정렬 순서

    @Column(name = "is_sold_out", nullable = false)
    private Boolean isSoldOut; // 품절 여부 (true: 품절)

    @Column(name = "is_active", nullable = false)
    private Boolean isActive; // 활성화 여부 (true: 활성)

    private ProductOption(
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

    public static ProductOption of(
        Long optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        Boolean isSoldOut,
        Boolean isActive
    ) {
        return new ProductOption(
            optionGroupId,
            name,
            additionalPrice,
            sort,
            isSoldOut,
            isActive
        );
    }

    public void update(
        String name,
        Integer additionalPrice,
        Integer sort,
        Boolean isSoldOut,
        Boolean isActive
    ) {
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.sort = sort;
        this.isSoldOut = isSoldOut;
        this.isActive = isActive;
    }
}
