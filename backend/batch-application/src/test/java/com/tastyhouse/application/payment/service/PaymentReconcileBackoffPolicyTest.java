package com.tastyhouse.application.payment.service;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentReconcileBackoffPolicyTest {

    private static final LocalDateTime DUE_AT = LocalDateTime.of(2026, 10, 18, 9, 0);

    @ParameterizedTest(name = "이전 시도 {0}회 → {1}분 뒤")
    @CsvSource({
        "0, 9",
        "1, 19",
        "2, 39",
        "7, 1279",
        "8, 1439",
        "9, 1439",
        "100, 1439"
    })
    @DisplayName("다음 대사 시각은 10분에서 두 배씩 늘고 24시간에서 멈추며, 1분 여유를 뺀다")
    void nextAttemptAt_doublesFromTenMinutesUpToOneDay(int attemptsSoFar, long expectedMinutes) {
        assertThat(PaymentReconcileBackoffPolicy.nextAttemptAt(attemptsSoFar, DUE_AT, DUE_AT))
            .isEqualTo(DUE_AT.plusMinutes(expectedMinutes));
    }

    @ParameterizedTest(name = "이전 시도 {0}회, 실행 시작 {1}초 뒤 선점 → 실행 시작 {2}초 뒤")
    @CsvSource({
        "0, 90, 540",
        "0, 450, 570",
        "0, 510, 630",
        "3, 3000, 4740"
    })
    @DisplayName("선점이 늦지 않으면 실행 시작 시각 격자를, 늦으면 선점 시각+2분을 따른다")
    void nextAttemptAt_followsRunGridUnlessClaimedLate(int attemptsSoFar, long claimedAfterSeconds, long expectedSeconds) {
        LocalDateTime claimedAt = DUE_AT.plusSeconds(claimedAfterSeconds);

        assertThat(PaymentReconcileBackoffPolicy.nextAttemptAt(attemptsSoFar, DUE_AT, claimedAt))
            .isEqualTo(DUE_AT.plusSeconds(expectedSeconds));
    }
}
