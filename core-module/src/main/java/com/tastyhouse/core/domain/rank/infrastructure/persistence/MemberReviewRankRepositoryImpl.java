package com.tastyhouse.core.domain.rank.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.member.domain.model.MemberGrade;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.rank.domain.model.MemberReviewRank;
import com.tastyhouse.core.domain.rank.domain.model.RankType;
import com.tastyhouse.core.domain.rank.domain.repository.MemberReviewRankRepository;
import com.tastyhouse.core.domain.rank.application.dto.result.MemberRankResult;
import com.tastyhouse.core.domain.rank.application.dto.result.QMemberRankResult;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.domain.rank.domain.model.QMemberReviewRank.memberReviewRank;

/**
 * {@code member}는 infrastructure-module로 이동한 {@code MemberJpaEntity}를 가리킨다.
 * core-module은 infrastructure-module을 의존할 수 없어(의존 방향: infrastructure → core)
 * 생성된 Q타입을 import할 수 없으므로, {@link PathBuilder}로 JPA 엔티티명("MemberJpaEntity")을
 * 문자열 참조해 필요한 컬럼만 타입 세이프하게 노출한다.
 */
@Repository
@RequiredArgsConstructor
public class MemberReviewRankRepositoryImpl implements MemberReviewRankRepository {

    private static final PathBuilder<Object> member = new PathBuilder<>(Object.class, "MemberJpaEntity");
    private static final NumberPath<Long> memberIdCol = member.getNumber("id", Long.class);
    private static final StringPath memberNicknameCol = member.getString("nickname");
    private static final NumberPath<Long> memberProfileImageFileIdCol = member.getNumber("profileImageFileId", Long.class);
    private static final EnumPath<MemberGrade> memberGradeCol = member.getEnum("memberGrade", MemberGrade.class);

    private final JPAQueryFactory queryFactory;
    private final MemberReviewRankJpaRepository memberReviewRankJpaRepository;

    @Override
    public List<MemberRankResult> findMemberRankList(RankType rankType, LocalDate baseDate, int limit) {
        return queryFactory
            .select(new QMemberRankResult(
                memberReviewRank.memberId,
                memberNicknameCol,
                uploadedFile.filePath,
                memberReviewRank.reviewCount,
                memberReviewRank.rankNo,
                memberGradeCol
            ))
            .from(memberReviewRank)
            .innerJoin(member).on(Expressions.numberPath(Long.class, memberReviewRank, "memberId").eq(memberIdCol))
            .leftJoin(uploadedFile).on(memberProfileImageFileIdCol.eq(uploadedFile.id))
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
                memberNicknameCol,
                uploadedFile.filePath,
                memberReviewRank.reviewCount,
                memberReviewRank.rankNo,
                memberGradeCol
            ))
            .from(memberReviewRank)
            .innerJoin(member).on(Expressions.numberPath(Long.class, memberReviewRank, "memberId").eq(memberIdCol))
            .leftJoin(uploadedFile).on(memberProfileImageFileIdCol.eq(uploadedFile.id))
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
