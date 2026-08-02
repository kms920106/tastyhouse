package com.tastyhouse.domain.product.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.domain.vo.ProductOptionId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.tastyhouse.domain.product.domain.vo.ProductOptionGroupId;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다.
 */
class ProductOptionTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자 없음)이고 추가금액 null은 0으로 보정된다")
    void of_createsTransientOption_withNullAdditionalPriceDefaultedToZero() {
        ProductOption option = ProductOption.of(ProductOptionGroupId.of(1L), "곱빼기", null, 1, false, true);

        assertThat(option.getId()).isNull();
        assertThat(option.getOptionGroupId()).isEqualTo(ProductOptionGroupId.of(1L));
        assertThat(option.getName()).isEqualTo("곱빼기");
        assertThat(option.getAdditionalPrice()).isZero();
        assertThat(option.isSoldOut()).isFalse();
        assertThat(option.isVisible()).isTrue();
    }

    @Test
    @DisplayName("update는 이름·추가금액·정렬순서·품절·노출여부를 변경한다")
    void update_changesFields() {
        ProductOption option = ProductOption.of(ProductOptionGroupId.of(1L), "곱빼기", 1000, 1, false, true);

        option.update("아주곱빼기", 2000, 2, true, false);

        assertThat(option.getName()).isEqualTo("아주곱빼기");
        assertThat(option.getAdditionalPrice()).isEqualTo(2000);
        assertThat(option.getSort()).isEqualTo(2);
        assertThat(option.isSoldOut()).isTrue();
        assertThat(option.isVisible()).isFalse();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ProductOption option = ProductOption.reconstitute(1L, ProductOptionGroupId.of(10L), "곱빼기", 1000, 1, false, true);

        assertThat(option.getId()).isEqualTo(1L);
        assertThat(option.getProductOptionId()).isEqualTo(ProductOptionId.of(1L));
    }

    @Test
    @DisplayName("미영속 상태에서 getProductOptionId를 호출하면 불변식 위반으로 예외가 발생한다")
    void getProductOptionId_onTransient_throws() {
        ProductOption option = ProductOption.of(ProductOptionGroupId.of(1L), "곱빼기", 1000, 1, false, true);

        assertThatThrownBy(option::getProductOptionId)
            .isInstanceOf(IllegalArgumentException.class);
    }
}
