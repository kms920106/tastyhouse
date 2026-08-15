package com.tastyhouse.infrastructure.member.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.domain.member.referral.event.ReferralRegisteredEvent;
import com.tastyhouse.domain.member.referral.service.ReferralRewardCompletionService;
import com.tastyhouse.domain.point.service.PointLedgerService;

/**
 * 추천 등록 이벤트를 받아 양쪽 보상 포인트를 적립하는 크로스커팅 리스너(분류 E).
 *
 * <p>infrastructure-module에 두는 이유: 추천 등록은 일반 가입과 소셜 가입(4종) 어느 경로에서도
 * 발생하므로, 특정 api 모듈이 아니라 모든 실행 모듈이 공통으로 의존하는 이 모듈이 소유해야 발행
 * 경로와 무관하게 동일하게 반응한다.
 *
 * <p><b>이 리스너가 적립을 소유하는 이유</b>: 과거에는 {@code ReferralRegistrationService}가
 * point의 애그리거트({@code Point}/{@code PointHistory})와 리포지토리를 직접 주입해 적립을
 * 재구현하고 있었다. 적립 시맨틱(잔액 증가 + EARNED 이력 + 적립 이벤트)의 단일 원천은 point
 * 컨텍스트의 {@link PointLedgerService}여야 하므로, 컨텍스트를 잇는 책임을 이 리스너로 옮겼다.
 * {@code PaymentEventListener}가 결제↔포인트를 잇는 것과 같은 형태다.
 *
 * <p><b>순서가 중요하다 — 적립 먼저, 보상 완료 전이는 그 다음이다.</b> 전이가 먼저 커밋되면
 * "완료로 표시됐지만 포인트는 없는" 추천 관계가 남아, 적립 실패 건을 상태로 식별할 수 없게 된다.
 * 지금 순서라면 적립이 실패했을 때 추천 관계가 {@code PENDING}에 머물러 재처리 대상으로 남는다.
 *
 * <p>{@code AFTER_COMMIT}이므로 등록 트랜잭션이 커밋된 뒤 새 트랜잭션
 * ({@code REQUIRES_NEW})에서 실행된다 — 이 핸들러가 실패해도 추천 등록 자체는 롤백되지 않는다.
 * 이는 추천 등록과 보상 적립의 결합을 끊기 위해 의도적으로 감수한 트레이드오프다.
 */
@Component
public class ReferralRegisteredEventListener {

    private static final Logger log = LoggerFactory.getLogger(ReferralRegisteredEventListener.class);

    private static final int REFERRER_REWARD_POINT = 1000;
    private static final int REFEREE_REWARD_POINT = 1000;

    private static final String REFERRER_REWARD_REASON = "추천인 보상";
    private static final String REFEREE_REWARD_REASON = "추천받기 보상";

    private final PointLedgerService pointLedgerService;
    private final ReferralRewardCompletionService referralRewardCompletionService;

    public ReferralRegisteredEventListener(
        PointLedgerService pointLedgerService,
        ReferralRewardCompletionService referralRewardCompletionService
    ) {
        this.pointLedgerService = pointLedgerService;
        this.referralRewardCompletionService = referralRewardCompletionService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ReferralRegisteredEvent event) {
        pointLedgerService.earnPoints(event.referrerId(), REFERRER_REWARD_POINT, REFERRER_REWARD_REASON);
        pointLedgerService.earnPoints(event.refereeId(), REFEREE_REWARD_POINT, REFEREE_REWARD_REASON);

        referralRewardCompletionService.complete(event.referralId());

        log.info("추천 등록 보상 적립 완료 — referralId={}, referrerId={}, refereeId={}, registeredAt={}",
            event.referralId().value(),
            event.referrerId().value(),
            event.refereeId().value(),
            event.registeredAt()
        );
    }
}
