package com.tastyhouse.application.phoneverification.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.domain.phoneverification.event.SmsVerifiedEvent;

@Component
class SmsVerificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(SmsVerificationEventListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(SmsVerifiedEvent event) {
        log.info("SMS 인증 완료 — verificationId={}, phoneNumber={}, verifiedAt={}",
            event.verificationId().value(), event.phoneNumber(), event.verifiedAt());
    }
}
