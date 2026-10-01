package com.tastyhouse.application.mail.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.mail.model.MailVerification;
import com.tastyhouse.domain.mail.model.MailVerificationStatus;

public interface MailVerificationPersistencePort {

    MailVerification save(MailVerification mailVerification);

    Optional<MailVerification> findLatestPendingByEmail(String email, MailVerificationStatus status);

    void expireAllPendingByEmail(String email);
}
