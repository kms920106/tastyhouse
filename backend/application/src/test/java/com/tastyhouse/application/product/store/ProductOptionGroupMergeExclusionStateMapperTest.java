package com.tastyhouse.application.product.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeExclusion;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductOptionGroupMergeExclusionStateMapperTest {

    @Test
    @DisplayName("ProductOptionGroupMergeExclusion → ProductOptionGroupMergeExclusionState → ProductOptionGroupMergeExclusion 왕복 시 모든 필드가 보존된다")
    void productOptionGroupMergeExclusionRoundTrip() {
        ProductOptionGroupMergeExclusion original = ProductOptionGroupMergeExclusion.reconstitute(
            201L,
            ShopId.of(202L),
            "sig-abc",
            CeoId.of(203L)
        );

        ProductOptionGroupMergeExclusion restored = ProductOptionGroupMergeExclusionStateMapper.toDomain(ProductOptionGroupMergeExclusionStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
