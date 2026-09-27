package com.tastyhouse.infrastructure.member.follow.persistence;

import com.tastyhouse.application.member.follow.port.out.write.MemberFollowState;

final class MemberFollowMapper {
    private MemberFollowMapper() {
    }

    static MemberFollowState toState(MemberFollowJpaEntity entity) {
        return new MemberFollowState(
            entity.getId(),
            entity.getFollowerId(),
            entity.getFollowingId()
        );
    }

    static MemberFollowJpaEntity toEntity(MemberFollowState state) {
        return MemberFollowJpaEntity.create(
            state.followerId(),
            state.followingId()
        );
    }
}
