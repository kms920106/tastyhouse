package com.tastyhouse.infrastructure.policy.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.domain.policy.event.PolicyActivatedEvent;

/**
 * 약관·정책 현행 전이 이벤트 리스너(크로스커팅).
 *
 * <p>정책 활성화는 지금은 admin-api에서 트리거되지만, 활성화 자체는 특정 액터에 묶이지 않는 도메인
 * 불변식({@code PolicyActivationService})이라 다른 모듈에서 발행돼도 동일하게 반응해야 한다. 따라서
 * 특정 api 모듈이 아니라 모든 실행 모듈이 스캔하는 infrastructure-module에 둔다(분류 E).
 *
 * <p>재동의 요청·개정 고지 발송은 아직 이 리스너가 담당하지 않는다 — 발송 대상이 전체 회원이라
 * 요청 스레드에서 처리할 수 없고 배치·큐 설계가 선행돼야 하기 때문이다. 그때까지는 전이 사실만
 * 남겨 두어, 어떤 정책이 언제 현행이 됐는지가 발행 지점 밖에서도 관측 가능하게 한다.
 */
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
