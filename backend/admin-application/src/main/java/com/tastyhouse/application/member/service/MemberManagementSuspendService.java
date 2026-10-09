package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.in.MemberManagementSuspendUseCase;
import com.tastyhouse.application.member.port.in.MemberSuspendCommand;
import com.tastyhouse.application.member.port.out.write.MemberLoadPort;
import com.tastyhouse.application.member.port.out.write.MemberSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class MemberManagementSuspendService implements MemberManagementSuspendUseCase {

    private final MemberLoadPort memberLoadPort;
    private final MemberSavePort memberSavePort;

    public MemberManagementSuspendService(MemberLoadPort memberLoadPort, MemberSavePort memberSavePort) {
        this.memberLoadPort = memberLoadPort;
        this.memberSavePort = memberSavePort;
    }

    @Override
    public void suspend(MemberSuspendCommand command) {
        MemberId memberId = MemberId.of(command.memberId());
        Member member = loadMember(memberId);
        member.suspend();
        memberSavePort.save(member);
    }

    private Member loadMember(MemberId memberId) {
        return memberLoadPort.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.MEMBER_NOT_FOUND));
    }
}
