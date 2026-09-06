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
