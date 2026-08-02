package com.tastyhouse.infrastructure.mail.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.domain.mail.event.MailVerifiedEvent;

/**
 * 메일 인증 완료 이벤트 리스너.
 *
 * <p>도메인별로 분리해 둔다 — 한 리스너가 여러 도메인 이벤트를 구독하면 한 도메인의 변경이
 * 다른 도메인의 리스너 파일을 건드리게 되어 독립성이 깨진다(선례: {@code CouponEventListener},
 * {@code MemberEventListener}도 도메인별 분리).
 *
 * <p>발송은 이 리스너가 담당하지 않는다 — 이 이벤트는 인증 <b>완료</b> 시점이고 발송은 <b>발급</b>
 * 시점에 필요하므로 시점이 다르다. 발송은 도메인 서비스 {@code MailVerificationService#issue}가
 * 발급과 원자적으로 수행한다.
 */
@Component
public class MailVerificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(MailVerificationEventListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(MailVerifiedEvent event) {
        log.info("메일 인증 완료 — verificationId={}, email={}, verifiedAt={}",
            event.verificationId().value(), event.email(), event.verifiedAt());
    }
}
