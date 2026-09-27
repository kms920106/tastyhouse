package com.tastyhouse.application.sms.store;

import com.tastyhouse.application.sms.port.out.write.SmsVerificationState;
import com.tastyhouse.domain.shared.vo.PhoneNumber;
import com.tastyhouse.domain.shared.vo.VerificationCode;
import com.tastyhouse.domain.sms.model.SmsVerification;
import com.tastyhouse.domain.sms.model.SmsVerificationStatus;

final class SmsVerificationStateMapper {
    private SmsVerificationStateMapper() {
    }

    static SmsVerification toDomain(SmsVerificationState state) {
        return SmsVerification.reconstitute(
            state.id(),
            state.phoneNumber() == null ? null : new PhoneNumber(state.phoneNumber()),
            state.verificationCode() == null ? null : VerificationCode.of(state.verificationCode()),
            state.status() == null ? null : SmsVerificationStatus.valueOf(state.status()),
            state.expiresAt(),
            state.verifiedAt(),
            state.createdAt()
        );
    }

    static SmsVerificationState toState(SmsVerification smsVerification) {
        return new SmsVerificationState(
            smsVerification.getId(),
            smsVerification.getPhoneNumber() == null ? null : smsVerification.getPhoneNumber().value(),
            smsVerification.getVerificationCode() == null ? null : smsVerification.getVerificationCode().value(),
            smsVerification.getStatus() == null ? null : smsVerification.getStatus().name(),
            smsVerification.getExpiresAt(),
            smsVerification.getVerifiedAt(),
            smsVerification.getCreatedAt()
        );
    }
}
