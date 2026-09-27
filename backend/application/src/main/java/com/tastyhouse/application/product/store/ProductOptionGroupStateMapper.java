package com.tastyhouse.application.product.store;

import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupState;

final class ProductOptionGroupStateMapper {
    private ProductOptionGroupStateMapper() {
    }

    static ProductOptionGroup toDomain(ProductOptionGroupState state) {
        return ProductOptionGroup.reconstitute(
            state.id(),
            state.productId() == null ? null : ProductId.of(state.productId()),
            state.name(),
            state.description(),
            state.required(),
            state.multipleSelect(),
            state.minSelect(),
            state.maxSelect(),
            state.sort(),
            state.visible(),
            state.groupType() == null ? null : ProductOptionGroupType.valueOf(state.groupType())
        );
    }

    static ProductOptionGroupState toState(ProductOptionGroup group) {
        return new ProductOptionGroupState(
            group.getId(),
            group.getProductId() == null ? null : group.getProductId().value(),
            group.getName(),
            group.getDescription(),
            group.isRequired(),
            group.isMultipleSelect(),
            group.getMinSelect(),
            group.getMaxSelect(),
            group.getSort(),
            group.isVisible(),
            group.getGroupType() == null ? null : group.getGroupType().name()
        );
    }
}
