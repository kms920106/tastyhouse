package com.tastyhouse.application.product.store;

import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupLinkState;

final class ProductOptionGroupLinkStateMapper {
    private ProductOptionGroupLinkStateMapper() {
    }

    static ProductOptionGroupLink toDomain(ProductOptionGroupLinkState state) {
        return ProductOptionGroupLink.reconstitute(
            state.id(),
            state.productId() == null ? null : ProductId.of(state.productId()),
            state.optionGroupId() == null ? null : ProductOptionGroupId.of(state.optionGroupId()),
            state.sort()
        );
    }

    static ProductOptionGroupLinkState toState(ProductOptionGroupLink link) {
        return new ProductOptionGroupLinkState(
            link.getId(),
            link.getProductId() == null ? null : link.getProductId().value(),
            link.getOptionGroupId() == null ? null : link.getOptionGroupId().value(),
            link.getSort()
        );
    }
}
