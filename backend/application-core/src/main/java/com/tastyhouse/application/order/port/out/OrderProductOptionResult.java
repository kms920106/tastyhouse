package com.tastyhouse.application.order.port.out;

public record OrderProductOptionResult(
    Long orderProductId,
    Long orderProductOptionId,
    String optionGroupName,
    String optionName,
    Integer additionalPrice,
    String optionGroupType,
    Integer cupCount,
    Integer depositAmount
) {
}
