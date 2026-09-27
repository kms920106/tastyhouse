package com.tastyhouse.application.product.store;

import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.application.product.port.out.write.ProductOptionState;

final class ProductOptionStateMapper {
    private ProductOptionStateMapper() {
    }

    static ProductOption toDomain(ProductOptionState state) {
        return ProductOption.reconstitute(
            state.id(),
            state.optionGroupId() == null ? null : ProductOptionGroupId.of(state.optionGroupId()),
            state.name(),
            state.additionalPrice(),
            state.sort(),
            state.soldOut(),
            state.soldOutUntil(),
            state.visible(),
            state.cupCount(),
            state.personalCupDiscountAmount()
        );
    }

    static ProductOptionState toState(ProductOption option) {
        return new ProductOptionState(
            option.getId(),
            option.getOptionGroupId() == null ? null : option.getOptionGroupId().value(),
            option.getName(),
            option.getAdditionalPrice(),
            option.getSort(),
            option.isSoldOut(),
            option.getSoldOutUntil(),
            option.isVisible(),
            option.getCupCount(),
            option.getPersonalCupDiscountAmount()
        );
    }
}
