package com.tastyhouse.application.emailverification.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.emailverification.model.MailVerification;
import com.tastyhouse.domain.emailverification.model.MailVerificationStatus;

public interface MailVerificationLoadPort {

    Optional<MailVerification> findLatestPendingByEmail(String email, MailVerificationStatus status);
}
