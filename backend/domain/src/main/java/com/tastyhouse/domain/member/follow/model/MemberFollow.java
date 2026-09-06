package com.tastyhouse.domain.member.follow.model;

import com.tastyhouse.domain.member.vo.MemberId;

public class MemberFollow {
    private final Long id;
    private final MemberId followerId;
    private final MemberId followingId;

    private MemberFollow(Long id, MemberId followerId, MemberId followingId) {
        this.id = id;
        this.followerId = followerId;
        this.followingId = followingId;
    }

    public static MemberFollow of(MemberId followerId, MemberId followingId) {
        return new MemberFollow(null, followerId, followingId);
    }

    public static MemberFollow reconstitute(Long id, MemberId followerId, MemberId followingId) {
        return new MemberFollow(id, followerId, followingId);
    }

    public Long getId() {
        return this.id;
    }

    public MemberId getFollowerId() {
        return this.followerId;
    }

    public MemberId getFollowingId() {
        return this.followingId;
    }
}
