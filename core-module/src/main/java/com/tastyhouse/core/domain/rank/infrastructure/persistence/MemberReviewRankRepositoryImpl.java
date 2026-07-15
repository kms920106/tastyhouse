package com.tastyhouse.core.domain.rank.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.rank.domain.model.MemberReviewRank;
import com.tastyhouse.core.domain.rank.domain.model.RankType;
import com.tastyhouse.core.domain.rank.domain.repository.MemberReviewRankRepository;
import com.tastyhouse.core.domain.rank.application.dto.result.MemberRankResult;
import com.tastyhouse.core.domain.rank.application.dto.result.QMemberRankResult;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.domain.member.domain.model.QMember.member;
import static com.tastyhouse.core.domain.rank.domain.model.QMemberReviewRank.memberReviewRank;

@Repository
@RequiredArgsConstructor
public class MemberReviewRankRepositoryImpl implements MemberReviewRankRepository {

    private final JPAQueryFactory queryFactory;
    private final MemberReviewRankJpaRepository memberReviewRankJpaRepository;

    @Override
    public List<MemberRankResult> findMemberRankList(RankType rankType, LocalDate baseDate, int limit) {
        return queryFactory
            .select(new QMemberRankResult(
                memberReviewRank.memberId,
                member.nickname,
                uploadedFile.filePath,
                memberReviewRank.reviewCount,
                memberReviewRank.rankNo,
                member.memberGrade
            ))
            .from(memberReviewRank)
            .innerJoin(member).on(Expressions.numberPath(Long.class, memberReviewRank, "memberId").eq(member.id))
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
            .where(
                memberReviewRank.rankType.eq(rankType),
                memberReviewRank.baseDate.eq(baseDate)
            )
            .orderBy(memberReviewRank.rankNo.asc())
            .limit(limit)
            .fetch();
    }

    @Override
    public MemberRankResult findMemberRank(MemberId memberId, RankType rankType, LocalDate baseDate) {
        return queryFactory
            .select(new QMemberRankResult(
                memberReviewRank.memberId,
                member.nickname,
                uploadedFile.filePath,
                memberReviewRank.reviewCount,
                memberReviewRank.rankNo,
                member.memberGrade
            ))
            .from(memberReviewRank)
            .innerJoin(member).on(Expressions.numberPath(Long.class, memberReviewRank, "memberId").eq(member.id))
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
            .where(
                memberReviewRank.memberId.eq(memberId),
                memberReviewRank.rankType.eq(rankType),
                memberReviewRank.baseDate.eq(baseDate)
            )
            .fetchOne();
    }

    @Override
    public Optional<MemberReviewRank> findLatestByMemberIdAndRankType(MemberId memberId, RankType rankType) {
        return Optional.ofNullable(queryFactory
            .selectFrom(memberReviewRank)
            .where(
                memberReviewRank.memberId.eq(memberId),
                memberReviewRank.rankType.eq(rankType)
            )
            .orderBy(memberReviewRank.baseDate.desc())
            .fetchFirst());
    }

    @Override
    public void saveAll(List<MemberReviewRank> ranks) {
        memberReviewRankJpaRepository.saveAll(ranks);
    }

    @Override
    public void deleteByRankTypeAndBaseDate(RankType rankType, LocalDate baseDate) {
        queryFactory
            .delete(memberReviewRank)
            .where(
                memberReviewRank.rankType.eq(rankType),
                memberReviewRank.baseDate.eq(baseDate)
            )
            .execute();
    }
}
