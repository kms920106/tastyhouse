package com.tastyhouse.infrastructure.rank.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.rank.model.MemberReviewRank;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 회원 리뷰 랭킹 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 * insert-only 애그리거트라 {@code applyChanges}는 두지 않는다.
 */
final class MemberReviewRankMapper {

    private MemberReviewRankMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static MemberReviewRank toDomain(MemberReviewRankJpaEntity entity) {
        return MemberReviewRank.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getMemberId(), MemberId::of),
            entity.getReviewCount(),
            entity.getRankNo(),
            entity.getRankType(),
            entity.getBaseDate(),
            entity.getLastReviewAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static MemberReviewRankJpaEntity toEntity(MemberReviewRank domain) {
        return MemberReviewRankJpaEntity.create(
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            domain.getReviewCount(),
            domain.getRankNo(),
            domain.getRankType(),
            domain.getBaseDate(),
            domain.getLastReviewAt()
        );
    }
}
