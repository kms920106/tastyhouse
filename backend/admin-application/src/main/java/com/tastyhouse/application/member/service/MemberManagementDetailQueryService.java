package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.in.MemberManagementDetailQueryUseCase;
import com.tastyhouse.application.member.port.out.MemberManagementDetailResult;
import com.tastyhouse.application.member.port.out.MemberManagementDetailWithProfileImageResult;
import com.tastyhouse.application.member.port.out.MemberManagementQueryPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class MemberManagementDetailQueryService implements MemberManagementDetailQueryUseCase {

    private final MemberManagementQueryPort memberManagementQueryPort;

    public MemberManagementDetailQueryService(MemberManagementQueryPort memberManagementQueryPort) {
        this.memberManagementQueryPort = memberManagementQueryPort;
    }

    @Override
    public MemberManagementDetailWithProfileImageResult getMember(Long id) {
        MemberManagementDetailResult member = memberManagementQueryPort.findManagementDetailById(MemberId.of(id).value())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.MEMBER_NOT_FOUND));

        String profileImageUrl = memberManagementQueryPort.findProfileImageUrl(MemberId.of(member.id()).value()).orElse(null);

        return new MemberManagementDetailWithProfileImageResult(member, profileImageUrl);
    }
}
