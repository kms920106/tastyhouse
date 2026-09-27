package com.tastyhouse.application.product.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.vo.ProductId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductImageStateMapperTest {

    @Test
    @DisplayName("ProductImage → ProductImageState → ProductImage 왕복 시 모든 필드가 보존된다")
    void productImageRoundTrip() {
        ProductImage original = ProductImage.reconstitute(
            81L,
            ProductId.of(82L),
            UploadedFileId.of(83L),
            4,
            true
        );

        ProductImage restored = ProductImageStateMapper.toDomain(ProductImageStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
