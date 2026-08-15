package com.tastyhouse.domain.shop.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 상태 전이 규칙만 검증한다.
 */
class ShopDeliveryAreaAdjustmentRequestTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 PENDING 상태다")
    void of_createsTransientRequest() {
        ShopDeliveryAreaAdjustmentRequest request = newRequest();

        assertThat(request.getId()).isNull();
        assertThat(request.getShopId()).isEqualTo(ShopId.of(1L));
        assertThat(request.getCounterpartShopName()).isEqualTo("맛있는집 강남점");
        assertThat(request.getCounterpartBusinessNumber()).isEqualTo("1234567890");
        assertThat(request.getFranchiseName()).isEqualTo("맛있는집 본사");
        assertThat(request.getReason()).isEqualTo("역삼1동 전역이 중첩됩니다.");
        assertThat(request.getConsentFileId()).isEqualTo(UploadedFileId.of(100L));
        assertThat(request.getStatus()).isEqualTo(DeliveryAreaAdjustmentStatus.PENDING);
        assertThat(request.getRejectReason()).isNull();
        assertThat(request.getCreatedAt()).isNull();
        assertThat(request.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("PENDING 상태에서 startProgress하면 IN_PROGRESS로 전이한다")
    void startProgress_onPending_changesToInProgress() {
        ShopDeliveryAreaAdjustmentRequest request = newRequest();

        request.startProgress();

        assertThat(request.getStatus()).isEqualTo(DeliveryAreaAdjustmentStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("PENDING이 아니면 startProgress는 실패한다")
    void startProgress_onNonPending_throws() {
        ShopDeliveryAreaAdjustmentRequest request = newRequest();
        request.startProgress();

        assertThatThrownBy(request::startProgress)
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("IN_PROGRESS 상태에서 complete하면 COMPLETED로 전이한다")
    void complete_onInProgress_changesToCompleted() {
        ShopDeliveryAreaAdjustmentRequest request = newRequest();
        request.startProgress();

        request.complete();

        assertThat(request.getStatus()).isEqualTo(DeliveryAreaAdjustmentStatus.COMPLETED);
    }

    @Test
    @DisplayName("PENDING 상태에서 바로 complete하면 실패한다 — 조정 중을 거쳐야 한다")
    void complete_onPending_throws() {
        ShopDeliveryAreaAdjustmentRequest request = newRequest();

        assertThatThrownBy(request::complete)
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("PENDING 상태에서 바로 reject할 수 있다 — 가맹본부가 조정절차를 개시하지 않는 경우")
    void reject_onPending_changesToRejected() {
        ShopDeliveryAreaAdjustmentRequest request = newRequest();

        request.reject("조정이 필요하지 않음이 명백합니다.");

        assertThat(request.getStatus()).isEqualTo(DeliveryAreaAdjustmentStatus.REJECTED);
        assertThat(request.getRejectReason()).isEqualTo("조정이 필요하지 않음이 명백합니다.");
    }

    @Test
    @DisplayName("IN_PROGRESS 상태에서도 reject할 수 있다 — 조정 불성립")
    void reject_onInProgress_changesToRejected() {
        ShopDeliveryAreaAdjustmentRequest request = newRequest();
        request.startProgress();

        request.reject("조정이 성립되지 않았습니다.");

        assertThat(request.getStatus()).isEqualTo(DeliveryAreaAdjustmentStatus.REJECTED);
    }

    @Test
    @DisplayName("이미 종결(COMPLETED)된 신청은 reject할 수 없다")
    void reject_onCompleted_throws() {
        ShopDeliveryAreaAdjustmentRequest request = newRequest();
        request.startProgress();
        request.complete();

        assertThatThrownBy(() -> request.reject("사유"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("이미 종결(REJECTED)된 신청은 다시 reject할 수 없다")
    void reject_onRejected_throws() {
        ShopDeliveryAreaAdjustmentRequest request = newRequest();
        request.reject("첫 반려");

        assertThatThrownBy(() -> request.reject("두 번째 반려"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("반려된 신청은 조정 절차를 시작할 수 없다")
    void startProgress_onRejected_throws() {
        ShopDeliveryAreaAdjustmentRequest request = newRequest();
        request.reject("형식 미비");

        assertThatThrownBy(request::startProgress)
            .isInstanceOf(BusinessException.class);
    }

    private ShopDeliveryAreaAdjustmentRequest newRequest() {
        return ShopDeliveryAreaAdjustmentRequest.of(
            ShopId.of(1L),
            "맛있는집 강남점",
            "1234567890",
            "맛있는집 본사",
            "역삼1동 전역이 중첩됩니다.",
            UploadedFileId.of(100L)
        );
    }
}
