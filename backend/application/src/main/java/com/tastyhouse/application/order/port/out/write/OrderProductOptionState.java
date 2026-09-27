package com.tastyhouse.application.order.port.out.write;

public record OrderProductOptionState(
    Long id,
    Long orderProductId,
    Long optionGroupId,
    String optionGroupName,
    Long optionId,
    String optionName,
    Integer additionalPrice,
    String optionGroupType,
    Integer cupCount,
    Integer depositAmount
) {
}
