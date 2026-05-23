package com.tastyhouse.core.entity.follow;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "FOLLOW",
    uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "following_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "follower_id", nullable = false)
    private Long followerId; // 팔로우를 건 회원 ID (MEMBER.id 참조)

    @Column(name = "following_id", nullable = false)
    private Long followingId; // 팔로우 대상 회원 ID (MEMBER.id 참조)

    public Follow(Long followerId, Long followingId) {
        this.followerId = followerId;
        this.followingId = followingId;
    }

    public static Follow of(Long followerId, Long followingId) {
        return new Follow(followerId, followingId);
    }
}
