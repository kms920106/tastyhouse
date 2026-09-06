package com.tastyhouse.domain.product.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.vo.ProductId;

class ProductImageTest {
    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자 없음)다")
    void of_createsTransientProductImage() {
        ProductImage image = ProductImage.of(ProductId.of(1L), UploadedFileId.of(100L), 1, true);

        assertThat(image.getId()).isNull();
        assertThat(image.getProductId()).isEqualTo(ProductId.of(1L));
        assertThat(image.getImageFileId()).isEqualTo(UploadedFileId.of(100L));
        assertThat(image.getSort()).isEqualTo(1);
        assertThat(image.isVisible()).isTrue();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ProductImage image = ProductImage.reconstitute(1L, ProductId.of(10L), UploadedFileId.of(100L), 1, true);

        assertThat(image.getId()).isEqualTo(1L);
        assertThat(image.getProductId()).isEqualTo(ProductId.of(10L));
        assertThat(image.getImageFileId()).isEqualTo(UploadedFileId.of(100L));
    }
}
