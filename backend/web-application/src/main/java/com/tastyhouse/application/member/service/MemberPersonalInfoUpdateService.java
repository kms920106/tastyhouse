package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberGender;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.in.MemberPersonalInfoUpdateCommand;
import com.tastyhouse.application.member.port.in.MemberPersonalInfoUpdateUseCase;
import com.tastyhouse.application.member.port.out.write.MemberLoadPort;
import com.tastyhouse.application.member.port.out.write.MemberSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class MemberPersonalInfoUpdateService implements MemberPersonalInfoUpdateUseCase {

    private final MemberLoadPort memberLoadPort;
    private final MemberSavePort memberSavePort;

    public MemberPersonalInfoUpdateService(MemberLoadPort memberLoadPort, MemberSavePort memberSavePort) {
        this.memberLoadPort = memberLoadPort;
        this.memberSavePort = memberSavePort;
    }

    @Override
    public void updatePersonalInfo(MemberPersonalInfoUpdateCommand command) {
        String gender = command.gender();
        Member member = loadMember(command.memberId());
        member.updatePersonalInfo(
            command.fullName(), command.phoneNumber(), command.birthDate(),
            gender == null ? null : MemberGender.from(gender),
            Boolean.TRUE.equals(command.pushNotificationEnabled()),
            Boolean.TRUE.equals(command.marketingInfoEnabled()),
            Boolean.TRUE.equals(command.eventInfoEnabled())
        );
        memberSavePort.save(member);
    }

    private Member loadMember(Long memberId) {
        return memberLoadPort.findById(MemberId.of(memberId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.MEMBER_NOT_FOUND));
    }
}
