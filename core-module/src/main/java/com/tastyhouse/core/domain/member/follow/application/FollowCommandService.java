package com.tastyhouse.core.domain.member.follow.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.follow.domain.model.Follow;
import com.tastyhouse.core.domain.member.follow.domain.repository.FollowRepository;
import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class FollowCommandService {

    private final FollowRepository followRepository;
    private final MemberQueryService memberQueryService;

    public void follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new BusinessException(ErrorCode.FOLLOW_SELF_NOT_ALLOWED);
        }

        if (memberQueryService.findById(MemberId.of(followingId)).isEmpty()) {
            throw new EntityNotFoundException(ErrorCode.FOLLOW_TARGET_NOT_FOUND);
        }

        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new BusinessException(ErrorCode.FOLLOW_ALREADY_EXISTS);
        }

        followRepository.save(Follow.of(followerId, followingId));
    }

    public void unfollow(Long followerId, Long followingId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FOLLOW_NOT_FOUND));

        followRepository.delete(follow);
    }

    public void removeFollower(MemberId memberId, MemberId followerId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId.value(), memberId.value())
            .orElseThrow(() -> new BusinessException(ErrorCode.FOLLOW_NOT_FOUND));

        followRepository.delete(follow);
    }
}
