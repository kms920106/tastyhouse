package com.tastyhouse.domain.shop.domain.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.tastyhouse.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.domain.shop.domain.vo.ShopId;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class ShopImageChangeRequestTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 PENDING 상태다")
    void of_createsTransientShopImageChangeRequest() {
        ShopImageChangeRequest request = ShopImageChangeRequest.of(ShopId.of(1L), ShopImageType.TRADEMARK, UploadedFileId.of(100L));

        assertThat(request.getId()).isNull();
        assertThat(request.getShopId()).isEqualTo(ShopId.of(1L));
        assertThat(request.getImageType()).isEqualTo(ShopImageType.TRADEMARK);
        assertThat(request.getImageFileId()).isEqualTo(UploadedFileId.of(100L));
        assertThat(request.getStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(request.getRejectReason()).isNull();
        assertThat(request.getCreatedAt()).isNull();
        assertThat(request.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("PENDING 상태에서 approve하면 APPROVED로 전이한다")
    void approve_onPending_changesToApproved() {
        ShopImageChangeRequest request = ShopImageChangeRequest.of(ShopId.of(1L), ShopImageType.THUMBNAIL, UploadedFileId.of(100L));

        request.approve();

        assertThat(request.getStatus()).isEqualTo(ApprovalStatus.APPROVED);
    }

    @Test
    @DisplayName("PENDING이 아닌 상태에서 approve하면 예외가 발생한다")
    void approve_onNonPending_throws() {
        ShopImageChangeRequest request = ShopImageChangeRequest.of(ShopId.of(1L), ShopImageType.THUMBNAIL, UploadedFileId.of(100L));
        request.approve();

        assertThatThrownBy(request::approve)
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("PENDING 상태에서 reject하면 REJECTED로 전이하고 반려 사유를 설정한다")
    void reject_onPending_changesToRejectedWithReason() {
        ShopImageChangeRequest request = ShopImageChangeRequest.of(ShopId.of(1L), ShopImageType.TRADEMARK, UploadedFileId.of(100L));

        request.reject("이미지 규격 미충족");

        assertThat(request.getStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(request.getRejectReason()).isEqualTo("이미지 규격 미충족");
    }

    @Test
    @DisplayName("PENDING이 아닌 상태에서 reject하면 예외가 발생한다")
    void reject_onNonPending_throws() {
        ShopImageChangeRequest request = ShopImageChangeRequest.of(ShopId.of(1L), ShopImageType.TRADEMARK, UploadedFileId.of(100L));
        request.reject("1차 반려");

        assertThatThrownBy(() -> request.reject("2차 반려"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("ShopImageType.from은 유효한 코드를 enum으로 변환한다")
    void shopImageType_from_validCode() {
        assertThat(ShopImageType.from("TRADEMARK")).isEqualTo(ShopImageType.TRADEMARK);
        assertThat(ShopImageType.from("THUMBNAIL")).isEqualTo(ShopImageType.THUMBNAIL);
    }

    @Test
    @DisplayName("ShopImageType.from은 알 수 없는 코드에 대해 BusinessException을 던진다")
    void shopImageType_from_invalidCode_throws() {
        assertThatThrownBy(() -> ShopImageType.from("UNKNOWN"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ShopImageChangeRequest request = ShopImageChangeRequest.reconstitute(
            1L, ShopId.of(2L), ShopImageType.TRADEMARK, UploadedFileId.of(100L), ApprovalStatus.REJECTED, "사유",
            LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 1, 2, 0, 0)
        );

        assertThat(request.getId()).isEqualTo(1L);
        assertThat(request.getShopId()).isEqualTo(ShopId.of(2L));
        assertThat(request.getStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(request.getRejectReason()).isEqualTo("사유");
        assertThat(request.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
        assertThat(request.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 1, 2, 0, 0));
    }
}
