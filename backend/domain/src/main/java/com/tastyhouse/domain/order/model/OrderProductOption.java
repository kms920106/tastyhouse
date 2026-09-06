package com.tastyhouse.domain.order.model;

import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;

public class OrderProductOption {
    private final Long id;
    private final OrderProductId orderProductId;
    private final ProductOptionGroupId optionGroupId;
    private final String optionGroupName;
    private final ProductOptionId optionId;
    private final String optionName;
    private final Integer additionalPrice;

    private final String optionGroupType;

    private final Integer cupCount;

    private final Integer depositAmount;

    private OrderProductOption(
        Long id,
        OrderProductId orderProductId,
        ProductOptionGroupId optionGroupId,
        String optionGroupName,
        ProductOptionId optionId,
        String optionName,
        Integer additionalPrice,
        String optionGroupType,
        Integer cupCount,
        Integer depositAmount
    ) {
        this.id = id;
        this.orderProductId = orderProductId;
        this.optionGroupId = optionGroupId;
        this.optionGroupName = optionGroupName;
        this.optionId = optionId;
        this.optionName = optionName;
        this.additionalPrice = additionalPrice;
        this.optionGroupType = optionGroupType;
        this.cupCount = cupCount;
        this.depositAmount = depositAmount != null ? depositAmount : 0;
    }

    public static OrderProductOption of(
        OrderProductId orderProductId,
        ProductOptionGroupId optionGroupId,
        String optionGroupName,
        ProductOptionId optionId,
        String optionName,
        Integer additionalPrice,
        String optionGroupType,
        Integer cupCount,
        Integer depositAmount
    ) {
        return new OrderProductOption(
            null,
            orderProductId,
            optionGroupId,
            optionGroupName,
            optionId,
            optionName,
            additionalPrice != null ? additionalPrice : 0,
            optionGroupType,
            cupCount,
            depositAmount
        );
    }

    public static OrderProductOption reconstitute(
        Long id,
        OrderProductId orderProductId,
        ProductOptionGroupId optionGroupId,
        String optionGroupName,
        ProductOptionId optionId,
        String optionName,
        Integer additionalPrice,
        String optionGroupType,
        Integer cupCount,
        Integer depositAmount
    ) {
        return new OrderProductOption(
            id,
            orderProductId,
            optionGroupId,
            optionGroupName,
            optionId,
            optionName,
            additionalPrice,
            optionGroupType,
            cupCount,
            depositAmount
        );
    }

    public Long getId() {
        return this.id;
    }

    public OrderProductId getOrderProductId() {
        return this.orderProductId;
    }

    public ProductOptionGroupId getOptionGroupId() {
        return this.optionGroupId;
    }

    public String getOptionGroupName() {
        return this.optionGroupName;
    }

    public ProductOptionId getOptionId() {
        return this.optionId;
    }

    public String getOptionName() {
        return this.optionName;
    }

    public Integer getAdditionalPrice() {
        return this.additionalPrice;
    }

    public String getOptionGroupType() {
        return this.optionGroupType;
    }

    public Integer getCupCount() {
        return this.cupCount;
    }

    public Integer getDepositAmount() {
        return this.depositAmount;
    }
}
