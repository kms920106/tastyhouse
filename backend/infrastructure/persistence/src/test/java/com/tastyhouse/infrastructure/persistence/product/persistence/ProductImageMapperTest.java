package com.tastyhouse.infrastructure.persistence.product.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.vo.ProductId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductImageMapperTest {

    @Test
    @DisplayName("ProductImage → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        ProductImage productImage = ProductImage.reconstitute(
            81L,
            ProductId.of(82L),
            UploadedFileId.of(83L),
            4,
            true
        );

        ProductImageJpaEntity entity = ProductImageMapper.toEntity(productImage);

        assertThat(entity.getProductId()).isEqualTo(82L);
        assertThat(entity.getImageFileId()).isEqualTo(83L);
        assertThat(entity.getSort()).isEqualTo(4);
        assertThat(entity.isVisible()).isEqualTo(true);
    }

    @Test
    @DisplayName("엔티티 → ProductImage 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ProductImageJpaEntity entity = ProductImageJpaEntity.create(
            82L,
            83L,
            4,
            true
        );
        ReflectionTestUtils.setField(entity, "id", 81L);

        ProductImage restored = ProductImageMapper.toDomain(entity);

        ProductImage expected = ProductImage.reconstitute(
            81L,
            ProductId.of(82L),
            UploadedFileId.of(83L),
            4,
            true
        );
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
