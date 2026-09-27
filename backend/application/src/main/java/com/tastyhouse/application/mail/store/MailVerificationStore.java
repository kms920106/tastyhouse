package com.tastyhouse.application.mail.store;

import java.util.Optional;

import com.tastyhouse.application.mail.port.out.write.MailVerificationStatePort;
import com.tastyhouse.domain.mail.model.MailVerification;
import com.tastyhouse.domain.mail.model.MailVerificationStatus;

public class MailVerificationStore implements MailVerificationRepository {
    private final MailVerificationStatePort mailVerificationStatePort;

    public MailVerificationStore(MailVerificationStatePort mailVerificationStatePort) {
        this.mailVerificationStatePort = mailVerificationStatePort;
    }

    @Override
    public MailVerification save(MailVerification mailVerification) {
        return MailVerificationStateMapper.toDomain(
            mailVerificationStatePort.save(MailVerificationStateMapper.toState(mailVerification)));
    }

    @Override
    public Optional<MailVerification> findLatestPendingByEmail(String email, MailVerificationStatus status) {
        return mailVerificationStatePort.findLatestPendingByEmail(email, status == null ? null : status.name())
            .map(MailVerificationStateMapper::toDomain);
    }

    @Override
    public void expireAllPendingByEmail(String email) {
        mailVerificationStatePort.changeStatusByEmail(
            email,
            MailVerificationStatus.PENDING.name(),
            MailVerificationStatus.EXPIRED.name()
        );
    }
}
