package com.tastyhouse.infrastructure.member.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

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
            entity.getGender(),
            entity.getPhoneNumber(),
            entity.getMemberGrade(),
            IdMapping.vo(entity.getProfileImageFileId(), UploadedFileId::of),
            entity.getStatusMessage(),
            entity.isPushNotificationEnabled(),
            entity.isMarketingInfoEnabled(),
            entity.isEventInfoEnabled(),
            entity.getMemberStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static MemberJpaEntity toEntity(Member domain) {
        return MemberJpaEntity.create(
            domain.getUsername(),
            domain.getPassword(),
            domain.getNickname(),
            domain.getFullName(),
            domain.getBirthDate(),
            domain.getGender(),
            domain.getPhoneNumber(),
            domain.getMemberGrade(),
            IdMapping.raw(domain.getProfileImageFileId(), UploadedFileId::value),
            domain.getStatusMessage(),
            domain.isPushNotificationEnabled(),
            domain.isMarketingInfoEnabled(),
            domain.isEventInfoEnabled(),
            domain.getMemberStatus()
        );
    }

    static void applyChanges(MemberJpaEntity entity, Member domain) {
        entity.applyChanges(
            domain.getPassword(),
            domain.getNickname(),
            domain.getFullName(),
            domain.getBirthDate(),
            domain.getGender(),
            domain.getPhoneNumber(),
            IdMapping.raw(domain.getProfileImageFileId(), UploadedFileId::value),
            domain.getStatusMessage(),
            domain.isPushNotificationEnabled(),
            domain.isMarketingInfoEnabled(),
            domain.isEventInfoEnabled(),
            domain.getMemberStatus()
        );
    }
}
