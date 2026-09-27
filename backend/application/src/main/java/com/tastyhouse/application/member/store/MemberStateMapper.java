package com.tastyhouse.application.member.store;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberGender;
import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.domain.shared.vo.PhoneNumber;
import com.tastyhouse.application.member.port.out.write.MemberState;

final class MemberStateMapper {
    private MemberStateMapper() {
    }

    static Member toDomain(MemberState state) {
        return Member.reconstitute(
            state.id(),
            state.username(),
            state.password(),
            state.nickname(),
            state.fullName(),
            state.birthDate(),
            state.gender() == null ? null : MemberGender.valueOf(state.gender()),
            state.phoneNumber() == null ? null : new PhoneNumber(state.phoneNumber()),
            state.memberGrade() == null ? null : MemberGrade.valueOf(state.memberGrade()),
            state.profileImageFileId() == null ? null : UploadedFileId.of(state.profileImageFileId()),
            state.statusMessage(),
            state.pushNotificationEnabled(),
            state.marketingInfoEnabled(),
            state.eventInfoEnabled(),
            state.memberStatus() == null ? null : MemberStatus.valueOf(state.memberStatus()),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static MemberState toState(Member member) {
        return new MemberState(
            member.getId(),
            member.getUsername(),
            member.getPassword(),
            member.getNickname(),
            member.getFullName(),
            member.getBirthDate(),
            member.getGender() == null ? null : member.getGender().name(),
            member.getPhoneNumber() == null ? null : member.getPhoneNumber().value(),
            member.getMemberGrade() == null ? null : member.getMemberGrade().name(),
            member.getProfileImageFileId() == null ? null : member.getProfileImageFileId().value(),
            member.getStatusMessage(),
            member.isPushNotificationEnabled(),
            member.isMarketingInfoEnabled(),
            member.isEventInfoEnabled(),
            member.getMemberStatus() == null ? null : member.getMemberStatus().name(),
            member.getCreatedAt(),
            member.getUpdatedAt()
        );
    }
}
