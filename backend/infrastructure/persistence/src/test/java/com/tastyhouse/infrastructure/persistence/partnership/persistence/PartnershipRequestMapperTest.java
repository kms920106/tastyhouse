package com.tastyhouse.infrastructure.persistence.partnership.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.partnership.model.PartnershipRequest;
import com.tastyhouse.domain.partnership.model.PartnershipStatus;

import static org.assertj.core.api.Assertions.assertThat;

class PartnershipRequestMapperTest {

    @Test
    @DisplayName("PartnershipRequest → 엔티티 변환 시 모든 컬럼 값이 옮겨진다")
    void toEntityCopiesColumns() {
        PartnershipRequestJpaEntity entity = PartnershipRequestMapper.toEntity(original());

        assertThat(entity.getBusinessName()).isEqualTo("상호명");
        assertThat(entity.getAddress()).isEqualTo("서울시 강남구");
        assertThat(entity.getAddressDetail()).isEqualTo("3층");
        assertThat(entity.getContactName()).isEqualTo("홍길동");
        assertThat(entity.getContactPhone()).isEqualTo("010-1234-5678");
        assertThat(entity.getConsultationRequestedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 10, 0));
        assertThat(entity.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(entity.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("엔티티 → PartnershipRequest 변환 시 id·생성일·수정일을 포함한 모든 필드가 복원된다")
    void toDomainRestoresAllFields() {
        PartnershipRequest original = original();
        PartnershipRequestJpaEntity entity = PartnershipRequestMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", original.getId());
        ReflectionTestUtils.setField(entity, "createdAt", original.getCreatedAt());
        ReflectionTestUtils.setField(entity, "updatedAt", original.getUpdatedAt());

        PartnershipRequest restored = PartnershipRequestMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("applyChanges는 상태와 삭제 여부를 옮긴다")
    void applyChangesCopiesWritableFields() {
        PartnershipRequestJpaEntity entity = PartnershipRequestMapper.toEntity(original());

        PartnershipRequestMapper.applyChanges(entity, PartnershipRequest.reconstitute(
            21L, "상호명", "서울시 강남구", "3층", "홍길동", "010-1234-5678",
            LocalDateTime.of(2026, 1, 1, 10, 0),
            PartnershipStatus.COMPLETED, false, null, null));

        assertThat(entity.getStatus()).isEqualTo("COMPLETED");
        assertThat(entity.isDeleted()).isFalse();
    }

    private static PartnershipRequest original() {
        return PartnershipRequest.reconstitute(
            21L, "상호명", "서울시 강남구", "3층", "홍길동", "010-1234-5678",
            LocalDateTime.of(2026, 1, 1, 10, 0),
            PartnershipStatus.IN_PROGRESS, true,
            LocalDateTime.of(2026, 2, 1, 11, 0),
            LocalDateTime.of(2026, 3, 1, 12, 0));
    }
}
