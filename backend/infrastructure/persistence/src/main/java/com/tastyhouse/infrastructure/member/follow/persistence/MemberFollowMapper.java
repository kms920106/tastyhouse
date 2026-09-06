package com.tastyhouse.infrastructure.member.follow.persistence;

import com.tastyhouse.domain.member.follow.model.MemberFollow;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class MemberFollowMapper {
    private MemberFollowMapper() {
    }

    static MemberFollow toDomain(MemberFollowJpaEntity entity) {
        return MemberFollow.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getFollowerId(), MemberId::of),
            IdMapping.vo(entity.getFollowingId(), MemberId::of)
        );
    }

    static MemberFollowJpaEntity toEntity(MemberFollow domain) {
        return MemberFollowJpaEntity.create(
            IdMapping.raw(domain.getFollowerId(), MemberId::value),
            IdMapping.raw(domain.getFollowingId(), MemberId::value)
        );
    }
}
