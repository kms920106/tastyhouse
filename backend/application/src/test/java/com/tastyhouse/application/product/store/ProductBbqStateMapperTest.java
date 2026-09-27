package com.tastyhouse.application.product.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.ProductBbq;
import com.tastyhouse.domain.product.vo.BbqCategoryId;
import com.tastyhouse.domain.product.vo.BbqMenuId;
import com.tastyhouse.domain.product.vo.ProductId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductBbqStateMapperTest {

    @Test
    @DisplayName("ProductBbq → ProductBbqState → ProductBbq 왕복 시 모든 필드가 보존된다")
    void productBbqRoundTrip() {
        ProductBbq original = ProductBbq.reconstitute(
            21L,
            ProductId.of(22L),
            BbqMenuId.of(23L),
            BbqCategoryId.of(24L),
            true
        );

        ProductBbq restored = ProductBbqStateMapper.toDomain(ProductBbqStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
