package com.tastyhouse.application.mail.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.mail.model.MailVerification;
import com.tastyhouse.domain.mail.model.MailVerificationStatus;

public interface MailVerificationLoadPort {

    Optional<MailVerification> findLatestPendingByEmail(String email, MailVerificationStatus status);
}
