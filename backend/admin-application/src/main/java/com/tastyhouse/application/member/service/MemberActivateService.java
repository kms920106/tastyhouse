package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.in.MemberActivateCommand;
import com.tastyhouse.application.member.port.in.MemberActivateUseCase;
import com.tastyhouse.application.member.port.out.write.MemberPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class MemberActivateService implements MemberActivateUseCase {

    private final MemberPersistencePort memberPersistencePort;

    public MemberActivateService(MemberPersistencePort memberPersistencePort) {
        this.memberPersistencePort = memberPersistencePort;
    }

    @Override
    public void activate(MemberActivateCommand command) {
        MemberId memberId = MemberId.of(command.memberId());
        Member member = loadMember(memberId);
        member.activate();
        memberPersistencePort.save(member);
    }

    private Member loadMember(MemberId memberId) {
        return memberPersistencePort.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.MEMBER_NOT_FOUND));
    }
}
