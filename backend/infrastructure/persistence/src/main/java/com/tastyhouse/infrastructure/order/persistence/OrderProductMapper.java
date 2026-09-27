package com.tastyhouse.infrastructure.order.persistence;

import com.tastyhouse.application.order.port.out.write.OrderProductState;

final class OrderProductMapper {
    private OrderProductMapper() {
    }

    static OrderProductState toState(OrderProductJpaEntity entity) {
        return new OrderProductState(
            entity.getId(),
            entity.getOrderId(),
            entity.getProductId(),
            entity.getName(),
            entity.getPriceName(),
            entity.getImageFileId(),
            entity.getQuantity(),
            entity.getOriginalPrice(),
            entity.getDiscountPrice(),
            entity.getTotalOptionPrice(),
            entity.getTotalPrice(),
            entity.getCupDepositAmount()
        );
    }

    static OrderProductJpaEntity toEntity(OrderProductState state) {
        return OrderProductJpaEntity.create(
            state.orderId(),
            state.productId(),
            state.name(),
            state.priceName(),
            state.imageFileId(),
            state.quantity(),
            state.originalPrice(),
            state.discountPrice(),
            state.totalOptionPrice(),
            state.totalPrice(),
            state.cupDepositAmount()
        );
    }

    static void applyChanges(OrderProductJpaEntity entity, OrderProductState state) {
        entity.applyChanges(
            state.totalOptionPrice(),
            state.totalPrice(),
            state.cupDepositAmount()
        );
    }
}
