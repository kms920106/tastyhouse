package com.tastyhouse.infrastructure.mail.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.domain.mail.event.MailVerifiedEvent;

@Component
public class MailVerificationEventListener {
    private static final Logger log = LoggerFactory.getLogger(MailVerificationEventListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(MailVerifiedEvent event) {
        log.info("메일 인증 완료 — verificationId={}, email={}, verifiedAt={}",
            event.verificationId().value(), event.email(), event.verifiedAt());
    }
}
