package com.tastyhouse.application.mail.store;

import com.tastyhouse.application.mail.port.out.write.MailVerificationState;
import com.tastyhouse.domain.mail.model.MailVerification;
import com.tastyhouse.domain.mail.model.MailVerificationStatus;
import com.tastyhouse.domain.shared.vo.VerificationCode;

final class MailVerificationStateMapper {
    private MailVerificationStateMapper() {
    }

    static MailVerification toDomain(MailVerificationState state) {
        return MailVerification.reconstitute(
            state.id(),
            state.email(),
            state.verificationCode() == null ? null : VerificationCode.of(state.verificationCode()),
            state.status() == null ? null : MailVerificationStatus.valueOf(state.status()),
            state.expiresAt(),
            state.verifiedAt(),
            state.createdAt()
        );
    }

    static MailVerificationState toState(MailVerification mailVerification) {
        return new MailVerificationState(
            mailVerification.getId(),
            mailVerification.getEmail(),
            mailVerification.getVerificationCode() == null ? null : mailVerification.getVerificationCode().value(),
            mailVerification.getStatus() == null ? null : mailVerification.getStatus().name(),
            mailVerification.getExpiresAt(),
            mailVerification.getVerifiedAt(),
            mailVerification.getCreatedAt()
        );
    }
}
