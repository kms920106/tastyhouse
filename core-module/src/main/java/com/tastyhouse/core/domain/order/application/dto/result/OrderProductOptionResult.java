package com.tastyhouse.core.domain.order.application.dto.result;

import com.tastyhouse.core.domain.order.domain.model.OrderProductOption;
import com.tastyhouse.core.domain.order.domain.vo.OrderProductOptionId;

public record OrderProductOptionResult(
    OrderProductOptionId orderProductOptionId,
    String optionGroupName,
    String optionName,
    Integer additionalPrice
) {
    public static OrderProductOptionResult from(OrderProductOption option) {
        return new OrderProductOptionResult(
            option.getOrderProductOptionId(),
            option.getOptionGroupName(),
            option.getOptionName(),
            option.getAdditionalPrice()
        );
    }
}
