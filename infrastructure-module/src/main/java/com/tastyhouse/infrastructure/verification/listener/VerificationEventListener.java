package com.tastyhouse.infrastructure.verification.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.core.domain.verification.domain.event.EmailVerifiedEvent;
import com.tastyhouse.core.domain.verification.domain.event.PhoneVerifiedEvent;

@Slf4j
@Component
public class VerificationEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(EmailVerifiedEvent event) {
        log.info("이메일 인증 완료 — verificationId={}, email={}, verifiedAt={}",
            event.verificationId().value(), event.email(), event.verifiedAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(PhoneVerifiedEvent event) {
        log.info("휴대폰 인증 완료 — verificationId={}, phoneNumber={}, verifiedAt={}",
            event.verificationId().value(), event.phoneNumber(), event.verifiedAt());
    }
}
