package com.tastyhouse.infrastructure.product.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeExclusion;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductOptionGroupMergeExclusionMapperTest {

    @Test
    @DisplayName("ProductOptionGroupMergeExclusion → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        ProductOptionGroupMergeExclusion productOptionGroupMergeExclusion = ProductOptionGroupMergeExclusion.reconstitute(
            201L,
            ShopId.of(202L),
            "sig-abc",
            CeoId.of(203L)
        );

        ProductOptionGroupMergeExclusionJpaEntity entity = ProductOptionGroupMergeExclusionMapper.toEntity(productOptionGroupMergeExclusion);

        assertThat(entity.getShopId()).isEqualTo(202L);
        assertThat(entity.getGroupSignature()).isEqualTo("sig-abc");
        assertThat(entity.getActorCeoId()).isEqualTo(203L);
    }

    @Test
    @DisplayName("엔티티 → ProductOptionGroupMergeExclusion 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ProductOptionGroupMergeExclusionJpaEntity entity = ProductOptionGroupMergeExclusionJpaEntity.create(
            202L,
            "sig-abc",
            203L
        );
        ReflectionTestUtils.setField(entity, "id", 201L);

        ProductOptionGroupMergeExclusion restored = ProductOptionGroupMergeExclusionMapper.toDomain(entity);

        ProductOptionGroupMergeExclusion expected = ProductOptionGroupMergeExclusion.reconstitute(
            201L,
            ShopId.of(202L),
            "sig-abc",
            CeoId.of(203L)
        );
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
