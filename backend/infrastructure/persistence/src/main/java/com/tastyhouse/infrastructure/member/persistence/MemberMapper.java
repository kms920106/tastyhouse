package com.tastyhouse.infrastructure.member.persistence;

import com.tastyhouse.application.member.port.out.write.MemberState;
import com.tastyhouse.infrastructure.shared.persistence.PhoneNumberEmbeddable;

final class MemberMapper {
    private MemberMapper() {
    }

    static MemberState toState(MemberJpaEntity entity) {
        return new MemberState(
            entity.getId(),
            entity.getUsername(),
            entity.getPassword(),
            entity.getNickname(),
            entity.getFullName(),
            entity.getBirthDate(),
            entity.getGender(),
            entity.getPhoneNumber() == null ? null : entity.getPhoneNumber().value(),
            entity.getMemberGrade(),
            entity.getProfileImageFileId(),
            entity.getStatusMessage(),
            entity.isPushNotificationEnabled(),
            entity.isMarketingInfoEnabled(),
            entity.isEventInfoEnabled(),
            entity.getMemberStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static MemberJpaEntity toEntity(MemberState state) {
        return MemberJpaEntity.create(
            state.username(),
            state.password(),
            state.nickname(),
            state.fullName(),
            state.birthDate(),
            state.gender(),
            phoneNumberOf(state),
            state.memberGrade(),
            state.profileImageFileId(),
            state.statusMessage(),
            state.pushNotificationEnabled(),
            state.marketingInfoEnabled(),
            state.eventInfoEnabled(),
            state.memberStatus()
        );
    }

    static void applyChanges(MemberJpaEntity entity, MemberState state) {
        entity.applyChanges(
            state.password(),
            state.nickname(),
            state.fullName(),
            state.birthDate(),
            state.gender(),
            phoneNumberOf(state),
            state.profileImageFileId(),
            state.statusMessage(),
            state.pushNotificationEnabled(),
            state.marketingInfoEnabled(),
            state.eventInfoEnabled(),
            state.memberStatus()
        );
    }

    private static PhoneNumberEmbeddable phoneNumberOf(MemberState state) {
        return state.phoneNumber() == null ? null : new PhoneNumberEmbeddable(state.phoneNumber());
    }
}
