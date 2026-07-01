package com.tastyhouse.core.domain.follow.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.follow.application.dto.result.FollowMemberResult;
import com.tastyhouse.core.domain.follow.domain.model.Follow;
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

    PageResult<FollowMemberResult> findFollowingList(Long memberId, Long viewerMemberId, PageQuery pageQuery);

    PageResult<FollowMemberResult> findFollowerList(Long memberId, Long viewerMemberId, PageQuery pageQuery);
}
