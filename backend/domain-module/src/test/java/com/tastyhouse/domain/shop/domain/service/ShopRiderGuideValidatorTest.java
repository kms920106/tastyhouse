package com.tastyhouse.domain.shop.domain.service;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.model.ProhibitedWord;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.repository.ProhibitedWordRepository;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.domain.shop.service.ShopRiderGuideValidator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 라이더 안내 등록 기준(PDF "작성 불가 3유형") 검증 단위 테스트. 금칙어 포트는 fake로 대체해
 * Spring/DB 없이 판정 로직만 검증한다.
 */
class ShopRiderGuideValidatorTest {

    private ShopRiderGuideValidator shopRiderGuideValidator;
    private Shop shop;

    /**
     * 금칙어 테이블을 대신하는 fake. 시드와 동일하게 "전화주문" 하나만 담는다.
     */
    private static class FakeProhibitedWordRepository implements ProhibitedWordRepository {

        @Override
        public List<ProhibitedWord> findAll() {
            return List.of(ProhibitedWord.reconstitute(1L, "전화주문", "전화 주문 유도"));
        }
    }

    @BeforeEach
    void setUp() {
        shopRiderGuideValidator = new ShopRiderGuideValidator(
            new ProhibitedWordValidator(new FakeProhibitedWordRepository())
        );
        shop = Shop.reconstitute(
            1L, null, null, "맛있는 분식",
            BigDecimal.valueOf(37.497942), BigDecimal.valueOf(127.027621), 4.5,
            "서울시 송파구 위례성대로 10", "서울시 송파구 방이동 44-1", "02-1234-5678",
            null, null, false, false, false, 10000, false, null, null
        );
    }

    @Test
    @DisplayName("PDF의 좋은 작성 예시는 위반 없이 통과한다")
    void findViolations_returnsEmpty_whenGuideFollowsGuideline() {
        List<String> violations = shopRiderGuideValidator.findViolations(
            shop, "대로변에서 분홍색 건물 1층 OO 안경 옆 가게입니다."
        );

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("문구가 비어 있으면 위반으로 보지 않는다(빈 값 = 삭제)")
    void findViolations_returnsEmpty_whenGuideIsBlank() {
        assertThat(shopRiderGuideValidator.findViolations(shop, null)).isEmpty();
        assertThat(shopRiderGuideValidator.findViolations(shop, "   ")).isEmpty();
    }

    @Test
    @DisplayName("금칙어가 포함되면 위반 사유에 담긴다")
    void findViolations_detectsProhibitedWord() {
        List<String> violations = shopRiderGuideValidator.findViolations(shop, "전화주문 하시면 빨리 나옵니다.");

        assertThat(violations).hasSize(1);
        assertThat(violations.getFirst()).contains("전화주문");
    }

    @Test
    @DisplayName("가게 실주소를 2토큰 이상 연속 기재하면 위반이다")
    void findViolations_detectsShopAddress() {
        List<String> violations = shopRiderGuideValidator.findViolations(shop, "서울시 송파구 위례성대로 10으로 오세요.");

        assertThat(violations).contains(ErrorCode.SHOP_RIDER_VISIT_GUIDE_CONTAINS_ADDRESS.getDefaultMessage());
    }

    @Test
    @DisplayName("주소 토큰 1개만 겹치는 정상 안내는 오탐하지 않는다")
    void findViolations_doesNotFlagSingleAddressToken() {
        List<String> violations = shopRiderGuideValidator.findViolations(shop, "송파구청 뒷골목에 있습니다.");

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("지번주소를 연속 기재해도 위반이다")
    void findViolations_detectsLotAddress() {
        List<String> violations = shopRiderGuideValidator.findViolations(shop, "서울시 송파구 방이동 44-1 입니다.");

        assertThat(violations).contains(ErrorCode.SHOP_RIDER_VISIT_GUIDE_CONTAINS_ADDRESS.getDefaultMessage());
    }

    @Test
    @DisplayName("배차·이동수단을 특정하는 문구는 위반이다")
    void findViolations_detectsDispatchRestriction() {
        List<String> violations = shopRiderGuideValidator.findViolations(
            shop, "18인치 피자의 경우 자동차 라이더만 부탁드립니다."
        );

        assertThat(violations).hasSize(1);
        assertThat(violations.getFirst()).contains("자동차 라이더");
    }

    @Test
    @DisplayName("보온가방 언급도 배차 특정 위반으로 판정한다")
    void findViolations_detectsWarmBagKeyword() {
        List<String> violations = shopRiderGuideValidator.findViolations(
            shop, "픽업 시 보온가방이 없으신 라이더분들은 자제 부탁드립니다."
        );

        assertThat(violations).hasSize(1);
        assertThat(violations.getFirst()).contains("보온가방");
    }

    @Test
    @DisplayName("사전 검수에서는 200자 초과도 위반 목록에 포함된다(프론트 maxLength 우회 대비)")
    void findViolations_includesLengthViolation() {
        List<String> violations = shopRiderGuideValidator.findViolations(shop, "가".repeat(201));

        assertThat(violations).contains(ErrorCode.SHOP_RIDER_VISIT_GUIDE_TOO_LONG.getDefaultMessage());
    }

    @Test
    @DisplayName("validate는 기준을 통과하는 문구에 예외를 던지지 않는다")
    void validate_passes_whenGuideFollowsGuideline() {
        assertThatCode(() -> shopRiderGuideValidator.validate(shop, "가게 뒷문 앞에 오토바이를 세워주세요."))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validate는 가게 실주소가 포함되면 예외를 던진다")
    void validate_throwsException_whenContainsShopAddress() {
        assertThatThrownBy(() -> shopRiderGuideValidator.validate(shop, "서울시 송파구 위례성대로 10"))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_RIDER_VISIT_GUIDE_CONTAINS_ADDRESS);
    }

    @Test
    @DisplayName("validate는 배차 특정 문구에 예외를 던진다")
    void validate_throwsException_whenDispatchRestricted() {
        assertThatThrownBy(() -> shopRiderGuideValidator.validate(shop, "면이라 금방 불어버리니 잡지 말아주세요."))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_RIDER_VISIT_GUIDE_DISPATCH_RESTRICTION);
    }

    @Test
    @DisplayName("validate는 금칙어가 포함되면 예외를 던진다")
    void validate_throwsException_whenProhibitedWordIncluded() {
        assertThatThrownBy(() -> shopRiderGuideValidator.validate(shop, "전화주문 환영합니다."))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_TEXT_PROHIBITED_WORD);
    }

    @Test
    @DisplayName("validate는 빈 문구를 통과시킨다(빈 값 = 삭제)")
    void validate_passes_whenGuideIsBlank() {
        assertThatCode(() -> shopRiderGuideValidator.validate(shop, "")).doesNotThrowAnyException();
        assertThatCode(() -> shopRiderGuideValidator.validate(shop, null)).doesNotThrowAnyException();
    }
}
