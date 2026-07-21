package com.tastyhouse.infrastructure.rank.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.rank.domain.model.MemberReviewRank;
import com.tastyhouse.core.domain.rank.domain.model.RankType;
import com.tastyhouse.core.domain.rank.domain.repository.MemberReviewRankRepository;
import com.tastyhouse.core.domain.rank.application.dto.result.MemberRankResult;
import com.tastyhouse.core.domain.rank.application.dto.result.QMemberRankResult;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity.memberJpaEntity;
import static com.tastyhouse.infrastructure.rank.persistence.QMemberReviewRankJpaEntity.memberReviewRankJpaEntity;

@Repository
@RequiredArgsConstructor
public class MemberReviewRankRepositoryImpl implements MemberReviewRankRepository {

    private final JPAQueryFactory queryFactory;
    private final MemberReviewRankJpaRepository memberReviewRankJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<MemberRankResult> findMemberRankList(RankType rankType, LocalDate baseDate, int limit) {
        return queryFactory
            .select(new QMemberRankResult(
                memberReviewRankJpaEntity.memberId,
                memberJpaEntity.nickname,
                uploadedFileJpaEntity.filePath,
                memberReviewRankJpaEntity.reviewCount,
                memberReviewRankJpaEntity.rankNo,
                memberJpaEntity.memberGrade
            ))
            .from(memberReviewRankJpaEntity)
            .innerJoin(memberJpaEntity).on(Expressions.numberPath(Long.class, memberReviewRankJpaEntity, "memberId").eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberJpaEntity.profileImageFileId.eq(uploadedFileJpaEntity.id))
            .where(
                memberReviewRankJpaEntity.rankType.eq(rankType),
                memberReviewRankJpaEntity.baseDate.eq(baseDate)
            )
            .orderBy(memberReviewRankJpaEntity.rankNo.asc())
            .limit(limit)
            .fetch();
    }

    @Override
    public MemberRankResult findMemberRank(MemberId memberId, RankType rankType, LocalDate baseDate) {
        return queryFactory
            .select(new QMemberRankResult(
                memberReviewRankJpaEntity.memberId,
                memberJpaEntity.nickname,
                uploadedFileJpaEntity.filePath,
                memberReviewRankJpaEntity.reviewCount,
                memberReviewRankJpaEntity.rankNo,
                memberJpaEntity.memberGrade
            ))
            .from(memberReviewRankJpaEntity)
            .innerJoin(memberJpaEntity).on(Expressions.numberPath(Long.class, memberReviewRankJpaEntity, "memberId").eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberJpaEntity.profileImageFileId.eq(uploadedFileJpaEntity.id))
            .where(
                memberReviewRankJpaEntity.memberId.eq(memberId),
                memberReviewRankJpaEntity.rankType.eq(rankType),
                memberReviewRankJpaEntity.baseDate.eq(baseDate)
            )
            .fetchOne();
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
