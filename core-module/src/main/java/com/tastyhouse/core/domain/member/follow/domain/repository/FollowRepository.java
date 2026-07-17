package com.tastyhouse.core.domain.member.follow.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.follow.domain.model.Follow;
import com.tastyhouse.core.domain.member.follow.application.dto.result.FollowMemberResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface FollowRepository {

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    List<Long> findFollowingIdsByFollowerId(Long followerId);

    long countByFollowerId(Long followerId);

    long countByFollowingId(Long followingId);

    Follow save(Follow follow);

    void delete(Follow follow);

    PageResult<FollowMemberResult> findFollowingList(MemberId memberId, MemberId viewerMemberId, PageQuery pageQuery);

    PageResult<FollowMemberResult> findFollowerList(MemberId memberId, MemberId viewerMemberId, PageQuery pageQuery);
}
