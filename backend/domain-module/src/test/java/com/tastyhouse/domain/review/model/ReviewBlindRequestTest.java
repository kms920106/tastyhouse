package com.tastyhouse.domain.review.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * 게시중단 요청 애그리거트의 상태 전이 규칙.
 *
 * <p>{@code blindUntil}을 파라미터로 받게 설계했으므로 <b>과거 시각을 주입해 만료를 즉시 재현</b>할 수
 * 있다 — cron을 기다리거나 시계를 조작할 필요가 없다.
 */
class ReviewBlindRequestTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 17, 10, 0);

    private ReviewBlindRequest pendingRequest() {
        return ReviewBlindRequest.of(
            ReviewId.of(1L),
            ShopId.of(2L),
            CeoId.of(3L),
            ReviewBlindReason.PROFANITY,
            null
        );
    }

    private ReviewBlindRequest approvedRequest() {
        ReviewBlindRequest request = pendingRequest();
        request.approve(NOW.plusDays(ReviewBlindRequest.BLIND_PERIOD_DAYS));
        return request;
    }

    @Nested
    @DisplayName("승인")
    class Approve {

        @Test
        @DisplayName("승인하면 게시중단 상태가 되고 재노출 기한이 설정된다")
        void approveSetsBlindUntil() {
            ReviewBlindRequest request = pendingRequest();
            LocalDateTime blindUntil = NOW.plusDays(ReviewBlindRequest.BLIND_PERIOD_DAYS);

            request.approve(blindUntil);

            assertThat(request.getStatus()).isEqualTo(ReviewBlindStatus.APPROVED);
            assertThat(request.getBlindUntil()).isEqualTo(blindUntil);
        }

        @Test
        @DisplayName("게시중단 기간은 30일이다")
        void blindPeriodIsThirtyDays() {
            assertThat(ReviewBlindRequest.BLIND_PERIOD_DAYS).isEqualTo(30);
        }

        @Test
        @DisplayName("대기중이 아니면 승인할 수 없다")
        void cannotApproveWhenNotPending() {
            ReviewBlindRequest request = approvedRequest();

            assertThatThrownBy(() -> request.approve(NOW))
                .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("만료 재노출")
    class Expire {

        @Test
        @DisplayName("게시중단 상태에서 만료하면 재노출 상태가 되고 기한이 비워진다")
        void expireClearsBlindUntil() {
            ReviewBlindRequest request = approvedRequest();

            request.expire();

            assertThat(request.getStatus()).isEqualTo(ReviewBlindStatus.EXPIRED);
            assertThat(request.getBlindUntil()).isNull();
        }

        @Test
        @DisplayName("대기중인 요청은 만료할 수 없다")
        void cannotExpireWhenPending() {
            ReviewBlindRequest request = pendingRequest();

            BusinessException exception = catchThrowableOfType(request::expire, BusinessException.class);

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_BLIND_REQUEST_NOT_APPROVED);
        }

        @Test
        @DisplayName("이미 만료된 요청은 다시 만료할 수 없다(배치 재실행 안전)")
        void cannotExpireTwice() {
            ReviewBlindRequest request = approvedRequest();
            request.expire();

            assertThatThrownBy(request::expire)
                .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("고객 동의 삭제")
    class DeleteByConsent {

        @Test
        @DisplayName("게시중단 상태에서 동의하면 삭제 상태가 되고 기한이 비워진다")
        void deleteByConsentClearsBlindUntil() {
            ReviewBlindRequest request = approvedRequest();

            request.deleteByConsent();

            assertThat(request.getStatus()).isEqualTo(ReviewBlindStatus.DELETED);
            assertThat(request.getBlindUntil()).isNull();
        }

        @Test
        @DisplayName("게시중단 상태가 아니면 동의 삭제할 수 없다")
        void cannotDeleteWhenNotApproved() {
            ReviewBlindRequest request = pendingRequest();

            BusinessException exception = catchThrowableOfType(
                request::deleteByConsent, BusinessException.class
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_BLIND_REQUEST_NOT_APPROVED);
        }

        @Test
        @DisplayName("반려된 요청은 동의 삭제할 수 없다")
        void cannotDeleteWhenRejected() {
            ReviewBlindRequest request = pendingRequest();
            request.reject("사유 없음");

            assertThatThrownBy(request::deleteByConsent)
                .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("취소")
    class Cancel {

        @Test
        @DisplayName("승인된 요청은 취소할 수 없다")
        void cannotCancelWhenApproved() {
            ReviewBlindRequest request = approvedRequest();

            assertThatThrownBy(request::cancel)
                .isInstanceOf(BusinessException.class);
        }
    }
}
