package com.tastyhouse.application.product.store;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroup;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionGroupState;

final class ProductCommonOptionGroupStateMapper {
    private ProductCommonOptionGroupStateMapper() {
    }

    static ProductCommonOptionGroup toDomain(ProductCommonOptionGroupState state) {
        return ProductCommonOptionGroup.reconstitute(
            state.id(),
            state.productId() == null ? null : ProductId.of(state.productId()),
            state.name(),
            state.description(),
            state.required(),
            state.multipleSelect(),
            state.minSelect(),
            state.maxSelect(),
            state.sort(),
            state.visible()
        );
    }

    static ProductCommonOptionGroupState toState(ProductCommonOptionGroup group) {
        return new ProductCommonOptionGroupState(
            group.getId(),
            group.getProductId() == null ? null : group.getProductId().value(),
            group.getName(),
            group.getDescription(),
            group.isRequired(),
            group.isMultipleSelect(),
            group.getMinSelect(),
            group.getMaxSelect(),
            group.getSort(),
            group.isVisible()
        );
    }
}
