package com.tastyhouse.infrastructure.sms.persistence;

import com.tastyhouse.domain.sms.model.SmsVerification;

final class SmsVerificationMapper {
    private SmsVerificationMapper() {
    }

    static SmsVerification toDomain(SmsVerificationJpaEntity entity) {
        return SmsVerification.reconstitute(
            entity.getId(),
            entity.getPhoneNumber(),
            entity.getVerificationCode(),
            entity.getStatus(),
            entity.getExpiresAt(),
            entity.getVerifiedAt(),
            entity.getCreatedAt()
        );
    }

    static SmsVerificationJpaEntity toEntity(SmsVerification domain) {
        return SmsVerificationJpaEntity.create(
            domain.getPhoneNumber(),
            domain.getVerificationCode(),
            domain.getStatus(),
            domain.getExpiresAt(),
            domain.getVerifiedAt(),
            domain.getCreatedAt()
        );
    }

    static void applyChanges(SmsVerificationJpaEntity entity, SmsVerification domain) {
        entity.applyChanges(domain.getStatus(), domain.getVerifiedAt());
    }
}
