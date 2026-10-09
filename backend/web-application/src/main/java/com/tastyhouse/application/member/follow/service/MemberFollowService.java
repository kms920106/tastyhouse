package com.tastyhouse.application.member.follow.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.follow.model.MemberFollow;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.follow.port.out.write.MemberFollowLoadPort;
import com.tastyhouse.application.member.follow.port.out.write.MemberFollowSavePort;
import com.tastyhouse.application.member.port.out.write.MemberLoadPort;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
public class MemberFollowService {

    private final MemberFollowLoadPort memberFollowLoadPort;
    private final MemberFollowSavePort memberFollowSavePort;
    private final MemberLoadPort memberLoadPort;

    public MemberFollowService(
        MemberFollowLoadPort memberFollowLoadPort,
        MemberFollowSavePort memberFollowSavePort,
        MemberLoadPort memberLoadPort
    ) {
        this.memberFollowLoadPort = memberFollowLoadPort;
        this.memberFollowSavePort = memberFollowSavePort;
        this.memberLoadPort = memberLoadPort;
    }

    public Long follow(MemberId followerId, MemberId followingId) {
        if (followerId.equals(followingId)) {
            throw new ApplicationException(WebErrorCode.FOLLOW_SELF_NOT_ALLOWED);
        }

        if (memberLoadPort.findById(followingId).isEmpty()) {
            throw new ResourceNotFoundException(WebErrorCode.FOLLOW_TARGET_NOT_FOUND);
        }

        if (memberFollowLoadPort.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new ApplicationException(WebErrorCode.FOLLOW_ALREADY_EXISTS);
        }

        MemberFollow saved = memberFollowSavePort.save(MemberFollow.of(followerId, followingId));
        return saved.getId();
    }

    public void unfollow(MemberId followerId, MemberId followingId) {
        MemberFollow memberFollow = memberFollowLoadPort.findByFollowerIdAndFollowingId(followerId, followingId)
            .orElseThrow(() -> new ApplicationException(WebErrorCode.FOLLOW_NOT_FOUND));

        memberFollowSavePort.delete(memberFollow);
    }

    public void removeFollower(MemberId memberId, MemberId followerId) {
        MemberFollow memberFollow = memberFollowLoadPort.findByFollowerIdAndFollowingId(followerId, memberId)
            .orElseThrow(() -> new ApplicationException(WebErrorCode.FOLLOW_NOT_FOUND));

        memberFollowSavePort.delete(memberFollow);
    }
}
