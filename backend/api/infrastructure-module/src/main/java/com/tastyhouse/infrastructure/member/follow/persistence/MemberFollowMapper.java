package com.tastyhouse.infrastructure.member.follow.persistence;

import com.tastyhouse.domain.member.follow.domain.model.MemberFollow;

/**
 * 회원 팔로우 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class MemberFollowMapper {

    private MemberFollowMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static MemberFollow toDomain(MemberFollowJpaEntity entity) {
        return MemberFollow.reconstitute(
            entity.getId(),
            entity.getFollowerId(),
            entity.getFollowingId()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static MemberFollowJpaEntity toEntity(MemberFollow domain) {
        return MemberFollowJpaEntity.create(
            domain.getFollowerId(),
            domain.getFollowingId()
        );
    }
}
