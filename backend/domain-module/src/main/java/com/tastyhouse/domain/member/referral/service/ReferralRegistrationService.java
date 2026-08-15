package com.tastyhouse.domain.member.referral.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.member.referral.event.ReferralRegisteredEvent;
import com.tastyhouse.domain.member.referral.model.MemberReferral;
import com.tastyhouse.domain.member.referral.repository.MemberReferralRepository;
import com.tastyhouse.domain.point.event.PointEarnedEvent;
import com.tastyhouse.domain.point.model.Point;
import com.tastyhouse.domain.point.model.PointHistory;
import com.tastyhouse.domain.point.model.PointType;
import com.tastyhouse.domain.point.repository.PointHistoryRepository;
import com.tastyhouse.domain.point.repository.PointRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * 추천인 등록(도메인 서비스).
 *
 * <p>등록은 한 트랜잭션에서 {@code MemberReferral} 애그리거트를 만들고, 추천인·피추천인 양쪽의
 * {@code Point}/{@code PointHistory} 애그리거트에 보상 적립을 반영한 뒤 추천 관계를 보상 완료 상태로
 * 전이시킨다. 애그리거트 타입 2개 이상을 함께 load &amp; save 하는 불변식 오케스트레이션(분류 C)이므로
 * 도메인 계층에 두어, 가입 경로(일반/소셜)가 여러 개여도 "추천 등록과 양쪽 보상 적립은 항상 함께
 * 일어난다"는 규칙이 갈리지 않게 한다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code MemberDomainConfig}가 담당한다.
 *
 * <p>보상 적립은 point 도메인의 write 포트({@code PointRepository}/{@code PointHistoryRepository})를
 * 직접 사용한다 — point 도메인은 아직 전환 전(별도 작업 대상)이라 그 application 계층을 이 시점에
 * 개편하지 않고, 적립 시맨틱(잔액 증가 + EARNED 이력 + 적립 이벤트)만 그대로 옮겨 담았다.
 */
public class ReferralRegistrationService {

    private static final int REFERRER_REWARD_POINT = 1000;
    private static final int REFEREE_REWARD_POINT = 1000;

    private static final String REFERRER_REWARD_REASON = "추천인 보상";
    private static final String REFEREE_REWARD_REASON = "추천받기 보상";

    private final MemberReferralRepository memberReferralRepository;
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final DomainEventPublisher domainEventPublisher;

    public ReferralRegistrationService(
        MemberReferralRepository memberReferralRepository,
        PointRepository pointRepository,
        PointHistoryRepository pointHistoryRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        this.memberReferralRepository = memberReferralRepository;
        this.pointRepository = pointRepository;
        this.pointHistoryRepository = pointHistoryRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    public void register(MemberId referrerId, MemberId refereeId) {
        if (referrerId.equals(refereeId)) {
            throw new BusinessException(ErrorCode.REFERRAL_SELF_NOT_ALLOWED);
        }

        if (memberReferralRepository.existsByRefereeId(refereeId)) {
            throw new BusinessException(ErrorCode.REFERRAL_ALREADY_EXISTS);
        }

        // save가 식별자 채워진 새 도메인 인스턴스를 반환하므로 반드시 재할당한다 — 재할당하지 않으면
        // 뒤이은 reward() 저장이 id null 상태로 다시 insert되어 중복 등록이 된다(AGENTS.md 선례).
        MemberReferral referral = memberReferralRepository.save(
            MemberReferral.register(referrerId, refereeId)
        );

        earnRewardPoints(referrerId, REFERRER_REWARD_POINT, REFERRER_REWARD_REASON);
        earnRewardPoints(refereeId, REFEREE_REWARD_POINT, REFEREE_REWARD_REASON);

        referral.reward();
        memberReferralRepository.save(referral);

        domainEventPublisher.publish(new ReferralRegisteredEvent(
            referral.getReferralId(),
            referral.getReferrerId(),
            referral.getRefereeId(),
            LocalDateTime.now()
        ));
    }

    private void earnRewardPoints(MemberId memberId, int pointAmount, String reason) {
        Point point = pointRepository.findByMemberId(memberId)
            .orElseGet(() -> pointRepository.save(Point.of(memberId)));

        point.addPoints(pointAmount);
        pointRepository.save(point);

        pointHistoryRepository.save(PointHistory.of(memberId, PointType.EARNED, pointAmount, reason));

        domainEventPublisher.publish(new PointEarnedEvent(memberId, pointAmount, reason, LocalDateTime.now()));
    }
}
