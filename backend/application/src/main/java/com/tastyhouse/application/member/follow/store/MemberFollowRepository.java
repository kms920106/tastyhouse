package com.tastyhouse.application.member.follow.store;

import java.util.Optional;

import com.tastyhouse.domain.member.follow.model.MemberFollow;
import com.tastyhouse.domain.member.vo.MemberId;

public interface MemberFollowRepository {
    Optional<MemberFollow> findByFollowerIdAndFollowingId(MemberId followerId, MemberId followingId);

    boolean existsByFollowerIdAndFollowingId(MemberId followerId, MemberId followingId);

    MemberFollow save(MemberFollow memberFollow);

    void delete(MemberFollow memberFollow);
}
