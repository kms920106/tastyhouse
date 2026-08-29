package com.tastyhouse.adminapi.member.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.adminapi.member.application.port.in.MemberActivateCommand;
import com.tastyhouse.adminapi.member.application.port.in.MemberCommandUseCase;
import com.tastyhouse.adminapi.member.application.port.in.MemberSuspendCommand;
import com.tastyhouse.adminapi.member.application.port.in.MemberWithdrawCommand;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberWithdrawalReason;
import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.member.service.MemberWithdrawalService;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 회원 관리 명령 서비스.
 *
 * <p>정지/활성은 {@code Member} 애그리거트 하나만 다루는 단일 애그리거트 연산(분류 A)이므로 이 서비스가
 * 직접 write 포트로 처리한다. 강제 탈퇴는 회원 상태 전이와 탈퇴 사유 기록을 함께 해야 하는 크로스
 * 애그리거트 불변식이므로 도메인 서비스({@link MemberWithdrawalService})에 위임한다.
 *
 * <p>도메인이 프레임워크-프리라 더티 체킹이 없으므로 상태 전이 후 저장은 명시적 save로 수행한다.
 */
@Service
@Transactional
public class MemberCommandService implements MemberCommandUseCase {

    private final MemberRepository memberRepository;
    private final MemberWithdrawalService memberWithdrawalService;

    public MemberCommandService(MemberRepository memberRepository, MemberWithdrawalService memberWithdrawalService) {
        this.memberRepository = memberRepository;
        this.memberWithdrawalService = memberWithdrawalService;
    }

    @Override
    public void suspend(MemberSuspendCommand command) {
        MemberId memberId = MemberId.of(command.memberId());
        Member member = loadMember(memberId);
        member.suspend();
        memberRepository.save(member);
    }

    @Override
    public void activate(MemberActivateCommand command) {
        MemberId memberId = MemberId.of(command.memberId());
        Member member = loadMember(memberId);
        member.activate();
        memberRepository.save(member);
    }

    @Override
    public void withdraw(MemberWithdrawCommand command) {
        MemberId memberId = MemberId.of(command.memberId());
        memberWithdrawalService.withdraw(memberId, MemberWithdrawalReason.from(command.reason()), command.reasonDetail());
    }

    private Member loadMember(MemberId memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
