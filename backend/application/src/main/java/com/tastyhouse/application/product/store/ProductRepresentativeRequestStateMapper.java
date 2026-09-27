package com.tastyhouse.application.product.store;

import com.tastyhouse.application.product.port.out.write.ProductRepresentativeRequestState;
import com.tastyhouse.domain.product.model.ProductRepresentativeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ProductRepresentativeRequestStateMapper {
    private ProductRepresentativeRequestStateMapper() {
    }

    static ProductRepresentativeRequest toDomain(ProductRepresentativeRequestState state) {
        return ProductRepresentativeRequest.reconstitute(
            state.id(),
            state.productId() == null ? null : ProductId.of(state.productId()),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.status() == null ? null : ApprovalStatus.valueOf(state.status()),
            state.rejectReason(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ProductRepresentativeRequestState toState(ProductRepresentativeRequest request) {
        return new ProductRepresentativeRequestState(
            request.getId(),
            request.getProductId() == null ? null : request.getProductId().value(),
            request.getShopId() == null ? null : request.getShopId().value(),
            request.getStatus() == null ? null : request.getStatus().name(),
            request.getRejectReason(),
            request.getCreatedAt(),
            request.getUpdatedAt()
        );
    }
}
