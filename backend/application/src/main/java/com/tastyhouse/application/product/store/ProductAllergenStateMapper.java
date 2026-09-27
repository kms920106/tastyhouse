package com.tastyhouse.application.product.store;

import com.tastyhouse.domain.product.model.AllergenType;
import com.tastyhouse.domain.product.model.ProductAllergen;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.out.write.ProductAllergenState;

final class ProductAllergenStateMapper {
    private ProductAllergenStateMapper() {
    }

    static ProductAllergen toDomain(ProductAllergenState state) {
        return ProductAllergen.reconstitute(
            state.id(),
            state.productId() == null ? null : ProductId.of(state.productId()),
            state.allergenType() == null ? null : AllergenType.valueOf(state.allergenType()),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ProductAllergenState toState(ProductAllergen productAllergen) {
        return new ProductAllergenState(
            productAllergen.getId(),
            productAllergen.getProductId() == null ? null : productAllergen.getProductId().value(),
            productAllergen.getAllergenType() == null ? null : productAllergen.getAllergenType().name(),
            productAllergen.getCreatedAt(),
            productAllergen.getUpdatedAt()
        );
    }
}
