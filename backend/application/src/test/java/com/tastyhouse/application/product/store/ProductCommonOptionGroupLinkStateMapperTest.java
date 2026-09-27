package com.tastyhouse.application.product.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroupLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCommonOptionGroupLinkStateMapperTest {

    @Test
    @DisplayName("ProductCommonOptionGroupLink → ProductCommonOptionGroupLinkState → ProductCommonOptionGroupLink 왕복 시 모든 필드가 보존된다")
    void productCommonOptionGroupLinkRoundTrip() {
        ProductCommonOptionGroupLink original = ProductCommonOptionGroupLink.reconstitute(
            191L,
            ProductId.of(192L),
            ProductOptionGroupId.of(193L),
            11
        );

        ProductCommonOptionGroupLink restored = ProductCommonOptionGroupLinkStateMapper.toDomain(ProductCommonOptionGroupLinkStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
