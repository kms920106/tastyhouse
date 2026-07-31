package com.tastyhouse.infrastructure.member.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.domain.member.domain.event.MemberRegisteredEvent;
import com.tastyhouse.domain.member.domain.event.MemberWithdrawnEvent;

/**
 * 회원 도메인 이벤트 리스너(크로스커팅).
 *
 * <p>가입·탈퇴는 web-api(본인)와 admin-api(관리자 강제 탈퇴) 양쪽에서 트리거되므로, 리스너를 특정 api
 * 모듈에 두면 다른 모듈에서 발생한 이벤트를 놓친다. 두 모듈이 공통으로 의존하는
 * infrastructure-module에 두어 어느 경로로 발행돼도 동일하게 반응하게 한다(분류 E).
 */
@Slf4j
@Component
public class MemberEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(MemberRegisteredEvent event) {
        log.info("회원가입 완료 — memberId={}, username={}, registeredAt={}",
            event.memberId().value(), event.username(), event.registeredAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(MemberWithdrawnEvent event) {
        log.info("회원탈퇴 완료 — memberId={}, reason={}, withdrawnAt={}",
            event.memberId().value(), event.reason(), event.withdrawnAt());
    }
}
