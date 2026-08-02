package com.tastyhouse.adminapi.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.domain.model.Member;
import com.tastyhouse.domain.member.domain.model.MemberWithdrawalReason;
import com.tastyhouse.domain.member.domain.repository.MemberRepository;
import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.member.domain.service.MemberWithdrawalService;
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
public class MemberCommandService {

    private final MemberRepository memberRepository;
    private final MemberWithdrawalService memberWithdrawalService;

    public MemberCommandService(MemberRepository memberRepository, MemberWithdrawalService memberWithdrawalService) {
        this.memberRepository = memberRepository;
        this.memberWithdrawalService = memberWithdrawalService;
    }

    public void suspend(Long id) {
        MemberId memberId = MemberId.of(id);
        Member member = loadMember(memberId);
        member.suspend();
        memberRepository.save(member);
    }

    public void activate(Long id) {
        MemberId memberId = MemberId.of(id);
        Member member = loadMember(memberId);
        member.activate();
        memberRepository.save(member);
    }

    public void withdraw(Long id, String reason, String reasonDetail) {
        MemberId memberId = MemberId.of(id);
        memberWithdrawalService.withdraw(memberId, MemberWithdrawalReason.from(reason), reasonDetail);
    }

    private Member loadMember(MemberId memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
