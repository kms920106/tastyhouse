package com.tastyhouse.infrastructure.order.persistence;

import com.tastyhouse.application.order.port.out.write.OrderProductOptionState;

final class OrderProductOptionMapper {
    private OrderProductOptionMapper() {
    }

    static OrderProductOptionJpaEntity toEntity(OrderProductOptionState state) {
        return OrderProductOptionJpaEntity.create(
            state.orderProductId(),
            state.optionGroupId(),
            state.optionGroupName(),
            state.optionId(),
            state.optionName(),
            state.additionalPrice(),
            state.optionGroupType(),
            state.cupCount(),
            state.depositAmount()
        );
    }
}
