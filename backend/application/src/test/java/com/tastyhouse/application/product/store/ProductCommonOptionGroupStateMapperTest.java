package com.tastyhouse.application.product.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroup;
import com.tastyhouse.domain.product.vo.ProductId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCommonOptionGroupStateMapperTest {

    @Test
    @DisplayName("ProductCommonOptionGroup → ProductCommonOptionGroupState → ProductCommonOptionGroup 왕복 시 모든 필드가 보존된다")
    void productCommonOptionGroupRoundTrip() {
        ProductCommonOptionGroup original = ProductCommonOptionGroup.reconstitute(
            171L,
            ProductId.of(172L),
            "토핑",
            "공통 토핑",
            false,
            true,
            0,
            4,
            9,
            true
        );

        ProductCommonOptionGroup restored = ProductCommonOptionGroupStateMapper.toDomain(ProductCommonOptionGroupStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
