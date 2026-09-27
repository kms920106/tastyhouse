package com.tastyhouse.application.partnership.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.partnership.model.PartnershipRequest;
import com.tastyhouse.domain.partnership.model.PartnershipStatus;

import static org.assertj.core.api.Assertions.assertThat;

class PartnershipRequestStateMapperTest {

    @Test
    @DisplayName("PartnershipRequest → PartnershipRequestState → PartnershipRequest 왕복 시 모든 필드가 보존된다")
    void roundTrip() {
        PartnershipRequest original = PartnershipRequest.reconstitute(
            21L, "상호명", "서울시 강남구", "3층", "홍길동", "010-1234-5678",
            LocalDateTime.of(2026, 1, 1, 10, 0),
            PartnershipStatus.IN_PROGRESS, true,
            LocalDateTime.of(2026, 2, 1, 11, 0),
            LocalDateTime.of(2026, 3, 1, 12, 0));

        PartnershipRequest restored = PartnershipRequestStateMapper.toDomain(PartnershipRequestStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
