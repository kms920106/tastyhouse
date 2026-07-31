package com.tastyhouse.domain.member.follow.domain.model;

import lombok.Getter;

import com.tastyhouse.domain.member.domain.vo.MemberId;

/**
 * 회원 팔로우 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code MemberFollowJpaEntity} + {@code MemberFollowMapper}가 담당한다. 상태전이가 없어
 * 생성(팔로우)과 삭제(언팔로우)만 존재하므로, 감사 시각을 소비하는 조회 결과가 없어 필드로 두지 않는다.
 */
@Getter
public class MemberFollow {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final MemberId followerId;
    private final MemberId followingId;

    private MemberFollow(Long id, MemberId followerId, MemberId followingId) {
        this.id = id;
        this.followerId = followerId;
        this.followingId = followingId;
    }

    /**
     * 신규 팔로우 관계를 생성한다. 아직 영속되지 않았으므로 식별자는 없다.
     */
    public static MemberFollow of(MemberId followerId, MemberId followingId) {
        return new MemberFollow(null, followerId, followingId);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static MemberFollow reconstitute(Long id, MemberId followerId, MemberId followingId) {
        return new MemberFollow(id, followerId, followingId);
    }
}
