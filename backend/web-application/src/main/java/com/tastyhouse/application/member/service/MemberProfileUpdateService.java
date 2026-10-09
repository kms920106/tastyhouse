package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.in.MemberProfileUpdateCommand;
import com.tastyhouse.application.member.port.in.MemberProfileUpdateUseCase;
import com.tastyhouse.application.member.port.out.write.MemberLoadPort;
import com.tastyhouse.application.member.port.out.write.MemberSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class MemberProfileUpdateService implements MemberProfileUpdateUseCase {

    private final MemberLoadPort memberLoadPort;
    private final MemberSavePort memberSavePort;

    public MemberProfileUpdateService(MemberLoadPort memberLoadPort, MemberSavePort memberSavePort) {
        this.memberLoadPort = memberLoadPort;
        this.memberSavePort = memberSavePort;
    }

    @Override
    public void updateProfile(MemberProfileUpdateCommand command) {
        Long profileImageFileId = command.profileImageFileId();
        Member member = loadMember(command.memberId());
        UploadedFileId uploadedFileId = profileImageFileId == null ? null : UploadedFileId.of(profileImageFileId);
        member.updateProfile(command.nickname(), command.statusMessage(), uploadedFileId);
        memberSavePort.save(member);
    }

    private Member loadMember(Long memberId) {
        return memberLoadPort.findById(MemberId.of(memberId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.MEMBER_NOT_FOUND));
    }
}
