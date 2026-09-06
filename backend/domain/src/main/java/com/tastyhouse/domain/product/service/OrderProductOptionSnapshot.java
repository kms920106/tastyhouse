package com.tastyhouse.domain.product.service;

import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;

public record OrderProductOptionSnapshot(
    ProductOptionGroupId optionGroupId,
    String optionGroupName,
    ProductOptionId optionId,
    String optionName,
    int additionalPrice,
    String optionGroupType,
    Integer cupCount,
    int depositAmount,
    int personalCupDiscountAmount
) {
}
