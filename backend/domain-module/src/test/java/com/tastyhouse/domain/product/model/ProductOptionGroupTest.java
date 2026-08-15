package com.tastyhouse.domain.product.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.tastyhouse.domain.product.vo.ProductId;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다.
 */
class ProductOptionGroupTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자 없음)다")
    void of_createsTransientOptionGroup() {
        ProductOptionGroup group = ProductOptionGroup.of(
            ProductId.of(1L), "맵기 선택", "매운 정도를 선택하세요", true, false, 1, 1, 1, true
        );

        assertThat(group.getId()).isNull();
        assertThat(group.getProductId()).isEqualTo(ProductId.of(1L));
        assertThat(group.getName()).isEqualTo("맵기 선택");
        assertThat(group.isRequired()).isTrue();
        assertThat(group.isMultipleSelect()).isFalse();
        assertThat(group.getMinSelect()).isEqualTo(1);
        assertThat(group.getMaxSelect()).isEqualTo(1);
    }

    @Test
    @DisplayName("update는 그룹 정보를 변경한다")
    void update_changesFields() {
        ProductOptionGroup group = ProductOptionGroup.of(
            ProductId.of(1L), "맵기 선택", "매운 정도를 선택하세요", true, false, 1, 1, 1, true
        );

        group.update("토핑 선택", "추가 토핑을 선택하세요", false, true, 0, 3, 2, false);

        assertThat(group.getName()).isEqualTo("토핑 선택");
        assertThat(group.getDescription()).isEqualTo("추가 토핑을 선택하세요");
        assertThat(group.isRequired()).isFalse();
        assertThat(group.isMultipleSelect()).isTrue();
        assertThat(group.getMinSelect()).isZero();
        assertThat(group.getMaxSelect()).isEqualTo(3);
        assertThat(group.getSort()).isEqualTo(2);
        assertThat(group.isVisible()).isFalse();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ProductOptionGroup group = ProductOptionGroup.reconstitute(
            1L, ProductId.of(10L), "맵기 선택", "매운 정도를 선택하세요", true, false, 1, 1, 1, true
        );

        assertThat(group.getId()).isEqualTo(1L);
        assertThat(group.getProductOptionGroupId()).isEqualTo(ProductOptionGroupId.of(1L));
    }

    @Test
    @DisplayName("미영속 상태에서 getProductOptionGroupId를 호출하면 불변식 위반으로 예외가 발생한다")
    void getProductOptionGroupId_onTransient_throws() {
        ProductOptionGroup group = ProductOptionGroup.of(
            ProductId.of(1L), "맵기 선택", "매운 정도를 선택하세요", true, false, 1, 1, 1, true
        );

        assertThatThrownBy(group::getProductOptionGroupId)
            .isInstanceOf(IllegalArgumentException.class);
    }
}
