package com.tastyhouse.domain.sms.domain.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.sms.domain.vo.SmsVerificationId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.vo.PhoneNumber;
import com.tastyhouse.domain.shared.vo.VerificationCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class SmsVerificationTest {

    @Test
    @DisplayName("create로 생성하면 미영속 상태(식별자 없음)이고 PENDING 상태의 6자리 인증코드를 갖는다")
    void create_createsTransientSmsVerification() {
        SmsVerification verification = SmsVerification.create("01012345678");

        assertThat(verification.getId()).isNull();
        assertThat(verification.getPhoneNumber()).isEqualTo(new PhoneNumber("01012345678"));
        assertThat(verification.getStatus()).isEqualTo(SmsVerificationStatus.PENDING);
        assertThat(verification.getVerificationCode().value()).matches("^[0-9]{6}$");
        assertThat(verification.getVerifiedAt()).isNull();
        assertThat(verification.getCreatedAt()).isNotNull();
        assertThat(verification.getExpiresAt()).isAfter(verification.getCreatedAt());
    }

    @Test
    @DisplayName("verify는 올바른 코드로 만료 전에 호출하면 VERIFIED 상태로 전이하고 인증 시각을 기록한다")
    void verify_withCorrectCodeBeforeExpiry_marksVerified() {
        SmsVerification verification = SmsVerification.create("01012345678");
        LocalDateTime now = verification.getCreatedAt().plusMinutes(1);

        verification.verify(verification.getVerificationCode(), now);

        assertThat(verification.getStatus()).isEqualTo(SmsVerificationStatus.VERIFIED);
        assertThat(verification.getVerifiedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("verify는 만료 시각 이후에 호출하면 EXPIRED로 전이하고 예외를 던진다")
    void verify_afterExpiry_marksExpiredAndThrows() {
        SmsVerification verification = SmsVerification.create("01012345678");
        LocalDateTime afterExpiry = verification.getExpiresAt().plusSeconds(1);

        assertThatThrownBy(() -> verification.verify(verification.getVerificationCode(), afterExpiry))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SMS_VERIFICATION_CODE_EXPIRED);
        assertThat(verification.getStatus()).isEqualTo(SmsVerificationStatus.EXPIRED);
    }

    @Test
    @DisplayName("verify는 코드가 일치하지 않으면 예외를 던지고 상태를 바꾸지 않는다")
    void verify_withMismatchedCode_throws() {
        SmsVerification verification = SmsVerification.create("01012345678");
        LocalDateTime now = verification.getCreatedAt().plusMinutes(1);

        assertThatThrownBy(() -> verification.verify(VerificationCode.of("000000"), now))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SMS_VERIFICATION_CODE_MISMATCH);
        assertThat(verification.getStatus()).isEqualTo(SmsVerificationStatus.PENDING);
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime expiresAt = createdAt.plusMinutes(5);
        LocalDateTime verifiedAt = createdAt.plusMinutes(1);
        VerificationCode code = VerificationCode.of("123456");
        PhoneNumber phoneNumber = new PhoneNumber("01012345678");

        SmsVerification verification = SmsVerification.reconstitute(
            1L, phoneNumber, code, SmsVerificationStatus.VERIFIED, expiresAt, verifiedAt, createdAt
        );

        assertThat(verification.getId()).isEqualTo(1L);
        assertThat(verification.getSmsVerificationId()).isEqualTo(SmsVerificationId.of(1L));
        assertThat(verification.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(verification.getVerificationCode()).isEqualTo(code);
        assertThat(verification.getStatus()).isEqualTo(SmsVerificationStatus.VERIFIED);
        assertThat(verification.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(verification.getVerifiedAt()).isEqualTo(verifiedAt);
        assertThat(verification.getCreatedAt()).isEqualTo(createdAt);
    }
}
