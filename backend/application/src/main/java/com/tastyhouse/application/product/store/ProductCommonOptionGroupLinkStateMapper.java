package com.tastyhouse.application.product.store;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroupLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionGroupLinkState;

final class ProductCommonOptionGroupLinkStateMapper {
    private ProductCommonOptionGroupLinkStateMapper() {
    }

    static ProductCommonOptionGroupLink toDomain(ProductCommonOptionGroupLinkState state) {
        return ProductCommonOptionGroupLink.reconstitute(
            state.id(),
            state.productId() == null ? null : ProductId.of(state.productId()),
            state.optionGroupId() == null ? null : ProductOptionGroupId.of(state.optionGroupId()),
            state.sort()
        );
    }

    static ProductCommonOptionGroupLinkState toState(ProductCommonOptionGroupLink link) {
        return new ProductCommonOptionGroupLinkState(
            link.getId(),
            link.getProductId() == null ? null : link.getProductId().value(),
            link.getOptionGroupId() == null ? null : link.getOptionGroupId().value(),
            link.getSort()
        );
    }
}
