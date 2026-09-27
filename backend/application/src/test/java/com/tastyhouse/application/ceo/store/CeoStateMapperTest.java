package com.tastyhouse.application.ceo.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.ceo.model.Ceo;
import com.tastyhouse.domain.ceo.model.CeoLoginFailureReason;
import com.tastyhouse.domain.ceo.model.CeoLoginHistory;
import com.tastyhouse.domain.ceo.model.CeoLoginResult;
import com.tastyhouse.domain.ceo.model.CeoReplyPhrase;
import com.tastyhouse.domain.ceo.model.CeoStatus;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shared.vo.PhoneNumber;

import static org.assertj.core.api.Assertions.assertThat;

class CeoStateMapperTest {

    @Test
    @DisplayName("Ceo → CeoState → Ceo 왕복 시 모든 필드가 보존된다")
    void ceoRoundTrip() {
        Ceo original = Ceo.reconstitute(
            1L, "ceo_user", "{bcrypt}hash", "홍길동", "123-45-67890",
            new PhoneNumber("01055556666"), "ceo@example.com", CeoStatus.INACTIVE);

        Ceo restored = CeoStateMapper.toDomain(CeoStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("Ceo의 nullable 필드가 null이어도 왕복된다")
    void ceoRoundTripWithNulls() {
        Ceo original = Ceo.reconstitute(
            2L, "ceo2", "pw", "이름", null, null, null, CeoStatus.ACTIVE);

        Ceo restored = CeoStateMapper.toDomain(CeoStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("CeoLoginHistory → CeoLoginHistoryState → CeoLoginHistory 왕복 시 모든 필드가 보존된다")
    void loginHistoryRoundTrip() {
        CeoLoginHistory original = CeoLoginHistory.reconstitute(
            3L, CeoId.of(4L), CeoLoginResult.FAILURE, CeoLoginFailureReason.ACCOUNT_INACTIVE,
            "10.0.0.1", "Mozilla/5.0", LocalDateTime.of(2026, 1, 2, 3, 4, 5));

        CeoLoginHistory restored = CeoLoginHistoryStateMapper.toDomain(CeoLoginHistoryStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("CeoReplyPhrase → CeoReplyPhraseState → CeoReplyPhrase 왕복 시 모든 필드가 보존된다")
    void replyPhraseRoundTrip() {
        CeoReplyPhrase original = CeoReplyPhrase.reconstitute(
            5L, CeoId.of(6L), "감사 인사", "방문해 주셔서 감사합니다", 3,
            LocalDateTime.of(2026, 2, 3, 4, 5, 6),
            LocalDateTime.of(2026, 3, 4, 5, 6, 7));

        CeoReplyPhrase restored = CeoReplyPhraseStateMapper.toDomain(CeoReplyPhraseStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
