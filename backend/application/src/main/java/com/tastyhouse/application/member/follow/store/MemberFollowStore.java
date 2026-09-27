package com.tastyhouse.application.member.follow.store;

import java.util.Optional;

import com.tastyhouse.domain.member.follow.model.MemberFollow;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.follow.port.out.write.MemberFollowStatePort;

public class MemberFollowStore implements MemberFollowRepository {
    private final MemberFollowStatePort memberFollowStatePort;

    public MemberFollowStore(MemberFollowStatePort memberFollowStatePort) {
        this.memberFollowStatePort = memberFollowStatePort;
    }

    @Override
    public Optional<MemberFollow> findByFollowerIdAndFollowingId(MemberId followerId, MemberId followingId) {
        return memberFollowStatePort.findByFollowerIdAndFollowingId(followerId.value(), followingId.value())
            .map(MemberFollowStateMapper::toDomain);
    }

    @Override
    public boolean existsByFollowerIdAndFollowingId(MemberId followerId, MemberId followingId) {
        return memberFollowStatePort.existsByFollowerIdAndFollowingId(followerId.value(), followingId.value());
    }

    @Override
    public MemberFollow save(MemberFollow memberFollow) {
        return MemberFollowStateMapper.toDomain(memberFollowStatePort.save(MemberFollowStateMapper.toState(memberFollow)));
    }

    @Override
    public void delete(MemberFollow memberFollow) {
        memberFollowStatePort.delete(memberFollow.getId());
    }
}
