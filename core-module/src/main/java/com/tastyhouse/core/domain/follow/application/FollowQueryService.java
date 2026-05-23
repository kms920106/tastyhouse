package com.tastyhouse.core.domain.follow.application;

import com.tastyhouse.core.domain.follow.application.dto.result.FollowMemberResult;
import com.tastyhouse.core.domain.follow.domain.repository.FollowRepository;
import com.tastyhouse.core.entity.user.dto.MemberWithProfileImageDto;
import com.tastyhouse.core.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FollowQueryService {

    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;

    public Page<FollowMemberResult> findFollowingList(Long memberId, Long viewerMemberId, int page, int size) {
        return followRepository.findFollowingList(memberId, viewerMemberId, PageRequest.of(page, size));
    }

    public Page<FollowMemberResult> findFollowerList(Long memberId, Long viewerMemberId, int page, int size) {
        return followRepository.findFollowerList(memberId, viewerMemberId, PageRequest.of(page, size));
    }

    public Page<MemberWithProfileImageDto> findMembersByNicknameContaining(String nickname, int page, int size) {
        return memberRepository.findByNicknameContaining(nickname, PageRequest.of(page, size));
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
