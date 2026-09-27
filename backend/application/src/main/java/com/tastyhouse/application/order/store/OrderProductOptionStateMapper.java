package com.tastyhouse.application.order.store;

import com.tastyhouse.domain.order.model.OrderProductOption;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.application.order.port.out.write.OrderProductOptionState;

final class OrderProductOptionStateMapper {
    private OrderProductOptionStateMapper() {
    }

    static OrderProductOption toDomain(OrderProductOptionState state) {
        return OrderProductOption.reconstitute(
            state.id(),
            state.orderProductId() == null ? null : OrderProductId.of(state.orderProductId()),
            state.optionGroupId() == null ? null : ProductOptionGroupId.of(state.optionGroupId()),
            state.optionGroupName(),
            state.optionId() == null ? null : ProductOptionId.of(state.optionId()),
            state.optionName(),
            state.additionalPrice(),
            state.optionGroupType(),
            state.cupCount(),
            state.depositAmount()
        );
    }

    static OrderProductOptionState toState(OrderProductOption option) {
        return new OrderProductOptionState(
            option.getId(),
            option.getOrderProductId() == null ? null : option.getOrderProductId().value(),
            option.getOptionGroupId() == null ? null : option.getOptionGroupId().value(),
            option.getOptionGroupName(),
            option.getOptionId() == null ? null : option.getOptionId().value(),
            option.getOptionName(),
            option.getAdditionalPrice(),
            option.getOptionGroupType(),
            option.getCupCount(),
            option.getDepositAmount()
        );
    }
}
