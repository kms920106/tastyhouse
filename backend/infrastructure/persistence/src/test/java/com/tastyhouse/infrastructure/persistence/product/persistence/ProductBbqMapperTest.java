package com.tastyhouse.infrastructure.persistence.product.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.product.model.ProductBbq;
import com.tastyhouse.domain.product.vo.BbqCategoryId;
import com.tastyhouse.domain.product.vo.BbqMenuId;
import com.tastyhouse.domain.product.vo.ProductId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductBbqMapperTest {

    @Test
    @DisplayName("ProductBbq → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        ProductBbq productBbq = ProductBbq.reconstitute(
            21L,
            ProductId.of(22L),
            BbqMenuId.of(23L),
            BbqCategoryId.of(24L),
            true
        );

        ProductBbqJpaEntity entity = ProductBbqMapper.toEntity(productBbq);

        assertThat(entity.getProductId()).isEqualTo(22L);
        assertThat(entity.getBbqMenuId()).isEqualTo(23L);
        assertThat(entity.getBbqCategoryId()).isEqualTo(24L);
        assertThat(entity.isOptionsSynced()).isEqualTo(true);
    }

    @Test
    @DisplayName("엔티티 → ProductBbq 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ProductBbqJpaEntity entity = ProductBbqJpaEntity.create(
            22L,
            23L,
            24L,
            true
        );
        ReflectionTestUtils.setField(entity, "id", 21L);

        ProductBbq restored = ProductBbqMapper.toDomain(entity);

        ProductBbq expected = ProductBbq.reconstitute(
            21L,
            ProductId.of(22L),
            BbqMenuId.of(23L),
            BbqCategoryId.of(24L),
            true
        );
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
