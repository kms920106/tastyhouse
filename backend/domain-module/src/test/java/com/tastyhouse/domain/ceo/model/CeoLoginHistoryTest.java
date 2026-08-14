package com.tastyhouse.domain.ceo.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.ceo.vo.CeoId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 점주 로그인 이력 순수 도메인 모델 단위 테스트.
 */
class CeoLoginHistoryTest {

    @Test
    @DisplayName("of는 식별자·생성시각 없이 신규 이력을 만든다")
    void of_createsNewHistoryWithoutIdAndCreatedAt() {
        CeoLoginHistory history = CeoLoginHistory.of(
            CeoId.of(7L),
            CeoLoginResult.SUCCESS,
            null,
            "121.130.11.24",
            "Mozilla/5.0"
        );

        assertThat(history.getId()).isNull();
        assertThat(history.getCreatedAt()).isNull();
        assertThat(history.getCeoId()).isEqualTo(CeoId.of(7L));
        assertThat(history.getResult()).isEqualTo(CeoLoginResult.SUCCESS);
        assertThat(history.getFailureReason()).isNull();
        assertThat(history.getIpAddress()).isEqualTo("121.130.11.24");
        assertThat(history.getUserAgent()).isEqualTo("Mozilla/5.0");
    }

    @Test
    @DisplayName("of는 실패 사유를 그대로 담는다")
    void of_keepsFailureReason() {
        CeoLoginHistory history = CeoLoginHistory.of(
            CeoId.of(7L),
            CeoLoginResult.FAILURE,
            CeoLoginFailureReason.BAD_CREDENTIALS,
            null,
            null
        );

        assertThat(history.getResult()).isEqualTo(CeoLoginResult.FAILURE);
        assertThat(history.getFailureReason()).isEqualTo(CeoLoginFailureReason.BAD_CREDENTIALS);
        assertThat(history.getIpAddress()).isNull();
        assertThat(history.getUserAgent()).isNull();
    }

    @Test
    @DisplayName("reconstitute는 저장된 식별자·생성시각까지 복원한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 14, 9, 12, 41);

        CeoLoginHistory history = CeoLoginHistory.reconstitute(
            1024L,
            CeoId.of(7L),
            CeoLoginResult.FAILURE,
            CeoLoginFailureReason.ACCOUNT_INACTIVE,
            "10.0.0.1",
            "curl/8.4.0",
            createdAt
        );

        assertThat(history.getId()).isEqualTo(1024L);
        assertThat(history.getCeoId()).isEqualTo(CeoId.of(7L));
        assertThat(history.getResult()).isEqualTo(CeoLoginResult.FAILURE);
        assertThat(history.getFailureReason()).isEqualTo(CeoLoginFailureReason.ACCOUNT_INACTIVE);
        assertThat(history.getIpAddress()).isEqualTo("10.0.0.1");
        assertThat(history.getUserAgent()).isEqualTo("curl/8.4.0");
        assertThat(history.getCreatedAt()).isEqualTo(createdAt);
    }
}
