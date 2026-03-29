package com.tastyhouse.core.repository.follow;

import com.tastyhouse.core.entity.follow.Follow;
import com.tastyhouse.core.entity.follow.dto.FollowMemberDto;
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

    Page<FollowMemberDto> findFollowingList(Long memberId, Long viewerMemberId, Pageable pageable);

    Page<FollowMemberDto> findFollowerList(Long memberId, Long viewerMemberId, Pageable pageable);
}
