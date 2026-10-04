package com.tastyhouse.infrastructure.persistence.member.follow.persistence;

import com.tastyhouse.domain.member.follow.model.MemberFollow;
import com.tastyhouse.domain.member.vo.MemberId;

final class MemberFollowMapper {

    private MemberFollowMapper() {
    }

    static MemberFollow toDomain(MemberFollowJpaEntity entity) {
        return MemberFollow.reconstitute(
            entity.getId(),
            entity.getFollowerId() == null ? null : MemberId.of(entity.getFollowerId()),
            entity.getFollowingId() == null ? null : MemberId.of(entity.getFollowingId())
        );
    }

    static MemberFollowJpaEntity toEntity(MemberFollow memberFollow) {
        return MemberFollowJpaEntity.create(
            memberFollow.getFollowerId() == null ? null : memberFollow.getFollowerId().value(),
            memberFollow.getFollowingId() == null ? null : memberFollow.getFollowingId().value()
        );
    }
}
