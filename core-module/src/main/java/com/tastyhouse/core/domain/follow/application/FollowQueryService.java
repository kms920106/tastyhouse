package com.tastyhouse.core.domain.follow.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.follow.domain.repository.FollowRepository;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.follow.application.dto.result.FollowMemberResult;
import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.domain.member.application.dto.result.MemberWithProfileImageResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FollowQueryService {

    private final FollowRepository followRepository;
    private final MemberQueryService memberQueryService;

    public PageResult<FollowMemberResult> findFollowingList(MemberId memberId, MemberId viewerMemberId, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return followRepository.findFollowingList(memberId, viewerMemberId, pageQuery);
    }

    public PageResult<FollowMemberResult> findFollowerList(MemberId memberId, MemberId viewerMemberId, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return followRepository.findFollowerList(memberId, viewerMemberId, pageQuery);
    }

    public PageResult<MemberWithProfileImageResult> findMembersByNicknameContaining(String nickname, int page, int size) {
        return memberQueryService.findByNicknameContaining(nickname, page, size);
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    public long countFollowing(MemberId memberId) {
        return followRepository.countByFollowerId(memberId.value());
    }

    public long countFollower(MemberId memberId) {
        return followRepository.countByFollowingId(memberId.value());
    }

    public List<Long> findFollowingIds(MemberId memberId) {
        return followRepository.findFollowingIdsByFollowerId(memberId.value());
    }
}
