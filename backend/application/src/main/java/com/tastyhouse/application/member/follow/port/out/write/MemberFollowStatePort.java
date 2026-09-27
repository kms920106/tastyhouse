package com.tastyhouse.application.member.follow.port.out.write;

import java.util.Optional;

public interface MemberFollowStatePort {
    Optional<MemberFollowState> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    MemberFollowState save(MemberFollowState state);

    void delete(Long id);
}
