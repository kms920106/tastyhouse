package com.tastyhouse.infrastructure.jpa.sms.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.shared.vo.PhoneNumber;
import com.tastyhouse.domain.shared.vo.VerificationCode;
import com.tastyhouse.domain.sms.model.SmsVerification;
import com.tastyhouse.domain.sms.model.SmsVerificationStatus;
import com.tastyhouse.infrastructure.jpa.shared.persistence.PhoneNumberEmbeddable;
import com.tastyhouse.infrastructure.jpa.shared.persistence.VerificationCodeEmbeddable;

import static org.assertj.core.api.Assertions.assertThat;

class SmsVerificationMapperTest {

    @Test
    @DisplayName("SmsVerification → 엔티티 변환 시 VO는 Embeddable로, enum은 name으로 채워진다")
    void toEntity() {
        SmsVerification smsVerification = SmsVerification.reconstitute(
            9L, new PhoneNumber("01012345678"), VerificationCode.of("246801"), SmsVerificationStatus.VERIFIED,
            LocalDateTime.of(2026, 1, 2, 3, 4, 5),
            LocalDateTime.of(2026, 2, 3, 4, 5, 6),
            LocalDateTime.of(2026, 3, 4, 5, 6, 7));

        SmsVerificationJpaEntity entity = SmsVerificationMapper.toEntity(smsVerification);

        assertThat(entity.getPhoneNumber()).isEqualTo(new PhoneNumberEmbeddable("01012345678"));
        assertThat(entity.getVerificationCode()).isEqualTo(new VerificationCodeEmbeddable("246801"));
        assertThat(entity.getStatus()).isEqualTo("VERIFIED");
        assertThat(entity.getExpiresAt()).isEqualTo(LocalDateTime.of(2026, 1, 2, 3, 4, 5));
        assertThat(entity.getVerifiedAt()).isEqualTo(LocalDateTime.of(2026, 2, 3, 4, 5, 6));
        assertThat(entity.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 3, 4, 5, 6, 7));
    }

    @Test
    @DisplayName("verifiedAt이 null인 SmsVerification도 엔티티로 변환된다")
    void toEntityWithNullVerifiedAt() {
        SmsVerification smsVerification = SmsVerification.reconstitute(
            10L, new PhoneNumber("01098765432"), VerificationCode.of("135790"), SmsVerificationStatus.PENDING,
            LocalDateTime.of(2026, 4, 5, 6, 7, 8),
            null,
            LocalDateTime.of(2026, 5, 6, 7, 8, 9));

        SmsVerificationJpaEntity entity = SmsVerificationMapper.toEntity(smsVerification);

        assertThat(entity.getPhoneNumber()).isEqualTo(new PhoneNumberEmbeddable("01098765432"));
        assertThat(entity.getVerificationCode()).isEqualTo(new VerificationCodeEmbeddable("135790"));
        assertThat(entity.getStatus()).isEqualTo("PENDING");
        assertThat(entity.getExpiresAt()).isEqualTo(LocalDateTime.of(2026, 4, 5, 6, 7, 8));
        assertThat(entity.getVerifiedAt()).isNull();
        assertThat(entity.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 5, 6, 7, 8, 9));
    }

    @Test
    @DisplayName("엔티티 → SmsVerification 변환 시 id를 포함한 모든 필드가 복원된다")
    void toDomain() {
        SmsVerificationJpaEntity entity = SmsVerificationJpaEntity.create(
            new PhoneNumberEmbeddable("01012345678"), new VerificationCodeEmbeddable("246801"), "VERIFIED",
            LocalDateTime.of(2026, 1, 2, 3, 4, 5),
            LocalDateTime.of(2026, 2, 3, 4, 5, 6),
            LocalDateTime.of(2026, 3, 4, 5, 6, 7));
        ReflectionTestUtils.setField(entity, "id", 9L);

        SmsVerification smsVerification = SmsVerificationMapper.toDomain(entity);

        SmsVerification expected = SmsVerification.reconstitute(
            9L, new PhoneNumber("01012345678"), VerificationCode.of("246801"), SmsVerificationStatus.VERIFIED,
            LocalDateTime.of(2026, 1, 2, 3, 4, 5),
            LocalDateTime.of(2026, 2, 3, 4, 5, 6),
            LocalDateTime.of(2026, 3, 4, 5, 6, 7));
        assertThat(smsVerification).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @DisplayName("verifiedAt이 null인 엔티티도 SmsVerification으로 복원된다")
    void toDomainWithNullVerifiedAt() {
        SmsVerificationJpaEntity entity = SmsVerificationJpaEntity.create(
            new PhoneNumberEmbeddable("01098765432"), new VerificationCodeEmbeddable("135790"), "PENDING",
            LocalDateTime.of(2026, 4, 5, 6, 7, 8),
            null,
            LocalDateTime.of(2026, 5, 6, 7, 8, 9));
        ReflectionTestUtils.setField(entity, "id", 10L);

        SmsVerification smsVerification = SmsVerificationMapper.toDomain(entity);

        SmsVerification expected = SmsVerification.reconstitute(
            10L, new PhoneNumber("01098765432"), VerificationCode.of("135790"), SmsVerificationStatus.PENDING,
            LocalDateTime.of(2026, 4, 5, 6, 7, 8),
            null,
            LocalDateTime.of(2026, 5, 6, 7, 8, 9));
        assertThat(smsVerification).usingRecursiveComparison().isEqualTo(expected);
    }
}
