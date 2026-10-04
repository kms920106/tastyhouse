package com.tastyhouse.infrastructure.persistence.order.persistence;

import com.tastyhouse.domain.order.model.OrderProductOption;

final class OrderProductOptionMapper {

    private OrderProductOptionMapper() {
    }

    static OrderProductOptionJpaEntity toEntity(OrderProductOption option) {
        return OrderProductOptionJpaEntity.create(
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
