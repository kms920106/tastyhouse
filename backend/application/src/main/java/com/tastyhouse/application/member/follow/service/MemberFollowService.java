package com.tastyhouse.application.member.follow.service;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.member.follow.model.MemberFollow;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.follow.port.out.write.MemberFollowPersistencePort;
import com.tastyhouse.application.member.port.out.write.MemberPersistencePort;
import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public class MemberFollowService {

    private final MemberFollowPersistencePort memberFollowPersistencePort;
    private final MemberPersistencePort memberPersistencePort;

    public MemberFollowService(
        MemberFollowPersistencePort memberFollowPersistencePort,
        MemberPersistencePort memberPersistencePort
    ) {
        this.memberFollowPersistencePort = memberFollowPersistencePort;
        this.memberPersistencePort = memberPersistencePort;
    }

    public Long follow(MemberId followerId, MemberId followingId) {
        if (followerId.equals(followingId)) {
            throw new BusinessException(ErrorCode.FOLLOW_SELF_NOT_ALLOWED);
        }

        if (memberPersistencePort.findById(followingId).isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.FOLLOW_TARGET_NOT_FOUND);
        }

        if (memberFollowPersistencePort.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new BusinessException(ErrorCode.FOLLOW_ALREADY_EXISTS);
        }

        MemberFollow saved = memberFollowPersistencePort.save(MemberFollow.of(followerId, followingId));
        return saved.getId();
    }

    public void unfollow(MemberId followerId, MemberId followingId) {
        MemberFollow memberFollow = memberFollowPersistencePort.findByFollowerIdAndFollowingId(followerId, followingId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FOLLOW_NOT_FOUND));

        memberFollowPersistencePort.delete(memberFollow);
    }

    public void removeFollower(MemberId memberId, MemberId followerId) {
        MemberFollow memberFollow = memberFollowPersistencePort.findByFollowerIdAndFollowingId(followerId, memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FOLLOW_NOT_FOUND));

        memberFollowPersistencePort.delete(memberFollow);
    }
}
