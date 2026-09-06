package com.tastyhouse.domain.member.follow.service;

import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.member.follow.model.MemberFollow;
import com.tastyhouse.domain.member.follow.repository.MemberFollowRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

public class MemberFollowService {
    private final MemberFollowRepository memberFollowRepository;
    private final MemberRepository memberRepository;

    public MemberFollowService(
        MemberFollowRepository memberFollowRepository,
        MemberRepository memberRepository
    ) {
        this.memberFollowRepository = memberFollowRepository;
        this.memberRepository = memberRepository;
    }

    public Long follow(MemberId followerId, MemberId followingId) {
        if (followerId.equals(followingId)) {
            throw new BusinessException(ErrorCode.FOLLOW_SELF_NOT_ALLOWED);
        }

        if (memberRepository.findById(followingId).isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.FOLLOW_TARGET_NOT_FOUND);
        }

        if (memberFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new BusinessException(ErrorCode.FOLLOW_ALREADY_EXISTS);
        }

        MemberFollow saved = memberFollowRepository.save(MemberFollow.of(followerId, followingId));
        return saved.getId();
    }

    public void unfollow(MemberId followerId, MemberId followingId) {
        MemberFollow memberFollow = memberFollowRepository.findByFollowerIdAndFollowingId(followerId, followingId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FOLLOW_NOT_FOUND));

        memberFollowRepository.delete(memberFollow);
    }

    public void removeFollower(MemberId memberId, MemberId followerId) {
        MemberFollow memberFollow = memberFollowRepository.findByFollowerIdAndFollowingId(followerId, memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FOLLOW_NOT_FOUND));

        memberFollowRepository.delete(memberFollow);
    }
}
