package com.tastyhouse.application.member.follow.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.member.follow.model.MemberFollow;
import com.tastyhouse.domain.member.vo.MemberId;

public interface MemberFollowLoadPort {

    Optional<MemberFollow> findByFollowerIdAndFollowingId(MemberId followerId, MemberId followingId);

    boolean existsByFollowerIdAndFollowingId(MemberId followerId, MemberId followingId);
}
