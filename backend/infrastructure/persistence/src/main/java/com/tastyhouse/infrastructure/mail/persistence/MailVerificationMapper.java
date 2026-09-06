package com.tastyhouse.infrastructure.mail.persistence;

import com.tastyhouse.domain.mail.model.MailVerification;

final class MailVerificationMapper {
    private MailVerificationMapper() {
    }

    static MailVerification toDomain(MailVerificationJpaEntity entity) {
        return MailVerification.reconstitute(
            entity.getId(),
            entity.getEmail(),
            entity.getVerificationCode(),
            entity.getStatus(),
            entity.getExpiresAt(),
            entity.getVerifiedAt(),
            entity.getCreatedAt()
        );
    }

    static MailVerificationJpaEntity toEntity(MailVerification domain) {
        return MailVerificationJpaEntity.create(
            domain.getEmail(),
            domain.getVerificationCode(),
            domain.getStatus(),
            domain.getExpiresAt(),
            domain.getVerifiedAt(),
            domain.getCreatedAt()
        );
    }

    static void applyChanges(MailVerificationJpaEntity entity, MailVerification domain) {
        entity.applyChanges(domain.getStatus(), domain.getVerifiedAt());
    }
}
