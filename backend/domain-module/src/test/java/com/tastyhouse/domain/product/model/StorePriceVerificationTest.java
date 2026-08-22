package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 매장 가격 인증 요청 애그리거트의 상태 전이 단위 테스트.
 *
 * <p><b>종결된 요청의 재전이를 막는 것이 핵심이다</b> — 막지 않으면 이미 반려된 요청을 다시 승인해
 * 검수 결과를 덮어쓸 수 있다.
 */
class StorePriceVerificationTest {

    private static final ShopId SHOP_ID = ShopId.of(1L);
    private static final UploadedFileId FILE_ID = UploadedFileId.of(7L);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 1, 12, 0);

    private static StorePriceVerification pending() {
        return StorePriceVerification.of(SHOP_ID, FILE_ID, 5L);
    }

    @Test
    @DisplayName("새 요청은 PENDING으로 시작하고 처리 시각·반려 사유가 없다")
    void newRequest_startsPending() {
        StorePriceVerification verification = pending();

        assertThat(verification.getStatus()).isEqualTo(StorePriceVerificationStatus.PENDING);
        assertThat(verification.getProcessedAt()).isNull();
        assertThat(verification.getRejectReason()).isNull();
    }

    @Test
    @DisplayName("검수 착수는 PENDING에서만 가능하다")
    void startReview_onlyFromPending() {
        StorePriceVerification verification = pending();
        verification.startReview(NOW);

        assertThat(verification.getStatus()).isEqualTo(StorePriceVerificationStatus.IN_PROGRESS);
        assertThat(verification.getProcessedAt()).isEqualTo(NOW);

        // 이미 진행 중인 요청을 다시 착수시키지 않는다.
        assertThatThrownBy(() -> verification.startReview(NOW))
            .isInstanceOf(BusinessException.class)
            .satisfies(thrown -> assertThat(((BusinessException) thrown).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_NOT_PENDING));
    }

    @Test
    @DisplayName("검수 중 상태에서도 승인할 수 있다")
    void approve_fromInProgress() {
        StorePriceVerification verification = pending();
        verification.startReview(NOW);

        assertThatCode(() -> verification.approve(NOW.plusHours(1))).doesNotThrowAnyException();
        assertThat(verification.getStatus()).isEqualTo(StorePriceVerificationStatus.APPROVED);
    }

    @Test
    @DisplayName("반려는 사유를 보관한다")
    void reject_keepsReason() {
        StorePriceVerification verification = pending();

        verification.reject("가격표가 실제 가게 메뉴판이 아닙니다.", NOW);

        assertThat(verification.getStatus()).isEqualTo(StorePriceVerificationStatus.REJECTED);
        assertThat(verification.getRejectReason()).isEqualTo("가격표가 실제 가게 메뉴판이 아닙니다.");
    }

    @Test
    @DisplayName("취소는 사유 없는 종결이다")
    void cancel_clearsReason() {
        StorePriceVerification verification = pending();

        verification.cancel(NOW);

        assertThat(verification.getStatus()).isEqualTo(StorePriceVerificationStatus.CANCELED);
        assertThat(verification.getRejectReason()).isNull();
    }

    @Test
    @DisplayName("종결된 요청은 다시 전이할 수 없다 — 검수 결과를 덮어쓰지 못하게 한다")
    void closedRequest_cannotTransitionAgain() {
        StorePriceVerification rejected = pending();
        rejected.reject("사유", NOW);

        assertThatThrownBy(() -> rejected.approve(NOW))
            .isInstanceOf(BusinessException.class)
            .satisfies(thrown -> assertThat(((BusinessException) thrown).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_NOT_PENDING));

        StorePriceVerification approved = pending();
        approved.approve(NOW);

        assertThatThrownBy(() -> approved.reject("뒤늦은 반려", NOW))
            .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> approved.cancel(NOW))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("승인은 이전 반려 사유를 비운다")
    void approve_clearsStaleRejectReason() {
        // 대기 상태에서 곧바로 승인되는 정상 경로에서도 반려 사유가 남지 않아야 한다.
        StorePriceVerification verification = pending();

        verification.approve(NOW);

        assertThat(verification.getRejectReason()).isNull();
    }

    @Test
    @DisplayName("isOpen은 대기·진행만 true다 — 재요청 차단 판정의 근거다")
    void isOpen_coversPendingAndInProgress() {
        assertThat(StorePriceVerificationStatus.PENDING.isOpen()).isTrue();
        assertThat(StorePriceVerificationStatus.IN_PROGRESS.isOpen()).isTrue();
        assertThat(StorePriceVerificationStatus.APPROVED.isOpen()).isFalse();
        assertThat(StorePriceVerificationStatus.REJECTED.isOpen()).isFalse();
        assertThat(StorePriceVerificationStatus.CANCELED.isOpen()).isFalse();
    }

    @Test
    @DisplayName("알 수 없는 상태 코드는 400 ErrorCode로 변환된다")
    void from_withUnknownCode_throwsBusinessException() {
        assertThatThrownBy(() -> StorePriceVerificationStatus.from("UNKNOWN"))
            .isInstanceOf(BusinessException.class)
            .satisfies(thrown -> assertThat(((BusinessException) thrown).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_STATUS_UNKNOWN));
    }
}
