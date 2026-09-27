package com.tastyhouse.application.product.store;

import com.tastyhouse.application.product.port.out.write.ProductBbqState;
import com.tastyhouse.domain.product.model.ProductBbq;
import com.tastyhouse.domain.product.vo.BbqCategoryId;
import com.tastyhouse.domain.product.vo.BbqMenuId;
import com.tastyhouse.domain.product.vo.ProductId;

final class ProductBbqStateMapper {
    private ProductBbqStateMapper() {
    }

    static ProductBbq toDomain(ProductBbqState state) {
        return ProductBbq.reconstitute(
            state.id(),
            state.productId() == null ? null : ProductId.of(state.productId()),
            state.bbqMenuId() == null ? null : BbqMenuId.of(state.bbqMenuId()),
            state.bbqCategoryId() == null ? null : BbqCategoryId.of(state.bbqCategoryId()),
            state.optionsSynced()
        );
    }

    static ProductBbqState toState(ProductBbq productBbq) {
        return new ProductBbqState(
            productBbq.getId(),
            productBbq.getProductId() == null ? null : productBbq.getProductId().value(),
            productBbq.getBbqMenuId() == null ? null : productBbq.getBbqMenuId().value(),
            productBbq.getBbqCategoryId() == null ? null : productBbq.getBbqCategoryId().value(),
            productBbq.isOptionsSynced()
        );
    }
}
