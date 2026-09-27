package com.tastyhouse.application.product.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeEntryType;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeHistory;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductOptionGroupMergeHistoryStateMapperTest {

    @Test
    @DisplayName("ProductOptionGroupMergeHistory → ProductOptionGroupMergeHistoryState → ProductOptionGroupMergeHistory 왕복 시 모든 필드가 보존된다")
    void productOptionGroupMergeHistoryRoundTrip() {
        ProductOptionGroupMergeHistory original = ProductOptionGroupMergeHistory.reconstitute(
            211L,
            ShopId.of(212L),
            ProductOptionGroupId.of(213L),
            ProductOptionGroupId.of(214L),
            "병합그룹",
            ProductOptionGroupMergeEntryType.MANUAL,
            CeoId.of(215L)
        );

        ProductOptionGroupMergeHistory restored = ProductOptionGroupMergeHistoryStateMapper.toDomain(ProductOptionGroupMergeHistoryStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
