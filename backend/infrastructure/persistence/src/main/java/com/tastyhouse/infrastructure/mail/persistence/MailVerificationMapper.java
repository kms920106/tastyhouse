package com.tastyhouse.infrastructure.mail.persistence;

import com.tastyhouse.application.mail.port.out.write.MailVerificationState;
import com.tastyhouse.infrastructure.shared.persistence.VerificationCodeEmbeddable;

final class MailVerificationMapper {
    private MailVerificationMapper() {
    }

    static MailVerificationState toState(MailVerificationJpaEntity entity) {
        return new MailVerificationState(
            entity.getId(),
            entity.getEmail(),
            entity.getVerificationCode() == null ? null : entity.getVerificationCode().value(),
            entity.getStatus(),
            entity.getExpiresAt(),
            entity.getVerifiedAt(),
            entity.getCreatedAt()
        );
    }

    static MailVerificationJpaEntity toEntity(MailVerificationState state) {
        return MailVerificationJpaEntity.create(
            state.email(),
            state.verificationCode() == null ? null : new VerificationCodeEmbeddable(state.verificationCode()),
            state.status(),
            state.expiresAt(),
            state.verifiedAt(),
            state.createdAt()
        );
    }

    static void applyChanges(MailVerificationJpaEntity entity, MailVerificationState state) {
        entity.applyChanges(state.status(), state.verifiedAt());
    }
}
