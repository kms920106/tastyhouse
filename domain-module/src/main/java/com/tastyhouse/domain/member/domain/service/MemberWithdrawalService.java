package com.tastyhouse.domain.member.domain.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.domain.event.MemberWithdrawnEvent;
import com.tastyhouse.domain.member.domain.model.Member;
import com.tastyhouse.domain.member.domain.model.MemberWithdrawal;
import com.tastyhouse.domain.member.domain.model.MemberWithdrawalReason;
import com.tastyhouse.domain.member.domain.repository.MemberRepository;
import com.tastyhouse.domain.member.domain.repository.MemberWithdrawalRepository;
import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * 회원 탈퇴(도메인 서비스).
 *
 * <p>탈퇴는 한 트랜잭션에서 {@code Member} 애그리거트의 상태를 전이시키고, 그 사유를 별도
 * {@code MemberWithdrawal} 애그리거트로 남긴다. 애그리거트 타입 2개를 함께 load &amp; save 하는
 * 불변식 오케스트레이션(분류 C)이므로 소비 모듈의 command 서비스가 아니라 도메인 계층에 두어,
 * 본인 탈퇴(web-api)와 관리자 강제 탈퇴(admin-api) 어느 경로로 들어와도 "회원 상태 전이와 사유
 * 기록은 항상 함께 일어난다"는 규칙이 갈리지 않게 한다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 도메인이 프레임워크-프리라
 * 더티 체킹이 없으므로 상태 전이 후 저장은 명시적 save로 수행한다.
 */
public class MemberWithdrawalService {

    private final MemberRepository memberRepository;
    private final MemberWithdrawalRepository memberWithdrawalRepository;
    private final DomainEventPublisher domainEventPublisher;

    public MemberWithdrawalService(
        MemberRepository memberRepository,
        MemberWithdrawalRepository memberWithdrawalRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        this.memberRepository = memberRepository;
        this.memberWithdrawalRepository = memberWithdrawalRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    public void withdraw(MemberId memberId, MemberWithdrawalReason reason, String reasonDetail) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        member.withdraw();
        memberRepository.save(member);

        memberWithdrawalRepository.save(MemberWithdrawal.of(memberId, reason, reasonDetail));

        domainEventPublisher.publish(
            new MemberWithdrawnEvent(member.getMemberId(), reason, LocalDateTime.now())
        );
    }
}
