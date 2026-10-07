package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.in.MemberProfileQueryUseCase;
import com.tastyhouse.application.member.port.out.MemberQueryPort;
import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class MemberProfileQueryService implements MemberProfileQueryUseCase {

    private final MemberQueryPort memberQueryPort;

    public MemberProfileQueryService(MemberQueryPort memberQueryPort) {
        this.memberQueryPort = memberQueryPort;
    }

    @Override
    public MemberWithProfileImageResult getMemberProfile(Long targetMemberId) {
        return findProfile(targetMemberId);
    }

    private MemberWithProfileImageResult findProfile(Long memberId) {
        return memberQueryPort.findMemberWithProfileImageById(MemberId.of(memberId).value())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.MEMBER_NOT_FOUND));
    }
}
