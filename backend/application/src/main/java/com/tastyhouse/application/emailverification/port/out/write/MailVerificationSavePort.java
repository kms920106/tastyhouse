package com.tastyhouse.application.emailverification.port.out.write;

import com.tastyhouse.domain.emailverification.model.MailVerification;

public interface MailVerificationSavePort {

    MailVerification save(MailVerification mailVerification);

    void expireAllPendingByEmail(String email);
}
