package com.tastyhouse.domain.member.referral.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.member.referral.event.ReferralRegisteredEvent;
import com.tastyhouse.domain.member.referral.model.MemberReferral;
import com.tastyhouse.domain.member.referral.repository.MemberReferralRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * 추천인 등록(도메인 서비스).
 *
 * <p>등록은 한 트랜잭션에서 {@code MemberReferral} 애그리거트를 만들고, 등록 사실을
 * {@link ReferralRegisteredEvent}로 발행하는 데까지만 책임진다. <b>보상 적립과 보상 완료 전이는 이
 * 서비스가 하지 않는다</b> — 커밋 이후 infrastructure의 {@code ReferralRegisteredEventListener}가
 * point 컨텍스트의 자기 서비스({@code PointLedgerService})를 경유해 수행한다.
 *
 * <p><b>왜 이관했는가</b>: 과거 이 서비스는 {@code point.model.{Point,PointHistory,PointType}}과
 * {@code point.repository.{PointRepository,PointHistoryRepository}}를 직접 주입해 타 컨텍스트의
 * 애그리거트를 직접 생성·저장했다. point의 적립 불변식(잔액 증가 + EARNED 이력 + 적립 이벤트가 항상
 * 함께 일어난다)이 point 컨텍스트 밖에서 재구현되는 구조여서, 원장 규칙이 바뀌면 두 곳을 함께 고쳐야
 * 했다. 지금은 적립 시맨틱의 단일 원천이 {@code PointLedgerService#earnPoints} 하나다.
 *
 * <p><b>트레이드오프(의도된 선택)</b>: 적립이 AFTER_COMMIT 리스너로 밀려나므로, 등록 트랜잭션이
 * 커밋된 뒤 리스너가 실패하면 <b>추천 관계는 남고 보상 포인트만 유실</b>된다. 그 경우 추천 관계는
 * {@code PENDING}에 머무르므로(보상 완료 전이도 리스너가 함께 수행한다) "적립되지 않았는데 완료로
 * 보이는" 상태는 생기지 않고, 미적립 건을 상태로 식별해 재처리할 수 있다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code MemberDomainConfig}가 담당한다.
 */
public class ReferralRegistrationService {

    private final MemberReferralRepository memberReferralRepository;
    private final DomainEventPublisher domainEventPublisher;

    public ReferralRegistrationService(
        MemberReferralRepository memberReferralRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        this.memberReferralRepository = memberReferralRepository;
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
        // 뒤이은 저장이 id null 상태로 다시 insert되어 중복 등록이 된다(AGENTS.md 선례).
        // 이 식별자는 아래 이벤트에 실려 리스너의 보상 완료 전이 대상이 되므로 특히 중요하다.
        MemberReferral referral = memberReferralRepository.save(
            MemberReferral.register(referrerId, refereeId)
        );

        domainEventPublisher.publish(new ReferralRegisteredEvent(
            referral.getReferralId(),
            referral.getReferrerId(),
            referral.getRefereeId(),
            LocalDateTime.now()
        ));
    }
}
