package com.tastyhouse.infrastructure.order.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.order.model.OrderProduct;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class OrderProductMapper {
    private OrderProductMapper() {
    }

    static OrderProduct toDomain(OrderProductJpaEntity entity) {
        return OrderProduct.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getOrderId(), OrderId::of),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            entity.getName(),
            entity.getPriceName(),
            IdMapping.vo(entity.getImageFileId(), UploadedFileId::of),
            entity.getQuantity(),
            entity.getOriginalPrice(),
            entity.getDiscountPrice(),
            entity.getTotalOptionPrice(),
            entity.getTotalPrice(),
            entity.getCupDepositAmount()
        );
    }

    static OrderProductJpaEntity toEntity(OrderProduct domain) {
        return OrderProductJpaEntity.create(
            IdMapping.raw(domain.getOrderId(), OrderId::value),
            IdMapping.raw(domain.getProductId(), ProductId::value),
            domain.getName(),
            domain.getPriceName(),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getQuantity(),
            domain.getOriginalPrice(),
            domain.getDiscountPrice(),
            domain.getTotalOptionPrice(),
            domain.getTotalPrice(),
            domain.getCupDepositAmount()
        );
    }

    static void applyChanges(OrderProductJpaEntity entity, OrderProduct domain) {
        entity.applyChanges(
            domain.getTotalOptionPrice(),
            domain.getTotalPrice(),
            domain.getCupDepositAmount()
        );
    }
}
