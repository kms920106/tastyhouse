package com.tastyhouse.core.domain.member.follow.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.follow.domain.model.MemberFollow;
import com.tastyhouse.core.domain.member.follow.application.dto.result.FollowMemberResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface MemberFollowRepository {

    Optional<MemberFollow> findByFollowerIdAndFollowingId(MemberId followerId, MemberId followingId);

    boolean existsByFollowerIdAndFollowingId(MemberId followerId, MemberId followingId);

    List<Long> findFollowingIdsByFollowerId(MemberId followerId);

    long countByFollowerId(MemberId followerId);

    long countByFollowingId(MemberId followingId);

    MemberFollow save(MemberFollow memberFollow);

    void delete(MemberFollow memberFollow);

    PageResult<FollowMemberResult> findFollowingList(MemberId memberId, MemberId viewerMemberId, PageQuery pageQuery);

    PageResult<FollowMemberResult> findFollowerList(MemberId memberId, MemberId viewerMemberId, PageQuery pageQuery);
}
