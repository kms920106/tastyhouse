package com.tastyhouse.application.product.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductOptionGroupLinkStateMapperTest {

    @Test
    @DisplayName("ProductOptionGroupLink → ProductOptionGroupLinkState → ProductOptionGroupLink 왕복 시 모든 필드가 보존된다")
    void productOptionGroupLinkRoundTrip() {
        ProductOptionGroupLink original = ProductOptionGroupLink.reconstitute(
            181L,
            ProductId.of(182L),
            ProductOptionGroupId.of(183L),
            10
        );

        ProductOptionGroupLink restored = ProductOptionGroupLinkStateMapper.toDomain(ProductOptionGroupLinkStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
