package com.tastyhouse.application.order.store;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.order.model.OrderProduct;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.order.port.out.write.OrderProductState;

final class OrderProductStateMapper {
    private OrderProductStateMapper() {
    }

    static OrderProduct toDomain(OrderProductState state) {
        return OrderProduct.reconstitute(
            state.id(),
            state.orderId() == null ? null : OrderId.of(state.orderId()),
            state.productId() == null ? null : ProductId.of(state.productId()),
            state.name(),
            state.priceName(),
            state.imageFileId() == null ? null : UploadedFileId.of(state.imageFileId()),
            state.quantity(),
            state.originalPrice(),
            state.discountPrice(),
            state.totalOptionPrice(),
            state.totalPrice(),
            state.cupDepositAmount()
        );
    }

    static OrderProductState toState(OrderProduct orderProduct) {
        return new OrderProductState(
            orderProduct.getId(),
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
}
