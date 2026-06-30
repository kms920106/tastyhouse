package com.tastyhouse.core.domain.follow.application;

import com.tastyhouse.core.domain.follow.application.dto.result.FollowMemberResult;
import com.tastyhouse.core.domain.follow.domain.repository.FollowRepository;
import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.domain.member.application.dto.result.MemberWithProfileImageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FollowQueryService {

    private final FollowRepository followRepository;
    private final MemberQueryService memberQueryService;

    public Page<FollowMemberResult> findFollowingList(Long memberId, Long viewerMemberId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return followRepository.findFollowingList(memberId, viewerMemberId, pageable);
    }

    public Page<FollowMemberResult> findFollowerList(Long memberId, Long viewerMemberId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return followRepository.findFollowerList(memberId, viewerMemberId, pageable);
    }

    public Page<MemberWithProfileImageResult> findMembersByNicknameContaining(String nickname, int page, int size) {
        return memberQueryService.findByNicknameContaining(nickname, page, size);
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    public long countFollowing(Long memberId) {
        return followRepository.countByFollowerId(memberId);
    }

    public long countFollower(Long memberId) {
        return followRepository.countByFollowingId(memberId);
    }

    public List<Long> findFollowingIds(Long memberId) {
        return followRepository.findFollowingIdsByFollowerId(memberId);
    }
}
