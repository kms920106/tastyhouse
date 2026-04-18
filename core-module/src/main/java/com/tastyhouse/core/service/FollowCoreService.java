package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.follow.Follow;
import com.tastyhouse.core.entity.follow.dto.FollowMemberDto;
import com.tastyhouse.core.entity.user.Member;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.repository.follow.FollowRepository;
import com.tastyhouse.core.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowCoreService {

    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new BusinessException(ErrorCode.FOLLOW_SELF_NOT_ALLOWED);
        }

        if (!memberRepository.existsById(followingId)) {
            throw new EntityNotFoundException(ErrorCode.FOLLOW_TARGET_NOT_FOUND);
        }

        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new BusinessException(ErrorCode.FOLLOW_ALREADY_EXISTS);
        }

        followRepository.save(Follow.of(followerId, followingId));
    }

    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FOLLOW_NOT_FOUND));

        followRepository.delete(follow);
    }

    @Transactional
    public void removeFollower(Long memberId, Long followerId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FOLLOW_NOT_FOUND));

        followRepository.delete(follow);
    }

    @Transactional(readOnly = true)
    public Page<FollowMemberDto> findFollowingList(Long memberId, Long viewerMemberId, int page, int size) {
        return followRepository.findFollowingList(memberId, viewerMemberId, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<FollowMemberDto> findFollowerList(Long memberId, Long viewerMemberId, int page, int size) {
        return followRepository.findFollowerList(memberId, viewerMemberId, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<Member> findMembersByNicknameContaining(String nickname, int page, int size) {
        return memberRepository.findByNicknameContaining(nickname, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Transactional(readOnly = true)
    public long countFollowing(Long memberId) {
        return followRepository.countByFollowerId(memberId);
    }

    @Transactional(readOnly = true)
    public long countFollower(Long memberId) {
        return followRepository.countByFollowingId(memberId);
    }
}
