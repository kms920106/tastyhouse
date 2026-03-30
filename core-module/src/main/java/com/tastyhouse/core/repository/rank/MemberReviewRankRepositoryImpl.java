package com.tastyhouse.core.repository.rank;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.file.QUploadedFile;
import com.tastyhouse.core.entity.rank.MemberReviewRank;
import com.tastyhouse.core.entity.rank.QMemberReviewRank;
import com.tastyhouse.core.entity.rank.RankType;
import com.tastyhouse.core.entity.rank.dto.MemberRankDto;
import com.tastyhouse.core.entity.rank.dto.QMemberRankDto;
import com.tastyhouse.core.entity.user.QMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberReviewRankRepositoryImpl implements MemberReviewRankRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MemberRankDto> findMemberRankList(RankType rankType, LocalDate baseDate, int limit) {
        QMemberReviewRank rank = QMemberReviewRank.memberReviewRank;
        QMember member = QMember.member;
        QUploadedFile uploadedFile = QUploadedFile.uploadedFile;

        return queryFactory
            .select(new QMemberRankDto(
                rank.memberId,
                member.nickname,
                uploadedFile.filePath,
                rank.reviewCount,
                rank.rankNo,
                member.memberGrade
            ))
            .from(rank)
            .innerJoin(member).on(rank.memberId.eq(member.id))
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
            .where(
                rank.rankType.eq(rankType),
                rank.baseDate.eq(baseDate)
            )
            .orderBy(rank.rankNo.asc())
            .limit(limit)
            .fetch();
    }

    @Override
    public Optional<MemberRankDto> findMemberRank(Long memberId, RankType rankType, LocalDate baseDate) {
        QMemberReviewRank rank = QMemberReviewRank.memberReviewRank;
        QMember member = QMember.member;
        QUploadedFile uploadedFile = QUploadedFile.uploadedFile;

        return Optional.ofNullable(queryFactory
            .select(new QMemberRankDto(
                rank.memberId,
                member.nickname,
                uploadedFile.filePath,
                rank.reviewCount,
                rank.rankNo,
                member.memberGrade
            ))
            .from(rank)
            .innerJoin(member).on(rank.memberId.eq(member.id))
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
            .where(
                rank.memberId.eq(memberId),
                rank.rankType.eq(rankType),
                rank.baseDate.eq(baseDate)
            )
            .fetchOne());
    }

    @Override
    public Optional<MemberReviewRank> findLatestByMemberIdAndRankType(Long memberId, RankType rankType) {
        QMemberReviewRank rank = QMemberReviewRank.memberReviewRank;

        return Optional.ofNullable(queryFactory
            .selectFrom(rank)
            .where(
                rank.memberId.eq(memberId),
                rank.rankType.eq(rankType)
            )
            .orderBy(rank.baseDate.desc())
            .fetchFirst());
    }

    @Override
    public void deleteByRankTypeAndBaseDate(RankType rankType, LocalDate baseDate) {
        QMemberReviewRank rank = QMemberReviewRank.memberReviewRank;
        queryFactory
            .delete(rank)
            .where(rank.rankType.eq(rankType), rank.baseDate.eq(baseDate))
            .execute();
    }
}
