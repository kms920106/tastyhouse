package com.tastyhouse.infrastructure.order.persistence;

import com.tastyhouse.domain.order.model.OrderProductOption;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class OrderProductOptionMapper {
    private OrderProductOptionMapper() {
    }

    static OrderProductOptionJpaEntity toEntity(OrderProductOption domain) {
        return OrderProductOptionJpaEntity.create(
            IdMapping.raw(domain.getOrderProductId(), OrderProductId::value),
            IdMapping.raw(domain.getOptionGroupId(), ProductOptionGroupId::value),
            domain.getOptionGroupName(),
            IdMapping.raw(domain.getOptionId(), ProductOptionId::value),
            domain.getOptionName(),
            domain.getAdditionalPrice(),
            domain.getOptionGroupType(),
            domain.getCupCount(),
            domain.getDepositAmount()
        );
    }
}
