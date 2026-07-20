package com.tastyhouse.core.domain.member.follow.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.follow.domain.model.MemberFollow;
import com.tastyhouse.core.domain.member.follow.domain.repository.MemberFollowRepository;
import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberFollowCommandService {

    private final MemberFollowRepository memberFollowRepository;
    private final MemberQueryService memberQueryService;

    public void follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new BusinessException(ErrorCode.FOLLOW_SELF_NOT_ALLOWED);
        }

        MemberId followerMemberId = MemberId.of(followerId);
        MemberId followingMemberId = MemberId.of(followingId);

        if (memberQueryService.findById(followingMemberId).isEmpty()) {
            throw new EntityNotFoundException(ErrorCode.FOLLOW_TARGET_NOT_FOUND);
        }

        if (memberFollowRepository.existsByFollowerIdAndFollowingId(followerMemberId, followingMemberId)) {
            throw new BusinessException(ErrorCode.FOLLOW_ALREADY_EXISTS);
        }

        memberFollowRepository.save(MemberFollow.of(followerMemberId, followingMemberId));
    }

    public void unfollow(Long followerId, Long followingId) {
        MemberId followerMemberId = MemberId.of(followerId);
        MemberId followingMemberId = MemberId.of(followingId);

        MemberFollow memberFollow = memberFollowRepository.findByFollowerIdAndFollowingId(followerMemberId, followingMemberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FOLLOW_NOT_FOUND));

        memberFollowRepository.delete(memberFollow);
    }

    public void removeFollower(MemberId memberId, MemberId followerId) {
        MemberFollow memberFollow = memberFollowRepository.findByFollowerIdAndFollowingId(followerId, memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FOLLOW_NOT_FOUND));

        memberFollowRepository.delete(memberFollow);
    }
}
