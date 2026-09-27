package com.tastyhouse.application.mail.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.mail.model.MailVerification;
import com.tastyhouse.domain.mail.model.MailVerificationStatus;
import com.tastyhouse.domain.shared.vo.VerificationCode;

import static org.assertj.core.api.Assertions.assertThat;

class MailVerificationStateMapperTest {

    @Test
    @DisplayName("MailVerification → MailVerificationState → MailVerification 왕복 시 모든 필드가 보존된다")
    void roundTrip() {
        MailVerification original = MailVerification.reconstitute(
            5L, "user@example.com", VerificationCode.of("123456"), MailVerificationStatus.VERIFIED,
            LocalDateTime.of(2026, 1, 2, 3, 4, 5),
            LocalDateTime.of(2026, 2, 3, 4, 5, 6),
            LocalDateTime.of(2026, 3, 4, 5, 6, 7));

        MailVerification restored = MailVerificationStateMapper.toDomain(MailVerificationStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("verifiedAt이 null이어도 왕복된다")
    void roundTripWithNullVerifiedAt() {
        MailVerification original = MailVerification.reconstitute(
            6L, "other@example.com", VerificationCode.of("654321"), MailVerificationStatus.PENDING,
            LocalDateTime.of(2026, 4, 5, 6, 7, 8),
            null,
            LocalDateTime.of(2026, 5, 6, 7, 8, 9));

        MailVerification restored = MailVerificationStateMapper.toDomain(MailVerificationStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
