package com.tastyhouse.application.product.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.domain.product.vo.ProductId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductOptionGroupStateMapperTest {

    @Test
    @DisplayName("ProductOptionGroup → ProductOptionGroupState → ProductOptionGroup 왕복 시 모든 필드가 보존된다")
    void productOptionGroupRoundTrip() {
        ProductOptionGroup original = ProductOptionGroup.reconstitute(
            161L,
            ProductId.of(162L),
            "사이즈",
            "컵 크기",
            true,
            false,
            1,
            3,
            8,
            true,
            ProductOptionGroupType.CUP_DEPOSIT
        );

        ProductOptionGroup restored = ProductOptionGroupStateMapper.toDomain(ProductOptionGroupStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
