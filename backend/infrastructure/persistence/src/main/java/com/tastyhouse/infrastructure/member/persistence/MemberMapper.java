package com.tastyhouse.infrastructure.member.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberGender;
import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.domain.shared.vo.PhoneNumber;
import com.tastyhouse.infrastructure.shared.persistence.PhoneNumberEmbeddable;

final class MemberMapper {

    private MemberMapper() {
    }

    static Member toDomain(MemberJpaEntity entity) {
        return Member.reconstitute(
            entity.getId(),
            entity.getUsername(),
            entity.getPassword(),
            entity.getNickname(),
            entity.getFullName(),
            entity.getBirthDate(),
            entity.getGender() == null ? null : MemberGender.valueOf(entity.getGender()),
            phoneNumberOf(entity),
            entity.getMemberGrade() == null ? null : MemberGrade.valueOf(entity.getMemberGrade()),
            entity.getProfileImageFileId() == null ? null : UploadedFileId.of(entity.getProfileImageFileId()),
            entity.getStatusMessage(),
            entity.isPushNotificationEnabled(),
            entity.isMarketingInfoEnabled(),
            entity.isEventInfoEnabled(),
            entity.getMemberStatus() == null ? null : MemberStatus.valueOf(entity.getMemberStatus()),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static MemberJpaEntity toEntity(Member member) {
        return MemberJpaEntity.create(
            member.getUsername(),
            member.getPassword(),
            member.getNickname(),
            member.getFullName(),
            member.getBirthDate(),
            member.getGender() == null ? null : member.getGender().name(),
            phoneNumberEmbeddableOf(member),
            member.getMemberGrade() == null ? null : member.getMemberGrade().name(),
            member.getProfileImageFileId() == null ? null : member.getProfileImageFileId().value(),
            member.getStatusMessage(),
            member.isPushNotificationEnabled(),
            member.isMarketingInfoEnabled(),
            member.isEventInfoEnabled(),
            member.getMemberStatus() == null ? null : member.getMemberStatus().name()
        );
    }

    static void applyChanges(MemberJpaEntity entity, Member member) {
        entity.applyChanges(
            member.getPassword(),
            member.getNickname(),
            member.getFullName(),
            member.getBirthDate(),
            member.getGender() == null ? null : member.getGender().name(),
            phoneNumberEmbeddableOf(member),
            member.getProfileImageFileId() == null ? null : member.getProfileImageFileId().value(),
            member.getStatusMessage(),
            member.isPushNotificationEnabled(),
            member.isMarketingInfoEnabled(),
            member.isEventInfoEnabled(),
            member.getMemberStatus() == null ? null : member.getMemberStatus().name()
        );
    }

    private static PhoneNumber phoneNumberOf(MemberJpaEntity entity) {
        String phoneNumber = entity.getPhoneNumber() == null ? null : entity.getPhoneNumber().value();
        return phoneNumber == null ? null : new PhoneNumber(phoneNumber);
    }

    private static PhoneNumberEmbeddable phoneNumberEmbeddableOf(Member member) {
        String phoneNumber = member.getPhoneNumber() == null ? null : member.getPhoneNumber().value();
        return phoneNumber == null ? null : new PhoneNumberEmbeddable(phoneNumber);
    }
}
