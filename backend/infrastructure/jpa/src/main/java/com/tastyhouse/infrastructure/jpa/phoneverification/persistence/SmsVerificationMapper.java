package com.tastyhouse.infrastructure.jpa.phoneverification.persistence;

import com.tastyhouse.domain.phoneverification.model.SmsVerification;
import com.tastyhouse.domain.phoneverification.model.SmsVerificationStatus;
import com.tastyhouse.domain.shared.vo.PhoneNumber;
import com.tastyhouse.domain.shared.vo.VerificationCode;
import com.tastyhouse.infrastructure.jpa.shared.persistence.PhoneNumberEmbeddable;
import com.tastyhouse.infrastructure.jpa.shared.persistence.VerificationCodeEmbeddable;

final class SmsVerificationMapper {

    private SmsVerificationMapper() {
    }

    static SmsVerification toDomain(SmsVerificationJpaEntity entity) {
        return SmsVerification.reconstitute(
            entity.getId(),
            entity.getPhoneNumber() == null || entity.getPhoneNumber().value() == null
                ? null : new PhoneNumber(entity.getPhoneNumber().value()),
            entity.getVerificationCode() == null || entity.getVerificationCode().value() == null
                ? null : VerificationCode.of(entity.getVerificationCode().value()),
            entity.getStatus() == null ? null : SmsVerificationStatus.valueOf(entity.getStatus()),
            entity.getExpiresAt(),
            entity.getVerifiedAt(),
            entity.getCreatedAt()
        );
    }

    static SmsVerificationJpaEntity toEntity(SmsVerification smsVerification) {
        return SmsVerificationJpaEntity.create(
            smsVerification.getPhoneNumber() == null || smsVerification.getPhoneNumber().value() == null
                ? null : new PhoneNumberEmbeddable(smsVerification.getPhoneNumber().value()),
            smsVerification.getVerificationCode() == null || smsVerification.getVerificationCode().value() == null
                ? null : new VerificationCodeEmbeddable(smsVerification.getVerificationCode().value()),
            smsVerification.getStatus() == null ? null : smsVerification.getStatus().name(),
            smsVerification.getExpiresAt(),
            smsVerification.getVerifiedAt(),
            smsVerification.getCreatedAt()
        );
    }

    static void applyChanges(SmsVerificationJpaEntity entity, SmsVerification smsVerification) {
        entity.applyChanges(
            smsVerification.getStatus() == null ? null : smsVerification.getStatus().name(),
            smsVerification.getVerifiedAt()
        );
    }
}
