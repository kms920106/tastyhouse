package com.tastyhouse.infrastructure.member.follow.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.infrastructure.member.persistence.MemberIdConverter;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 회원 팔로우 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code MemberFollow}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code MemberFollowMapper}가 수행한다.
 */
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

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "follower_id", nullable = false)
    private MemberId followerId;

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "following_id", nullable = false)
    private MemberId followingId;

    protected MemberFollowJpaEntity() {
    }

    private MemberFollowJpaEntity(MemberId followerId, MemberId followingId) {
        this.followerId = followerId;
        this.followingId = followingId;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code MemberFollowMapper#toEntity}에서만 호출한다.
     */
    static MemberFollowJpaEntity create(MemberId followerId, MemberId followingId) {
        return new MemberFollowJpaEntity(followerId, followingId);
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
