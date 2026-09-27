package com.tastyhouse.application.mail.port.out.write;

import java.util.Optional;

public interface MailVerificationStatePort {
    MailVerificationState save(MailVerificationState state);

    Optional<MailVerificationState> findLatestPendingByEmail(String email, String status);

    void expireAllPendingByEmail(String email);
}
