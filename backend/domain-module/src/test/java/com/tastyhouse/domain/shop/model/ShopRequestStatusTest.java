package com.tastyhouse.domain.shop.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 통합 요청 상태 enum 단위 테스트.
 */
class ShopRequestStatusTest {

    @Test
    @DisplayName("알 수 없는 코드는 SHOP_REQUEST_STATUS_UNKNOWN(400)으로 변환된다")
    void from_withUnknownCode_throwsBusinessException() {
        assertThatThrownBy(() -> ShopRequestStatus.from("COMPLETED"))
            .isInstanceOf(BusinessException.class)
            .satisfies(thrown -> assertThat(((BusinessException) thrown).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_REQUEST_STATUS_UNKNOWN));
    }

    @Test
    @DisplayName("진행 중은 대기중·진행 둘뿐이고 나머지는 종결이다")
    void isOpen_isTrueOnlyForPendingAndInProgress() {
        assertThat(ShopRequestStatus.PENDING.isOpen()).isTrue();
        assertThat(ShopRequestStatus.IN_PROGRESS.isOpen()).isTrue();
        assertThat(ShopRequestStatus.REJECTED.isClosed()).isTrue();
        assertThat(ShopRequestStatus.CANCELED.isClosed()).isTrue();
        assertThat(ShopRequestStatus.APPROVED.isClosed()).isTrue();
    }

    @Test
    @DisplayName("배민 원문의 5종 라벨을 그대로 갖는다")
    void descriptions_matchSourceLabels() {
        assertThat(ShopRequestStatus.PENDING.getDescription()).isEqualTo("대기중");
        assertThat(ShopRequestStatus.IN_PROGRESS.getDescription()).isEqualTo("진행");
        assertThat(ShopRequestStatus.REJECTED.getDescription()).isEqualTo("반려");
        assertThat(ShopRequestStatus.CANCELED.getDescription()).isEqualTo("취소");
        assertThat(ShopRequestStatus.APPROVED.getDescription()).isEqualTo("승인");
    }
}
