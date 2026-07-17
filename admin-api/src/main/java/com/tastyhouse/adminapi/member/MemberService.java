package com.tastyhouse.adminapi.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.core.domain.member.domain.model.Member;
import com.tastyhouse.core.domain.member.domain.model.MemberGrade;
import com.tastyhouse.core.domain.member.domain.model.MemberStatus;
import com.tastyhouse.core.domain.member.domain.model.MemberWithdrawalReason;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.file.application.FileQueryService;
import com.tastyhouse.core.domain.member.application.MemberCommandService;
import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.domain.member.application.dto.MemberSearchCondition;
import com.tastyhouse.core.domain.member.application.dto.command.WithdrawMemberCommand;
import com.tastyhouse.core.domain.member.application.dto.result.MemberListItemResult;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.adminapi.member.response.MemberDetailResponse;
import com.tastyhouse.adminapi.member.response.MemberListItemResponse;
import com.tastyhouse.adminapi.member.response.MemberPageResponse;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberQueryService memberQueryService;
    private final MemberCommandService memberCommandService;
    private final FileQueryService fileQueryService;
    private final FileService fileService;

    public MemberPageResponse getMembers(
        String nickname,
        String username,
        String phone,
        String status,
        String grade,
        int page,
        int size
    ) {
        MemberSearchCondition condition = MemberSearchCondition.of(
            nickname,
            username,
            phone,
            status == null ? null : MemberStatus.from(status),
            grade == null ? null : MemberGrade.from(grade)
        );
        PageResult<MemberListItemResponse> pageResult = memberQueryService.findMembers(condition, page, size)
            .map(this::toMemberListItemResponse);
        return MemberPageResponse.from(pageResult);
    }

    public MemberDetailResponse getMember(Long id) {
        Member member = memberQueryService.getById(id);
        String profileImageUrl = resolveProfileImageUrl(member);
        return MemberDetailResponse.from(
            member.getId(),
            member.getUsername(),
            member.getNickname(),
            member.getFullName(),
            member.getPhoneNumber() != null ? member.getPhoneNumber().toString() : null,
            member.getGender().name(),
            member.getBirthDate(),
            member.getMemberGrade().name(),
            member.getMemberStatus().name(),
            member.getStatusMessage(),
            profileImageUrl,
            member.isPushNotificationEnabled(),
            member.isMarketingInfoEnabled(),
            member.isEventInfoEnabled(),
            member.getCreatedAt()
        );
    }

    public void suspend(Long id) {
        MemberId memberId = MemberId.of(id);
        memberCommandService.suspend(memberId);
    }

    public void activate(Long id) {
        MemberId memberId = MemberId.of(id);
        memberCommandService.activate(memberId);
    }

    public void withdraw(Long id, String reason, String reasonDetail) {
        MemberId memberId = MemberId.of(id);
        WithdrawMemberCommand command = WithdrawMemberCommand.of(memberId, MemberWithdrawalReason.from(reason), reasonDetail);
        memberCommandService.withdraw(command);
    }

    private String resolveProfileImageUrl(Member member) {
        if (member.getProfileImageFileId() == null) {
            return null;
        }
        return fileQueryService.findFilePath(UploadedFileId.of(member.getProfileImageFileId()))
            .map(fileService::getUrlByPath)
            .orElse(null);
    }

    private MemberListItemResponse toMemberListItemResponse(MemberListItemResult result) {
        return MemberListItemResponse.from(
            result.id(),
            result.username(),
            result.nickname(),
            result.fullName(),
            result.phoneNumber(),
            result.gender().name(),
            result.memberGrade().name(),
            result.memberStatus().name(),
            result.profileImageFilePath(),
            result.createdAt()
        );
    }
}
