package com.tastyhouse.domain.product.model;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductDiscountInfo;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

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
            ShopId.of(1L), ProductCategoryId.of(2L), "떡볶이", "매운맛", 10000,
            8000, BigDecimal.valueOf(0.2), 4.5, 10,
            true, 3, false, true, 1, false
        );

        assertThat(product.getId()).isNull();
        assertThat(product.getShopId()).isEqualTo(ShopId.of(1L));
        assertThat(product.getProductCategoryId()).isEqualTo(ProductCategoryId.of(2L));
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
            ShopId.of(1L), ProductCategoryId.of(2L), "떡볶이", "매운맛", 10000,
            null, null, null, 0,
            false, 3, false, true, 1, false
        );

        product.update(
            ProductCategoryId.of(3L), "국물떡볶이", "순한맛", 12000,
            9000, BigDecimal.valueOf(0.25), true, 1, true, false, 2
        );

        assertThat(product.getProductCategoryId()).isEqualTo(ProductCategoryId.of(3L));
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
            ShopId.of(1L), ProductCategoryId.of(2L), "떡볶이", "매운맛", 10000,
            null, null, null, 0, false, 3, false, true, 1, false
        );

        product.markSoldOut();

        assertThat(product.isSoldOut()).isTrue();
    }

    @Test
    @DisplayName("deactivate는 노출 여부를 false로 변경한다")
    void deactivate_marksInvisible() {
        Product product = Product.of(
            ShopId.of(1L), ProductCategoryId.of(2L), "떡볶이", "매운맛", 10000,
            null, null, null, 0, false, 3, false, true, 1, false
        );

        product.deactivate();

        assertThat(product.isVisible()).isFalse();
    }

    @Test
    @DisplayName("updateReviewStats는 평점과 리뷰 수를 변경한다")
    void updateReviewStats_changesRatingAndReviewCount() {
        Product product = Product.of(
            ShopId.of(1L), ProductCategoryId.of(2L), "떡볶이", "매운맛", 10000,
            null, null, null, 0, false, 3, false, true, 1, false
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
            1L, ShopId.of(10L), ProductCategoryId.of(20L), "떡볶이", "매운맛", 10000,
            discountInfo, 4.5, 10, true, 3, false, true, 1, false,
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
            ShopId.of(1L), ProductCategoryId.of(2L), "떡볶이", "매운맛", 10000,
            null, null, null, 0, false, 3, false, true, 1, false
        );

        assertThatThrownBy(product::getProductId)
            .isInstanceOf(IllegalArgumentException.class);
    }

    private static Product productWithPrices(Integer originalPrice, Integer discountPrice) {
        return Product.of(
            ShopId.of(1L), ProductCategoryId.of(2L), "떡볶이", "매운맛", originalPrice,
            discountPrice, null, null, 0, false, 3, false, true, 1, false
        );
    }

    @Test
    @DisplayName("of는 할인가가 정가와 같은 경계값을 통과시키고, 할인가 null(할인 없음)도 통과시킨다")
    void of_priceBoundaries_pass() {
        assertThat(productWithPrices(10000, 10000).getDiscountPrice()).isEqualTo(10000);
        assertThat(productWithPrices(10000, null).getDiscountPrice()).isNull();
        assertThat(productWithPrices(0, 0).getOriginalPrice()).isZero();
    }

    @Test
    @DisplayName("of는 정가가 음수면 PRODUCT_PRICE_NEGATIVE로 거부한다")
    void of_negativeOriginalPrice_throws() {
        assertThatThrownBy(() -> productWithPrices(-1, null))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_PRICE_NEGATIVE);
    }

    @Test
    @DisplayName("of는 할인가가 음수면 PRODUCT_PRICE_NEGATIVE로 거부한다")
    void of_negativeDiscountPrice_throws() {
        assertThatThrownBy(() -> productWithPrices(10000, -1))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_PRICE_NEGATIVE);
    }

    @Test
    @DisplayName("of는 할인가가 정가보다 크면 PRODUCT_DISCOUNT_PRICE_EXCEEDS_ORIGINAL로 거부한다")
    void of_discountPriceExceedsOriginal_throws() {
        assertThatThrownBy(() -> productWithPrices(10000, 10001))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_DISCOUNT_PRICE_EXCEEDS_ORIGINAL);
    }

    @Test
    @DisplayName("update도 of와 같은 가격 불변식을 강제한다(생성만 막고 변경을 열어두지 않는다)")
    void update_enforcesSameInvariants() {
        Product product = productWithPrices(10000, 8000);

        assertThatThrownBy(() -> product.update(
            ProductCategoryId.of(3L), "국물떡볶이", "순한맛", 10000,
            20000, null, true, 1, true, false, 2
        ))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_DISCOUNT_PRICE_EXCEEDS_ORIGINAL);

        // 실패한 update는 기존 상태를 바꾸지 않는다
        assertThat(product.getName()).isEqualTo("떡볶이");
        assertThat(product.getDiscountPrice()).isEqualTo(8000);
    }

    @Test
    @DisplayName("reconstitute는 가격 불변식 검증을 하지 않는다(불변식 위반 레거시 행도 로드 가능)")
    void reconstitute_bypassesPriceValidation() {
        Product product = Product.reconstitute(
            1L, ShopId.of(10L), ProductCategoryId.of(20L), "레거시상품", "설명", -5000,
            ProductDiscountInfo.of(99999, null), null, 0, false, 3, false, true, 1, false,
            null, null
        );

        assertThat(product.getOriginalPrice()).isEqualTo(-5000);
        assertThat(product.getDiscountPrice()).isEqualTo(99999);
    }
}
