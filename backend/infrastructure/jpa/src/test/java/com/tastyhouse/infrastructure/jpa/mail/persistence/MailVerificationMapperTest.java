package com.tastyhouse.infrastructure.jpa.mail.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.mail.model.MailVerification;
import com.tastyhouse.domain.mail.model.MailVerificationStatus;
import com.tastyhouse.domain.shared.vo.VerificationCode;

import static org.assertj.core.api.Assertions.assertThat;

class MailVerificationMapperTest {

    @Test
    @DisplayName("MailVerification → 엔티티 변환 시 인증코드는 Embeddable로, 상태는 상수명으로 옮겨진다")
    void toEntityCopiesColumns() {
        MailVerificationJpaEntity entity = MailVerificationMapper.toEntity(verified());

        assertThat(entity.getEmail()).isEqualTo("user@example.com");
        assertThat(entity.getVerificationCode().value()).isEqualTo("123456");
        assertThat(entity.getStatus()).isEqualTo("VERIFIED");
        assertThat(entity.getExpiresAt()).isEqualTo(LocalDateTime.of(2026, 1, 2, 3, 4, 5));
        assertThat(entity.getVerifiedAt()).isEqualTo(LocalDateTime.of(2026, 2, 3, 4, 5, 6));
        assertThat(entity.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 3, 4, 5, 6, 7));
    }

    @Test
    @DisplayName("엔티티 → MailVerification 변환 시 id를 포함한 모든 필드가 복원된다")
    void toDomainRestoresAllFields() {
        MailVerification original = verified();

        MailVerification restored = MailVerificationMapper.toDomain(persisted(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("verifiedAt이 null이어도 두 방향 모두 변환된다")
    void nullVerifiedAt() {
        MailVerification original = MailVerification.reconstitute(
            6L, "other@example.com", VerificationCode.of("654321"), MailVerificationStatus.PENDING,
            LocalDateTime.of(2026, 4, 5, 6, 7, 8),
            null,
            LocalDateTime.of(2026, 5, 6, 7, 8, 9));

        MailVerificationJpaEntity entity = MailVerificationMapper.toEntity(original);

        assertThat(entity.getVerifiedAt()).isNull();
        assertThat(entity.getStatus()).isEqualTo("PENDING");
        assertThat(MailVerificationMapper.toDomain(persisted(original))).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("applyChanges는 상태와 인증 시각을 옮긴다")
    void applyChangesCopiesWritableFields() {
        MailVerificationJpaEntity entity = MailVerificationMapper.toEntity(MailVerification.reconstitute(
            5L, "user@example.com", VerificationCode.of("123456"), MailVerificationStatus.PENDING,
            LocalDateTime.of(2026, 1, 2, 3, 4, 5), null, LocalDateTime.of(2026, 3, 4, 5, 6, 7)));

        MailVerificationMapper.applyChanges(entity, verified());

        assertThat(entity.getStatus()).isEqualTo("VERIFIED");
        assertThat(entity.getVerifiedAt()).isEqualTo(LocalDateTime.of(2026, 2, 3, 4, 5, 6));
    }

    private static MailVerification verified() {
        return MailVerification.reconstitute(
            5L, "user@example.com", VerificationCode.of("123456"), MailVerificationStatus.VERIFIED,
            LocalDateTime.of(2026, 1, 2, 3, 4, 5),
            LocalDateTime.of(2026, 2, 3, 4, 5, 6),
            LocalDateTime.of(2026, 3, 4, 5, 6, 7));
    }

    private static MailVerificationJpaEntity persisted(MailVerification mailVerification) {
        MailVerificationJpaEntity entity = MailVerificationMapper.toEntity(mailVerification);
        ReflectionTestUtils.setField(entity, "id", mailVerification.getId());
        return entity;
    }
}
