package com.tastyhouse.infrastructure.product.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCategoryMapperTest {

    @Test
    @DisplayName("ProductCategory → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        ProductCategory productCategory = ProductCategory.reconstitute(
            71L,
            ShopId.of(72L),
            "메인",
            "대표 메뉴",
            3,
            true
        );

        ProductCategoryJpaEntity entity = ProductCategoryMapper.toEntity(productCategory);

        assertThat(entity.getShopId()).isEqualTo(72L);
        assertThat(entity.getName()).isEqualTo("메인");
        assertThat(entity.getDescription()).isEqualTo("대표 메뉴");
        assertThat(entity.getSort()).isEqualTo(3);
        assertThat(entity.isVisible()).isEqualTo(true);
    }

    @Test
    @DisplayName("엔티티 → ProductCategory 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ProductCategoryJpaEntity entity = ProductCategoryJpaEntity.create(
            72L,
            "메인",
            "대표 메뉴",
            3,
            true
        );
        ReflectionTestUtils.setField(entity, "id", 71L);

        ProductCategory restored = ProductCategoryMapper.toDomain(entity);

        ProductCategory expected = ProductCategory.reconstitute(
            71L,
            ShopId.of(72L),
            "메인",
            "대표 메뉴",
            3,
            true
        );
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
