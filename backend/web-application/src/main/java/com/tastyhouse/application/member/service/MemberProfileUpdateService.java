package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.in.MemberProfileUpdateCommand;
import com.tastyhouse.application.member.port.in.MemberProfileUpdateUseCase;
import com.tastyhouse.application.member.port.out.write.MemberPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class MemberProfileUpdateService implements MemberProfileUpdateUseCase {

    private final MemberPersistencePort memberPersistencePort;

    public MemberProfileUpdateService(MemberPersistencePort memberPersistencePort) {
        this.memberPersistencePort = memberPersistencePort;
    }

    @Override
    public void updateProfile(MemberProfileUpdateCommand command) {
        Long profileImageFileId = command.profileImageFileId();
        Member member = loadMember(command.memberId());
        UploadedFileId uploadedFileId = profileImageFileId == null ? null : UploadedFileId.of(profileImageFileId);
        member.updateProfile(command.nickname(), command.statusMessage(), uploadedFileId);
        memberPersistencePort.save(member);
    }

    private Member loadMember(Long memberId) {
        return memberPersistencePort.findById(MemberId.of(memberId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.MEMBER_NOT_FOUND));
    }
}
