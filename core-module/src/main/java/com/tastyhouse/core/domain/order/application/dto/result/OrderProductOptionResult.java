package com.tastyhouse.core.domain.order.application.dto.result;

import com.tastyhouse.core.domain.order.domain.model.OrderItemOption;

public record OrderItemOptionResult(
    Long id,
    String optionGroupName,
    String optionName,
    Integer additionalPrice
) {
    public static OrderItemOptionResult from(OrderItemOption option) {
        return new OrderItemOptionResult(
            option.getId(),
            option.getOptionGroupName(),
            option.getOptionName(),
            option.getAdditionalPrice()
        );
    }
}
