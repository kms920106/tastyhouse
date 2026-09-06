package com.tastyhouse.domain.member.follow.repository;

import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.member.follow.model.MemberFollow;

public interface MemberFollowRepository {
    Optional<MemberFollow> findByFollowerIdAndFollowingId(MemberId followerId, MemberId followingId);

    boolean existsByFollowerIdAndFollowingId(MemberId followerId, MemberId followingId);

    MemberFollow save(MemberFollow memberFollow);

    void delete(MemberFollow memberFollow);
}
