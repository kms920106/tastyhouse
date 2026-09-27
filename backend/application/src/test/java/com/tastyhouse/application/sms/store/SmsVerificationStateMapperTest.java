package com.tastyhouse.application.sms.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shared.vo.PhoneNumber;
import com.tastyhouse.domain.shared.vo.VerificationCode;
import com.tastyhouse.domain.sms.model.SmsVerification;
import com.tastyhouse.domain.sms.model.SmsVerificationStatus;

import static org.assertj.core.api.Assertions.assertThat;

class SmsVerificationStateMapperTest {

    @Test
    @DisplayName("SmsVerification → SmsVerificationState → SmsVerification 왕복 시 모든 필드가 보존된다")
    void roundTrip() {
        SmsVerification original = SmsVerification.reconstitute(
            9L, new PhoneNumber("01012345678"), VerificationCode.of("246801"), SmsVerificationStatus.VERIFIED,
            LocalDateTime.of(2026, 1, 2, 3, 4, 5),
            LocalDateTime.of(2026, 2, 3, 4, 5, 6),
            LocalDateTime.of(2026, 3, 4, 5, 6, 7));

        SmsVerification restored = SmsVerificationStateMapper.toDomain(SmsVerificationStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("verifiedAt이 null이어도 왕복된다")
    void roundTripWithNullVerifiedAt() {
        SmsVerification original = SmsVerification.reconstitute(
            10L, new PhoneNumber("01098765432"), VerificationCode.of("135790"), SmsVerificationStatus.PENDING,
            LocalDateTime.of(2026, 4, 5, 6, 7, 8),
            null,
            LocalDateTime.of(2026, 5, 6, 7, 8, 9));

        SmsVerification restored = SmsVerificationStateMapper.toDomain(SmsVerificationStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
