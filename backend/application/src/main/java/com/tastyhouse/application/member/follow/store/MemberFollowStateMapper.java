package com.tastyhouse.application.member.follow.store;

import com.tastyhouse.domain.member.follow.model.MemberFollow;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.follow.port.out.write.MemberFollowState;

final class MemberFollowStateMapper {
    private MemberFollowStateMapper() {
    }

    static MemberFollow toDomain(MemberFollowState state) {
        return MemberFollow.reconstitute(
            state.id(),
            state.followerId() == null ? null : MemberId.of(state.followerId()),
            state.followingId() == null ? null : MemberId.of(state.followingId())
        );
    }

    static MemberFollowState toState(MemberFollow memberFollow) {
        return new MemberFollowState(
            memberFollow.getId(),
            memberFollow.getFollowerId() == null ? null : memberFollow.getFollowerId().value(),
            memberFollow.getFollowingId() == null ? null : memberFollow.getFollowingId().value()
        );
    }
}
