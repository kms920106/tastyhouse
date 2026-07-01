package com.tastyhouse.core.domain.follow.application;

import com.tastyhouse.core.domain.follow.application.dto.result.FollowMemberResult;
import com.tastyhouse.core.domain.follow.domain.repository.FollowRepository;
import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.domain.member.application.dto.result.MemberWithProfileImageResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FollowQueryService {

    private final FollowRepository followRepository;
    private final MemberQueryService memberQueryService;

    public PageResult<FollowMemberResult> findFollowingList(Long memberId, Long viewerMemberId, int page, int size) {
        return followRepository.findFollowingList(memberId, viewerMemberId, PageQuery.of(page, size));
    }

    public PageResult<FollowMemberResult> findFollowerList(Long memberId, Long viewerMemberId, int page, int size) {
        return followRepository.findFollowerList(memberId, viewerMemberId, PageQuery.of(page, size));
    }

    public PageResult<MemberWithProfileImageResult> findMembersByNicknameContaining(String nickname, int page, int size) {
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
