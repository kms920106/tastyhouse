package com.tastyhouse.core.domain.follow.domain.repository;

import com.tastyhouse.core.domain.follow.application.dto.result.FollowMemberResult;
import com.tastyhouse.core.domain.follow.domain.model.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FollowRepository {

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    boolean existsByFollowingId(Long followingId);

    List<Long> findFollowingIdsByFollowerId(Long followerId);

    long countByFollowerId(Long followerId);

    long countByFollowingId(Long followingId);

    Follow save(Follow follow);

    void delete(Follow follow);

    Page<FollowMemberResult> findFollowingList(Long memberId, Long viewerMemberId, Pageable pageable);

    Page<FollowMemberResult> findFollowerList(Long memberId, Long viewerMemberId, Pageable pageable);
}
