package com.tastyhouse.infrastructure.jpa.product.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeEntryType;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeHistory;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductOptionGroupMergeHistoryMapperTest {

    @Test
    @DisplayName("ProductOptionGroupMergeHistory → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        ProductOptionGroupMergeHistory productOptionGroupMergeHistory = ProductOptionGroupMergeHistory.reconstitute(
            211L,
            ShopId.of(212L),
            ProductOptionGroupId.of(213L),
            ProductOptionGroupId.of(214L),
            "병합그룹",
            ProductOptionGroupMergeEntryType.MANUAL,
            CeoId.of(215L)
        );

        ProductOptionGroupMergeHistoryJpaEntity entity = ProductOptionGroupMergeHistoryMapper.toEntity(productOptionGroupMergeHistory);

        assertThat(entity.getShopId()).isEqualTo(212L);
        assertThat(entity.getBaseOptionGroupId()).isEqualTo(213L);
        assertThat(entity.getMergedOptionGroupId()).isEqualTo(214L);
        assertThat(entity.getMergedGroupName()).isEqualTo("병합그룹");
        assertThat(entity.getEntryType()).isEqualTo("MANUAL");
        assertThat(entity.getActorCeoId()).isEqualTo(215L);
    }

    @Test
    @DisplayName("엔티티 → ProductOptionGroupMergeHistory 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ProductOptionGroupMergeHistoryJpaEntity entity = ProductOptionGroupMergeHistoryJpaEntity.create(
            212L,
            213L,
            214L,
            "병합그룹",
            "MANUAL",
            215L
        );
        ReflectionTestUtils.setField(entity, "id", 211L);

        ProductOptionGroupMergeHistory restored = ProductOptionGroupMergeHistoryMapper.toDomain(entity);

        ProductOptionGroupMergeHistory expected = ProductOptionGroupMergeHistory.reconstitute(
            211L,
            ShopId.of(212L),
            ProductOptionGroupId.of(213L),
            ProductOptionGroupId.of(214L),
            "병합그룹",
            ProductOptionGroupMergeEntryType.MANUAL,
            CeoId.of(215L)
        );
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
