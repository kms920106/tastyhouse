package com.tastyhouse.domain.product.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 메뉴 정보 고객 의견의 순수 단위 테스트.
 *
 * <p><b>내용 필수 판정이 이 테스트의 핵심이다.</b> {@code ETC}는 유형만으로 무엇이 틀렸는지 알 수 없어
 * 서술이 없으면 점주가 고칠 수 없는 제보가 되고, 공백 문자열로 그 검증을 우회할 수 있으면 규칙이 없는
 * 것과 같다.
 */
class ProductFeedbackTest {

    private static final ProductId PRODUCT_ID = ProductId.of(1L);
    private static final ShopId SHOP_ID = ShopId.of(2L);
    private static final MemberId MEMBER_ID = MemberId.of(3L);

    private static ProductFeedback create(ProductFeedbackType type, String content) {
        return ProductFeedback.of(PRODUCT_ID, SHOP_ID, MEMBER_ID, type, content);
    }

    @Nested
    @DisplayName("내용 불변식")
    class ContentInvariant {

        @Test
        @DisplayName("ETC는 내용이 없으면 거절한다")
        void etc_withoutContent_rejected() {
            assertThatThrownBy(() -> create(ProductFeedbackType.ETC, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_FEEDBACK_CONTENT_REQUIRED);
        }

        @Test
        @DisplayName("ETC는 공백만 담긴 내용도 없는 것으로 보고 거절한다")
        void etc_withBlankContent_rejected() {
            assertThatThrownBy(() -> create(ProductFeedbackType.ETC, "   "))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_FEEDBACK_CONTENT_REQUIRED);
        }

        @Test
        @DisplayName("ETC 외 유형은 내용이 없어도 접수된다 — 유형 자체가 내용이다")
        void nonEtc_withoutContent_accepted() {
            assertThatCode(() -> create(ProductFeedbackType.PRICE, null)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("500자를 넘는 내용은 거절한다")
        void tooLongContent_rejected() {
            String tooLong = "가".repeat(501);

            assertThatThrownBy(() -> create(ProductFeedbackType.ETC, tooLong))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_FEEDBACK_CONTENT_TOO_LONG);
        }

        @Test
        @DisplayName("정확히 500자는 접수된다 — 경계값은 허용이다")
        void exactlyMaxLengthContent_accepted() {
            String exact = "가".repeat(500);

            assertThat(create(ProductFeedbackType.ETC, exact).getContent()).hasSize(500);
        }

        @Test
        @DisplayName("내용의 앞뒤 공백은 제거해 저장한다")
        void content_isTrimmed() {
            ProductFeedback feedback = create(ProductFeedbackType.ETC, "  가격이 달라요  ");

            assertThat(feedback.getContent()).isEqualTo("가격이 달라요");
        }
    }

    @Nested
    @DisplayName("유형 승격")
    class TypeConversion {

        @Test
        @DisplayName("알 수 없는 문자열은 400으로 거절한다 — 500으로 새어 나가면 입력 오류임을 구분할 수 없다")
        void unknownType_rejected() {
            assertThatThrownBy(() -> ProductFeedbackType.from("UNKNOWN"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_FEEDBACK_TYPE_UNKNOWN);
        }

        @Test
        @DisplayName("null도 400으로 거절한다")
        @SuppressWarnings("ConstantConditions") // null 거절 자체가 이 테스트의 검증 대상이다.
        void nullType_rejected() {
            assertThatThrownBy(() -> ProductFeedbackType.from(null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_FEEDBACK_TYPE_UNKNOWN);
        }

        @Test
        @DisplayName("소문자·공백이 섞여도 승격한다")
        void lowercaseWithSpaces_converted() {
            assertThat(ProductFeedbackType.from(" price ")).isEqualTo(ProductFeedbackType.PRICE);
        }

        @Test
        @DisplayName("서술이 필수인 유형은 ETC 하나뿐이다")
        void onlyEtcRequiresContent() {
            assertThat(ProductFeedbackType.ETC.requiresContent()).isTrue();
            assertThat(ProductFeedbackType.PRICE.requiresContent()).isFalse();
            assertThat(ProductFeedbackType.IMAGE.requiresContent()).isFalse();
            assertThat(ProductFeedbackType.COMPOSITION.requiresContent()).isFalse();
            assertThat(ProductFeedbackType.SOLD_OUT.requiresContent()).isFalse();
        }
    }

    @Nested
    @DisplayName("재구성")
    class Reconstitute {

        @Test
        @DisplayName("불변식을 위반한 기존 행도 로드는 가능하다 — 검증 도입 이전 데이터를 막지 않는다")
        void reconstitute_skipsValidation() {
            ProductFeedback feedback = ProductFeedback.reconstitute(
                1L, PRODUCT_ID, SHOP_ID, MEMBER_ID, ProductFeedbackType.ETC, null, null, null
            );

            assertThat(feedback.getContent()).isNull();
        }
    }
}
