package com.tastyhouse.core.domain.partnership.domain.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.core.domain.partnership.domain.vo.PartnershipRequestId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class PartnershipRequestTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 PENDING·미삭제 상태다")
    void of_createsTransientPartnershipRequest() {
        LocalDateTime consultationRequestedAt = LocalDateTime.of(2026, 7, 1, 10, 0);

        PartnershipRequest partnershipRequest = PartnershipRequest.of(
            "사업체명", "주소", "상세주소", "담당자", "010-1234-5678", consultationRequestedAt
        );

        assertThat(partnershipRequest.getId()).isNull();
        assertThat(partnershipRequest.getBusinessName()).isEqualTo("사업체명");
        assertThat(partnershipRequest.getAddress()).isEqualTo("주소");
        assertThat(partnershipRequest.getAddressDetail()).isEqualTo("상세주소");
        assertThat(partnershipRequest.getContactName()).isEqualTo("담당자");
        assertThat(partnershipRequest.getContactPhone()).isEqualTo("010-1234-5678");
        assertThat(partnershipRequest.getConsultationRequestedAt()).isEqualTo(consultationRequestedAt);
        assertThat(partnershipRequest.getStatus()).isEqualTo(PartnershipStatus.PENDING);
        assertThat(partnershipRequest.isDeleted()).isFalse();
        assertThat(partnershipRequest.getCreatedAt()).isNull();
        assertThat(partnershipRequest.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("changeStatus는 처리 상태를 변경한다")
    void changeStatus_changesStatus() {
        PartnershipRequest partnershipRequest = PartnershipRequest.of(
            "사업체명", "주소", "상세주소", "담당자", "010-1234-5678", LocalDateTime.now()
        );

        partnershipRequest.changeStatus(PartnershipStatus.IN_PROGRESS);
        assertThat(partnershipRequest.getStatus()).isEqualTo(PartnershipStatus.IN_PROGRESS);

        partnershipRequest.changeStatus(PartnershipStatus.COMPLETED);
        assertThat(partnershipRequest.getStatus()).isEqualTo(PartnershipStatus.COMPLETED);
    }

    @Test
    @DisplayName("delete는 삭제 플래그를 true로 만든다(soft delete)")
    void delete_marksDeleted() {
        PartnershipRequest partnershipRequest = PartnershipRequest.of(
            "사업체명", "주소", "상세주소", "담당자", "010-1234-5678", LocalDateTime.now()
        );

        partnershipRequest.delete();

        assertThat(partnershipRequest.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime consultationRequestedAt = LocalDateTime.of(2026, 7, 1, 10, 0);
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 0, 0);

        PartnershipRequest partnershipRequest = PartnershipRequest.reconstitute(
            1L, "사업체명", "주소", "상세주소", "담당자", "010-1234-5678",
            consultationRequestedAt, PartnershipStatus.IN_PROGRESS, false, createdAt, updatedAt
        );

        assertThat(partnershipRequest.getId()).isEqualTo(1L);
        assertThat(partnershipRequest.getPartnershipRequestId()).isEqualTo(PartnershipRequestId.of(1L));
        assertThat(partnershipRequest.getStatus()).isEqualTo(PartnershipStatus.IN_PROGRESS);
        assertThat(partnershipRequest.getCreatedAt()).isEqualTo(createdAt);
        assertThat(partnershipRequest.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("미영속 상태에서 getPartnershipRequestId를 호출하면 PartnershipRequestId 불변식 위반으로 예외가 발생한다")
    void getPartnershipRequestId_onTransient_throws() {
        PartnershipRequest partnershipRequest = PartnershipRequest.of(
            "사업체명", "주소", "상세주소", "담당자", "010-1234-5678", LocalDateTime.now()
        );

        assertThatThrownBy(partnershipRequest::getPartnershipRequestId)
            .isInstanceOf(IllegalArgumentException.class);
    }
}
