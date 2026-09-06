package com.tastyhouse.domain.shop.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 요청 유형 enum 단위 테스트.
 *
 * <p>{@code from}이 생짜 {@code IllegalArgumentException}("No enum constant …")을 흘리지 않고 400
 * {@code ErrorCode}로 변환하는지가 핵심이다 — 이 변환이 없으면 잘못된 query 파라미터가 500이 된다.
 */
class ShopRequestTypeTest {

    @Test
    @DisplayName("알 수 없는 코드는 SHOP_REQUEST_TYPE_UNKNOWN(400)으로 변환된다")
    void from_withUnknownCode_throwsBusinessException() {
        assertThatThrownBy(() -> ShopRequestType.from("PARTNERSHIP"))
            .isInstanceOf(BusinessException.class)
            .satisfies(thrown -> assertThat(((BusinessException) thrown).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_REQUEST_TYPE_UNKNOWN));
    }

    @Test
    @DisplayName("정의된 코드는 그대로 승격된다")
    void from_withKnownCode_returnsConstant() {
        assertThat(ShopRequestType.from("DELIVERY_AREA_ADJUSTMENT"))
            .isEqualTo(ShopRequestType.DELIVERY_AREA_ADJUSTMENT);
    }

    @Test
    @DisplayName("계약서가 수정되는 요청은 배달지역 조정 신청뿐이다")
    void contractAmending_isTrueOnlyForAdjustment() {
        assertThat(ShopRequestType.DELIVERY_AREA_ADJUSTMENT.isContractAmending()).isTrue();
        assertThat(ShopRequestType.TRADEMARK_CHANGE.isContractAmending()).isFalse();
        assertThat(ShopRequestType.THUMBNAIL_CHANGE.isContractAmending()).isFalse();
    }

    @Test
    @DisplayName("모든 유형이 한글 라벨과 첨부 명칭을 갖는다(프론트 상수 복제를 막는 근거)")
    void allTypes_haveDescriptionAndAttachmentLabel() {
        assertThat(ShopRequestType.values()).allSatisfy(requestType -> {
            assertThat(requestType.getDescription()).isNotBlank();
            assertThat(requestType.getAttachmentLabel()).isNotBlank();
        });
    }
}
