package com.tastyhouse.infrastructure.sms.persistence;

import com.tastyhouse.application.sms.port.out.write.SmsVerificationState;
import com.tastyhouse.infrastructure.shared.persistence.PhoneNumberEmbeddable;
import com.tastyhouse.infrastructure.shared.persistence.VerificationCodeEmbeddable;

final class SmsVerificationMapper {
    private SmsVerificationMapper() {
    }

    static SmsVerificationState toState(SmsVerificationJpaEntity entity) {
        return new SmsVerificationState(
            entity.getId(),
            entity.getPhoneNumber() == null ? null : entity.getPhoneNumber().value(),
            entity.getVerificationCode() == null ? null : entity.getVerificationCode().value(),
            entity.getStatus(),
            entity.getExpiresAt(),
            entity.getVerifiedAt(),
            entity.getCreatedAt()
        );
    }

    static SmsVerificationJpaEntity toEntity(SmsVerificationState state) {
        return SmsVerificationJpaEntity.create(
            state.phoneNumber() == null ? null : new PhoneNumberEmbeddable(state.phoneNumber()),
            state.verificationCode() == null ? null : new VerificationCodeEmbeddable(state.verificationCode()),
            state.status(),
            state.expiresAt(),
            state.verifiedAt(),
            state.createdAt()
        );
    }

    static void applyChanges(SmsVerificationJpaEntity entity, SmsVerificationState state) {
        entity.applyChanges(state.status(), state.verifiedAt());
    }
}
