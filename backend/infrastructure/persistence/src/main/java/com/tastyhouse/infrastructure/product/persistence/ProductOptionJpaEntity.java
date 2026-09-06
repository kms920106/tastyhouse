package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "PRODUCT_OPTION")
public class ProductOptionJpaEntity extends BaseEntity {
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
    private boolean soldOut;

    @Column(name = "sold_out_until")
    private LocalDateTime soldOutUntil;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    @Column(name = "cup_count")
    private Integer cupCount;

    @Column(name = "personal_cup_discount_amount")
    private Integer personalCupDiscountAmount;

    protected ProductOptionJpaEntity() {
    }

    private ProductOptionJpaEntity(
        Long optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible,
        Integer cupCount,
        Integer personalCupDiscountAmount
    ) {
        this.optionGroupId = optionGroupId;
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.sort = sort;
        this.soldOut = soldOut;
        this.soldOutUntil = soldOutUntil;
        this.visible = visible;
        this.cupCount = cupCount;
        this.personalCupDiscountAmount = personalCupDiscountAmount;
    }

    static ProductOptionJpaEntity create(
        Long optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible,
        Integer cupCount,
        Integer personalCupDiscountAmount
    ) {
        return new ProductOptionJpaEntity(
            optionGroupId,
            name,
            additionalPrice,
            sort,
            soldOut,
            soldOutUntil,
            visible,
            cupCount,
            personalCupDiscountAmount
        );
    }

    void applyChanges(
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible,
        Integer cupCount,
        Integer personalCupDiscountAmount
    ) {
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.sort = sort;
        this.soldOut = soldOut;
        this.soldOutUntil = soldOutUntil;
        this.visible = visible;
        this.cupCount = cupCount;
        this.personalCupDiscountAmount = personalCupDiscountAmount;
    }

    public Long getId() {
        return this.id;
    }

    public Long getOptionGroupId() {
        return this.optionGroupId;
    }

    public String getName() {
        return this.name;
    }

    public Integer getAdditionalPrice() {
        return this.additionalPrice;
    }

    public Integer getSort() {
        return this.sort;
    }

    public boolean isSoldOut() {
        return this.soldOut;
    }

    public LocalDateTime getSoldOutUntil() {
        return this.soldOutUntil;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public Integer getCupCount() {
        return this.cupCount;
    }

    public Integer getPersonalCupDiscountAmount() {
        return this.personalCupDiscountAmount;
    }
}
