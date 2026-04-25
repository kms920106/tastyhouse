package com.tastyhouse.core.repository.rank;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.rank.MemberReviewRank;
import com.tastyhouse.core.entity.rank.RankType;
import com.tastyhouse.core.entity.rank.dto.MemberRankDto;
import com.tastyhouse.core.entity.rank.dto.QMemberRankDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.tastyhouse.core.entity.file.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.entity.rank.QMemberReviewRank.memberReviewRank;
import static com.tastyhouse.core.entity.user.QMember.member;

@Repository
@RequiredArgsConstructor
public class MemberReviewRankRepositoryImpl implements MemberReviewRankRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MemberRankDto> findMemberRankList(RankType rankType, LocalDate baseDate, int limit) {
        return queryFactory
            .select(new QMemberRankDto(
                memberReviewRank.memberId,
                member.nickname,
                uploadedFile.filePath,
                memberReviewRank.reviewCount,
                memberReviewRank.rankNo,
                member.memberGrade
            ))
            .from(memberReviewRank)
            .innerJoin(member).on(memberReviewRank.memberId.eq(member.id))
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
    public MemberRankDto findMemberRank(Long memberId, RankType rankType, LocalDate baseDate) {
        return queryFactory
            .select(new QMemberRankDto(
                memberReviewRank.memberId,
                member.nickname,
                uploadedFile.filePath,
                memberReviewRank.reviewCount,
                memberReviewRank.rankNo,
                member.memberGrade
            ))
            .from(memberReviewRank)
            .innerJoin(member).on(memberReviewRank.memberId.eq(member.id))
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
            .where(
                memberReviewRank.memberId.eq(memberId),
                memberReviewRank.rankType.eq(rankType),
                memberReviewRank.baseDate.eq(baseDate)
            )
            .fetchOne();
    }

    @Override
    public Optional<MemberReviewRank> findLatestByMemberIdAndRankType(Long memberId, RankType rankType) {
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
    public void deleteByRankTypeAndBaseDate(RankType rankType, LocalDate baseDate) {
        queryFactory
            .delete(memberReviewRank)
            .where(memberReviewRank.rankType.eq(rankType), memberReviewRank.baseDate.eq(baseDate))
            .execute();
    }
}
