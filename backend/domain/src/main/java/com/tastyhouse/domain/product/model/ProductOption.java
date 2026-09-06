package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;

public class ProductOption {
    private final Long id;
    private final ProductOptionGroupId optionGroupId;
    private String name;
    private Integer additionalPrice;
    private Integer sort;
    private boolean soldOut;

    private LocalDateTime soldOutUntil;
    private boolean visible;

    private Integer cupCount;

    private Integer personalCupDiscountAmount;

    private ProductOption(
        Long id,
        ProductOptionGroupId optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible,
        Integer cupCount,
        Integer personalCupDiscountAmount
    ) {
        this.id = id;
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

    public static ProductOption of(
        ProductOptionGroupId optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible,
        Integer cupCount,
        Integer personalCupDiscountAmount
    ) {
        return new ProductOption(
            null,
            optionGroupId,
            name,
            additionalPrice != null ? additionalPrice : 0,
            sort,
            soldOut,
            soldOutUntil,
            visible,
            cupCount,
            personalCupDiscountAmount
        );
    }

    public static ProductOption reconstitute(
        Long id,
        ProductOptionGroupId optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible,
        Integer cupCount,
        Integer personalCupDiscountAmount
    ) {
        return new ProductOption(
            id,
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

    public ProductOptionId getProductOptionId() {
        return ProductOptionId.of(this.id);
    }

    public void update(
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        boolean visible,
        Integer cupCount,
        Integer personalCupDiscountAmount
    ) {
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.sort = sort;
        this.soldOut = soldOut;
        this.visible = visible;
        this.cupCount = cupCount;
        this.personalCupDiscountAmount = personalCupDiscountAmount;

        if (!soldOut) {
            this.soldOutUntil = null;
        }
    }

    public void markSoldOut() {
        this.soldOut = true;
    }

    public void markSoldOut(LocalDateTime soldOutUntil) {
        this.soldOut = true;
        this.soldOutUntil = soldOutUntil;
    }

    public void releaseSoldOut() {
        this.soldOut = false;
        this.soldOutUntil = null;
    }

    public void hide() {
        this.visible = false;
    }

    public void activate() {
        this.visible = true;
    }

    public void changeSoldOutUntil(LocalDateTime until) {
        if (!this.soldOut) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_SOLD_OUT);
        }
        this.soldOutUntil = until;
    }

    public Long getId() {
        return this.id;
    }

    public ProductOptionGroupId getOptionGroupId() {
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
