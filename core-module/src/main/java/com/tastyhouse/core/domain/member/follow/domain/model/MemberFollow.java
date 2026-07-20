package com.tastyhouse.core.domain.member.follow.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.infrastructure.persistence.converter.MemberIdConverter;
import com.tastyhouse.core.shared.entity.BaseEntity;

@Getter
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberFollow extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "follower_id", nullable = false)
    private MemberId followerId;

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "following_id", nullable = false)
    private MemberId followingId;

    public MemberFollow(MemberId followerId, MemberId followingId) {
        this.followerId = followerId;
        this.followingId = followingId;
    }

    public static MemberFollow of(MemberId followerId, MemberId followingId) {
        return new MemberFollow(followerId, followingId);
    }
}
