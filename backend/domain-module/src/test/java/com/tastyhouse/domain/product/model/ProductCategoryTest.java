package com.tastyhouse.domain.product.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.vo.ProductCategoryId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다.
 */
class ProductCategoryTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자 없음)다")
    void of_createsTransientProductCategory() {
        ProductCategory category = ProductCategory.of(ShopId.of(1L), "분식", 1, true);

        assertThat(category.getId()).isNull();
        assertThat(category.getShopId()).isEqualTo(ShopId.of(1L));
        assertThat(category.getName()).isEqualTo("분식");
        assertThat(category.getSort()).isEqualTo(1);
        assertThat(category.isVisible()).isTrue();
    }

    @Test
    @DisplayName("update는 이름·정렬순서·노출여부를 변경한다")
    void update_changesFields() {
        ProductCategory category = ProductCategory.of(ShopId.of(1L), "분식", 1, true);

        category.update("디저트", 2, false);

        assertThat(category.getName()).isEqualTo("디저트");
        assertThat(category.getSort()).isEqualTo(2);
        assertThat(category.isVisible()).isFalse();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ProductCategory category = ProductCategory.reconstitute(1L, ShopId.of(10L), "분식", 1, true);

        assertThat(category.getId()).isEqualTo(1L);
        assertThat(category.getProductCategoryId()).isEqualTo(ProductCategoryId.of(1L));
    }

    @Test
    @DisplayName("미영속 상태에서 getProductCategoryId를 호출하면 불변식 위반으로 예외가 발생한다")
    void getProductCategoryId_onTransient_throws() {
        ProductCategory category = ProductCategory.of(ShopId.of(1L), "분식", 1, true);

        assertThatThrownBy(category::getProductCategoryId)
            .isInstanceOf(IllegalArgumentException.class);
    }
}
