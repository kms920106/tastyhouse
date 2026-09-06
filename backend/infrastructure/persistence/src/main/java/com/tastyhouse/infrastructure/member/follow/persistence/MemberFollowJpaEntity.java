package com.tastyhouse.infrastructure.member.follow.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(
    name = "MEMBER_FOLLOW",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_member_follow_follower_following",
        columnNames = {"follower_id", "following_id"}
    ),
    indexes = {
        @Index(name = "idx_member_follow_follower_id", columnList = "follower_id"),
        @Index(name = "idx_member_follow_following_id", columnList = "following_id")
    }
)
public class MemberFollowJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "follower_id", nullable = false)
    private Long followerId;

    @Column(name = "following_id", nullable = false)
    private Long followingId;

    protected MemberFollowJpaEntity() {
    }

    private MemberFollowJpaEntity(Long followerId, Long followingId) {
        this.followerId = followerId;
        this.followingId = followingId;
    }

    static MemberFollowJpaEntity create(Long followerId, Long followingId) {
        return new MemberFollowJpaEntity(followerId, followingId);
    }

    public Long getId() {
        return this.id;
    }

    public Long getFollowerId() {
        return this.followerId;
    }

    public Long getFollowingId() {
        return this.followingId;
    }
}
