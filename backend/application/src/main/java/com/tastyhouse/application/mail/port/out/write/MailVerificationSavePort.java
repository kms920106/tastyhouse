package com.tastyhouse.application.mail.port.out.write;

import com.tastyhouse.domain.mail.model.MailVerification;

public interface MailVerificationSavePort {

    MailVerification save(MailVerification mailVerification);

    void expireAllPendingByEmail(String email);
}
