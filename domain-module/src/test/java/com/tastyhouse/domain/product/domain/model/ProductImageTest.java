package com.tastyhouse.domain.product.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다.
 * 상태전이 메서드가 없는 불변 애그리거트라 생성·재구성만 검증한다.
 */
class ProductImageTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자 없음)다")
    void of_createsTransientProductImage() {
        ProductImage image = ProductImage.of(1L, 100L, 1, true);

        assertThat(image.getId()).isNull();
        assertThat(image.getProductId()).isEqualTo(1L);
        assertThat(image.getImageFileId()).isEqualTo(100L);
        assertThat(image.getSort()).isEqualTo(1);
        assertThat(image.isVisible()).isTrue();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ProductImage image = ProductImage.reconstitute(1L, 10L, 100L, 1, true);

        assertThat(image.getId()).isEqualTo(1L);
        assertThat(image.getProductId()).isEqualTo(10L);
        assertThat(image.getImageFileId()).isEqualTo(100L);
    }
}
