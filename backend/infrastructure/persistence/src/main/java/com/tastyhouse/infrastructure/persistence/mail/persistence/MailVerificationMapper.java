package com.tastyhouse.infrastructure.persistence.mail.persistence;

import com.tastyhouse.domain.mail.model.MailVerification;
import com.tastyhouse.domain.mail.model.MailVerificationStatus;
import com.tastyhouse.domain.shared.vo.VerificationCode;
import com.tastyhouse.infrastructure.persistence.shared.persistence.VerificationCodeEmbeddable;

final class MailVerificationMapper {

    private MailVerificationMapper() {
    }

    static MailVerification toDomain(MailVerificationJpaEntity entity) {
        return MailVerification.reconstitute(
            entity.getId(),
            entity.getEmail(),
            entity.getVerificationCode() == null || entity.getVerificationCode().value() == null
                ? null
                : VerificationCode.of(entity.getVerificationCode().value()),
            entity.getStatus() == null ? null : MailVerificationStatus.valueOf(entity.getStatus()),
            entity.getExpiresAt(),
            entity.getVerifiedAt(),
            entity.getCreatedAt()
        );
    }

    static MailVerificationJpaEntity toEntity(MailVerification mailVerification) {
        return MailVerificationJpaEntity.create(
            mailVerification.getEmail(),
            mailVerification.getVerificationCode() == null || mailVerification.getVerificationCode().value() == null
                ? null
                : new VerificationCodeEmbeddable(mailVerification.getVerificationCode().value()),
            mailVerification.getStatus() == null ? null : mailVerification.getStatus().name(),
            mailVerification.getExpiresAt(),
            mailVerification.getVerifiedAt(),
            mailVerification.getCreatedAt()
        );
    }

    static void applyChanges(MailVerificationJpaEntity entity, MailVerification mailVerification) {
        entity.applyChanges(
            mailVerification.getStatus() == null ? null : mailVerification.getStatus().name(),
            mailVerification.getVerifiedAt()
        );
    }
}
