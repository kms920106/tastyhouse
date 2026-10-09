package com.tastyhouse.infrastructure.jpa.order.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.order.model.OrderProduct;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;

final class OrderProductMapper {

    private OrderProductMapper() {
    }

    static OrderProduct toDomain(OrderProductJpaEntity entity) {
        return OrderProduct.reconstitute(
            entity.getId(),
            entity.getOrderId() == null ? null : OrderId.of(entity.getOrderId()),
            entity.getProductId() == null ? null : ProductId.of(entity.getProductId()),
            entity.getName(),
            entity.getPriceName(),
            entity.getImageFileId() == null ? null : UploadedFileId.of(entity.getImageFileId()),
            entity.getQuantity(),
            entity.getOriginalPrice(),
            entity.getDiscountPrice(),
            entity.getTotalOptionPrice(),
            entity.getTotalPrice(),
            entity.getCupDepositAmount()
        );
    }

    static OrderProductJpaEntity toEntity(OrderProduct orderProduct) {
        return OrderProductJpaEntity.create(
            orderProduct.getOrderId() == null ? null : orderProduct.getOrderId().value(),
            orderProduct.getProductId() == null ? null : orderProduct.getProductId().value(),
            orderProduct.getName(),
            orderProduct.getPriceName(),
            orderProduct.getImageFileId() == null ? null : orderProduct.getImageFileId().value(),
            orderProduct.getQuantity(),
            orderProduct.getOriginalPrice(),
            orderProduct.getDiscountPrice(),
            orderProduct.getTotalOptionPrice(),
            orderProduct.getTotalPrice(),
            orderProduct.getCupDepositAmount()
        );
    }

    static void applyChanges(OrderProductJpaEntity entity, OrderProduct orderProduct) {
        entity.applyChanges(
            orderProduct.getTotalOptionPrice(),
            orderProduct.getTotalPrice(),
            orderProduct.getCupDepositAmount()
        );
    }
}
