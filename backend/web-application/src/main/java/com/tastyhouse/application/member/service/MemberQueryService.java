package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.in.MemberQueryUseCase;
import com.tastyhouse.application.member.port.out.MemberPersonalInfoResult;
import com.tastyhouse.application.member.port.out.MemberQueryPort;
import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class MemberQueryService implements MemberQueryUseCase {

    private final MemberQueryPort memberQueryPort;

    public MemberQueryService(MemberQueryPort memberQueryPort) {
        this.memberQueryPort = memberQueryPort;
    }

    @Override
    public boolean checkNicknameAvailability(String nickname) {
        return !memberQueryPort.existsByNickname(nickname);
    }

    @Override
    public boolean checkPhoneAvailability(String phoneNumber) {
        return !memberQueryPort.existsByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED.name());
    }

    @Override
    public MemberWithProfileImageResult getMemberProfile(Long targetMemberId) {
        return findProfile(targetMemberId);
    }

    @Override
    public MemberWithProfileImageResult getMyProfile(Long memberId) {
        return findProfile(memberId);
    }

    @Override
    public MemberPersonalInfoResult getPersonalInfo(Long memberId) {
        return memberQueryPort.findPersonalInfoById(MemberId.of(memberId).value())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.MEMBER_NOT_FOUND));
    }

    private MemberWithProfileImageResult findProfile(Long memberId) {
        return memberQueryPort.findMemberWithProfileImageById(MemberId.of(memberId).value())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.MEMBER_NOT_FOUND));
    }
}
