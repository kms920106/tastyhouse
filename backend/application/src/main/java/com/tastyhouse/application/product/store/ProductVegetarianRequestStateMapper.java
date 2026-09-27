package com.tastyhouse.application.product.store;

import com.tastyhouse.domain.product.model.ProductVegetarianRequest;
import com.tastyhouse.domain.product.model.VegetarianType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.application.product.port.out.write.ProductVegetarianRequestState;

final class ProductVegetarianRequestStateMapper {
    private ProductVegetarianRequestStateMapper() {
    }

    static ProductVegetarianRequest toDomain(ProductVegetarianRequestState state) {
        return ProductVegetarianRequest.reconstitute(
            state.id(),
            state.productId() == null ? null : ProductId.of(state.productId()),
            state.vegetarianType() == null ? null : VegetarianType.valueOf(state.vegetarianType()),
            state.ingredients(),
            state.description(),
            state.status() == null ? null : ApprovalStatus.valueOf(state.status()),
            state.rejectReason(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ProductVegetarianRequestState toState(ProductVegetarianRequest request) {
        return new ProductVegetarianRequestState(
            request.getId(),
            request.getProductId() == null ? null : request.getProductId().value(),
            request.getVegetarianType() == null ? null : request.getVegetarianType().name(),
            request.getIngredients(),
            request.getDescription(),
            request.getStatus() == null ? null : request.getStatus().name(),
            request.getRejectReason(),
            request.getCreatedAt(),
            request.getUpdatedAt()
        );
    }
}
