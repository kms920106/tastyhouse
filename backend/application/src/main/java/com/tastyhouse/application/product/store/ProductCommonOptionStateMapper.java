package com.tastyhouse.application.product.store;

import com.tastyhouse.domain.product.model.ProductCommonOption;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionState;

final class ProductCommonOptionStateMapper {
    private ProductCommonOptionStateMapper() {
    }

    static ProductCommonOption toDomain(ProductCommonOptionState state) {
        return ProductCommonOption.reconstitute(
            state.id(),
            state.optionGroupId() == null ? null : ProductOptionGroupId.of(state.optionGroupId()),
            state.name(),
            state.additionalPrice(),
            state.sort(),
            state.soldOut(),
            state.soldOutUntil(),
            state.visible()
        );
    }

    static ProductCommonOptionState toState(ProductCommonOption option) {
        return new ProductCommonOptionState(
            option.getId(),
            option.getOptionGroupId() == null ? null : option.getOptionGroupId().value(),
            option.getName(),
            option.getAdditionalPrice(),
            option.getSort(),
            option.isSoldOut(),
            option.getSoldOutUntil(),
            option.isVisible()
        );
    }
}
