package com.tastyhouse.core.domain.order.application.dto.result;

import com.tastyhouse.core.domain.order.domain.model.OrderProductOption;

public record OrderProductOptionResult(
    Long id,
    String optionGroupName,
    String optionName,
    Integer additionalPrice
) {
    public static OrderProductOptionResult from(OrderProductOption option) {
        return new OrderProductOptionResult(
            option.getId(),
            option.getOptionGroupName(),
            option.getOptionName(),
            option.getAdditionalPrice()
        );
    }
}
