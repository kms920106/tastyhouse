package com.tastyhouse.domain.product.domain.model;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.domain.vo.ProductDiscountInfo;
import com.tastyhouse.domain.product.domain.vo.ProductId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다.
 */
class ProductTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자 없음)이고 판매중·비품절 상태다")
    void of_createsTransientProduct() {
        Product product = Product.of(
            1L, 2L, "떡볶이", "매운맛", 10000,
            8000, BigDecimal.valueOf(0.2), 4.5, 10,
            true, 3, false, true, 1
        );

        assertThat(product.getId()).isNull();
        assertThat(product.getShopId()).isEqualTo(1L);
        assertThat(product.getProductCategoryId()).isEqualTo(2L);
        assertThat(product.getName()).isEqualTo("떡볶이");
        assertThat(product.getDiscountPrice()).isEqualTo(8000);
        assertThat(product.getDiscountRate()).isEqualByComparingTo(BigDecimal.valueOf(0.2));
        assertThat(product.isSoldOut()).isFalse();
        assertThat(product.isVisible()).isTrue();
    }

    @Test
    @DisplayName("update는 카테고리·이름·가격 등 필드를 변경한다")
    void update_changesFields() {
        Product product = Product.of(
            1L, 2L, "떡볶이", "매운맛", 10000,
            null, null, null, 0,
            false, 3, false, true, 1
        );

        product.update(
            3L, "국물떡볶이", "순한맛", 12000,
            9000, BigDecimal.valueOf(0.25), true, 1, true, false, 2
        );

        assertThat(product.getProductCategoryId()).isEqualTo(3L);
        assertThat(product.getName()).isEqualTo("국물떡볶이");
        assertThat(product.getDescription()).isEqualTo("순한맛");
        assertThat(product.getOriginalPrice()).isEqualTo(12000);
        assertThat(product.getDiscountPrice()).isEqualTo(9000);
        assertThat(product.isRepresentative()).isTrue();
        assertThat(product.isSoldOut()).isTrue();
        assertThat(product.isVisible()).isFalse();
        assertThat(product.getSort()).isEqualTo(2);
    }

    @Test
    @DisplayName("markSoldOut은 품절 상태로 변경한다")
    void markSoldOut_marksSoldOut() {
        Product product = Product.of(
            1L, 2L, "떡볶이", "매운맛", 10000,
            null, null, null, 0, false, 3, false, true, 1
        );

        product.markSoldOut();

        assertThat(product.isSoldOut()).isTrue();
    }

    @Test
    @DisplayName("deactivate는 노출 여부를 false로 변경한다")
    void deactivate_marksInvisible() {
        Product product = Product.of(
            1L, 2L, "떡볶이", "매운맛", 10000,
            null, null, null, 0, false, 3, false, true, 1
        );

        product.deactivate();

        assertThat(product.isVisible()).isFalse();
    }

    @Test
    @DisplayName("updateReviewStats는 평점과 리뷰 수를 변경한다")
    void updateReviewStats_changesRatingAndReviewCount() {
        Product product = Product.of(
            1L, 2L, "떡볶이", "매운맛", 10000,
            null, null, null, 0, false, 3, false, true, 1
        );

        product.updateReviewStats(4.8, 25);

        assertThat(product.getRating()).isEqualTo(4.8);
        assertThat(product.getReviewCount()).isEqualTo(25);
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ProductDiscountInfo discountInfo = ProductDiscountInfo.of(8000, BigDecimal.valueOf(0.2));

        Product product = Product.reconstitute(
            1L, 10L, 20L, "떡볶이", "매운맛", 10000,
            discountInfo, 4.5, 10, true, 3, false, true, 1,
            null, null
        );

        assertThat(product.getId()).isEqualTo(1L);
        assertThat(product.getProductId()).isEqualTo(ProductId.of(1L));
        assertThat(product.getDiscountInfo()).isEqualTo(discountInfo);
    }

    @Test
    @DisplayName("미영속 상태에서 getProductId를 호출하면 ProductId 불변식 위반으로 예외가 발생한다")
    void getProductId_onTransient_throws() {
        Product product = Product.of(
            1L, 2L, "떡볶이", "매운맛", 10000,
            null, null, null, 0, false, 3, false, true, 1
        );

        assertThatThrownBy(product::getProductId)
            .isInstanceOf(IllegalArgumentException.class);
    }
}
