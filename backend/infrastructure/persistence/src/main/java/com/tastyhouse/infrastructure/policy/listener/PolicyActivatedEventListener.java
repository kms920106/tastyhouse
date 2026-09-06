package com.tastyhouse.infrastructure.policy.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.domain.policy.event.PolicyActivatedEvent;

@Component
public class PolicyActivatedEventListener {
    private static final Logger log = LoggerFactory.getLogger(PolicyActivatedEventListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(PolicyActivatedEvent event) {
        log.info("정책 현행 전이 완료 — policyDocumentId={}, type={}, version={}, activatedAt={}",
            event.policyDocumentId().value(),
            event.type(),
            event.version(),
            event.activatedAt()
        );
    }
}
