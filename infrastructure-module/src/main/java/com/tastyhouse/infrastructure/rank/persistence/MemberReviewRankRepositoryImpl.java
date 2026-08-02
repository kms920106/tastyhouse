package com.tastyhouse.infrastructure.rank.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.rank.domain.model.MemberReviewRank;
import com.tastyhouse.domain.rank.domain.model.RankType;
import com.tastyhouse.domain.rank.domain.repository.MemberReviewRankRepository;

import static com.tastyhouse.infrastructure.rank.persistence.QMemberReviewRankJpaEntity.memberReviewRankJpaEntity;

/**
 * 회원 리뷰 랭킹 write 어댑터.
 *
 * <p>표현 목적 조회(랭킹 목록·내 랭킹 join 투영)는 같은 모듈의 {@code rank/query/RankQueryDao}로
 * 이관했다. 여기에는 랭킹 확정 트랜잭션의 일괄 삭제·적재와 등급 산정용 단건 로드만 남는다.
 */
@Repository
public class MemberReviewRankRepositoryImpl implements MemberReviewRankRepository {

    private final JPAQueryFactory queryFactory;
    private final MemberReviewRankJpaRepository memberReviewRankJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public MemberReviewRankRepositoryImpl(JPAQueryFactory queryFactory, MemberReviewRankJpaRepository memberReviewRankJpaRepository) {
        this.queryFactory = queryFactory;
        this.memberReviewRankJpaRepository = memberReviewRankJpaRepository;
    }

    @Override
    public Optional<MemberReviewRank> findLatestByMemberIdAndRankType(MemberId memberId, RankType rankType) {
        MemberReviewRankJpaEntity entity = queryFactory
            .selectFrom(memberReviewRankJpaEntity)
            .where(
                memberReviewRankJpaEntity.memberId.eq(memberId),
                memberReviewRankJpaEntity.rankType.eq(rankType)
            )
            .orderBy(memberReviewRankJpaEntity.baseDate.desc())
            .fetchFirst();
        return Optional.ofNullable(entity).map(MemberReviewRankMapper::toDomain);
    }

    @Override
    public void saveAll(List<MemberReviewRank> ranks) {
        List<MemberReviewRankJpaEntity> entities = ranks.stream()
            .map(MemberReviewRankMapper::toEntity)
            .toList();
        memberReviewRankJpaRepository.saveAll(entities);
    }

    /**
     * 벌크 삭제 후 1차 캐시를 비운다 — 같은 트랜잭션에서 곧바로 같은 기준일 랭킹을 새로 적재하므로,
     * 삭제된 행이 캐시에 남아 있으면 적재분과 충돌한다.
     */
    @Override
    public void deleteByRankTypeAndBaseDate(RankType rankType, LocalDate baseDate) {
        queryFactory
            .delete(memberReviewRankJpaEntity)
            .where(
                memberReviewRankJpaEntity.rankType.eq(rankType),
                memberReviewRankJpaEntity.baseDate.eq(baseDate)
            )
            .execute();
        entityManager.flush();
        entityManager.clear();
    }
}
