package com.tastyhouse.infrastructure.rank.query;

import com.tastyhouse.application.rank.port.out.RankManagementQueryPort;
import com.tastyhouse.application.rank.port.out.RankQueryPort;
import com.tastyhouse.application.rank.port.out.MemberRankResult;
import com.tastyhouse.application.rank.port.out.RankDurationResult;
import com.tastyhouse.application.rank.port.out.RankPeriodResult;
import com.tastyhouse.application.rank.port.out.RankPrizeManagementResult;
import com.tastyhouse.application.rank.port.out.RankPrizeResult;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.ConstructorExpression;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.rank.model.RankType;
import com.tastyhouse.domain.rank.vo.RankPeriodId;
import com.tastyhouse.domain.rank.vo.RankPrizeId;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity.memberJpaEntity;
import static com.tastyhouse.infrastructure.rank.persistence.QMemberReviewRankJpaEntity.memberReviewRankJpaEntity;
import static com.tastyhouse.infrastructure.rank.persistence.QRankPeriodJpaEntity.rankPeriodJpaEntity;
import static com.tastyhouse.infrastructure.rank.persistence.QRankPrizeJpaEntity.rankPrizeJpaEntity;

@Repository
public class RankQueryDao implements RankQueryPort, RankManagementQueryPort {
    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public RankQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
    public Optional<RankDurationResult> findActiveDuration() {
        RankDurationResult result = queryFactory
            .select(Projections.constructor(RankDurationResult.class,
                rankPeriodJpaEntity.startAt,
                rankPeriodJpaEntity.endAt
            ))
            .from(rankPeriodJpaEntity)
            .where(rankPeriodJpaEntity.visible.isTrue(), rankPeriodJpaEntity.deleted.isFalse())
            .orderBy(rankPeriodJpaEntity.startAt.desc())
            .limit(1)
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<RankPrizeResult> findActivePrizes() {
        return queryFactory
            .select(Projections.constructor(RankPrizeResult.class,
                rankPrizeJpaEntity.id,
                rankPrizeJpaEntity.prizeRank,
                rankPrizeJpaEntity.name,
                rankPrizeJpaEntity.brand,
                uploadedFileJpaEntity.filePath
            ))
            .from(rankPeriodJpaEntity)
            .innerJoin(rankPrizeJpaEntity).on(rankPrizeJpaEntity.rankId.eq(rankPeriodJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(rankPrizeJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(
                rankPeriodJpaEntity.visible.isTrue(),
                rankPeriodJpaEntity.deleted.isFalse(),
                rankPrizeJpaEntity.deleted.isFalse()
            )
            .orderBy(rankPeriodJpaEntity.startAt.desc(), rankPrizeJpaEntity.prizeRank.asc())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
    }

    @Override
    public List<MemberRankResult> findMemberRanks(RankType rankType, LocalDate baseDate, int limit) {
        return queryFactory
            .select(memberRankProjection())
            .from(memberReviewRankJpaEntity)
            .innerJoin(memberJpaEntity).on(memberReviewRankJpaEntity.memberId.eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .where(
                memberReviewRankJpaEntity.rankType.eq(rankType),
                memberReviewRankJpaEntity.baseDate.eq(baseDate)
            )
            .orderBy(memberReviewRankJpaEntity.rankNo.asc())
            .limit(limit)
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
    }

    @Override
    public Optional<MemberRankResult> findMemberRank(Long memberId, RankType rankType, LocalDate baseDate) {
        MemberRankResult result = queryFactory
            .select(memberRankProjection())
            .from(memberReviewRankJpaEntity)
            .innerJoin(memberJpaEntity).on(memberReviewRankJpaEntity.memberId.eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .where(
                memberReviewRankJpaEntity.memberId.eq(memberId),
                memberReviewRankJpaEntity.rankType.eq(rankType),
                memberReviewRankJpaEntity.baseDate.eq(baseDate)
            )
            .fetchOne();

        return Optional.ofNullable(result).map(this::withResolvedImageUrl);
    }

    @Override
    public List<RankPeriodResult> findAllPeriods() {
        return queryFactory
            .select(rankPeriodProjection())
            .from(rankPeriodJpaEntity)
            .where(rankPeriodJpaEntity.deleted.isFalse())
            .orderBy(rankPeriodJpaEntity.startAt.desc())
            .fetch();
    }

    @Override
    public Optional<RankPeriodResult> findPeriodById(RankPeriodId id) {
        RankPeriodResult result = queryFactory
            .select(rankPeriodProjection())
            .from(rankPeriodJpaEntity)
            .where(rankPeriodJpaEntity.id.eq(id.value()), rankPeriodJpaEntity.deleted.isFalse())
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<RankPrizeManagementResult> findPrizesByPeriodId(RankPeriodId periodId) {
        return queryFactory
            .select(rankPrizeManagementProjection())
            .from(rankPrizeJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(rankPrizeJpaEntity.imageFileId))
            .where(rankPrizeJpaEntity.rankId.eq(periodId.value()), rankPrizeJpaEntity.deleted.isFalse())
            .orderBy(rankPrizeJpaEntity.prizeRank.asc())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
    }

    @Override
    public Optional<RankPrizeManagementResult> findPrizeById(RankPrizeId id) {
        RankPrizeManagementResult result = queryFactory
            .select(rankPrizeManagementProjection())
            .from(rankPrizeJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(rankPrizeJpaEntity.imageFileId))
            .where(rankPrizeJpaEntity.id.eq(id.value()), rankPrizeJpaEntity.deleted.isFalse())
            .fetchOne();

        return Optional.ofNullable(result).map(this::withResolvedImageUrl);
    }

    private ConstructorExpression<MemberRankResult> memberRankProjection() {
        return Projections.constructor(MemberRankResult.class,
                memberReviewRankJpaEntity.memberId,
            memberJpaEntity.nickname,
            uploadedFileJpaEntity.filePath,
            memberReviewRankJpaEntity.reviewCount,
            memberReviewRankJpaEntity.rankNo,
            memberJpaEntity.memberGrade
        );
    }

    private ConstructorExpression<RankPeriodResult> rankPeriodProjection() {
        return Projections.constructor(RankPeriodResult.class,
                rankPeriodJpaEntity.id,
            rankPeriodJpaEntity.startAt,
            rankPeriodJpaEntity.endAt,
            rankPeriodJpaEntity.visible,
            rankPeriodJpaEntity.createdAt,
            rankPeriodJpaEntity.updatedAt
        );
    }

    private ConstructorExpression<RankPrizeManagementResult> rankPrizeManagementProjection() {
        return Projections.constructor(RankPrizeManagementResult.class,
                rankPrizeJpaEntity.id,
            rankPrizeJpaEntity.rankId,
            rankPrizeJpaEntity.prizeRank,
            rankPrizeJpaEntity.name,
            rankPrizeJpaEntity.brand,
            rankPrizeJpaEntity.imageFileId,
            uploadedFileJpaEntity.originalFilename,
            uploadedFileJpaEntity.filePath
        );
    }

    private MemberRankResult withResolvedImageUrl(MemberRankResult row) {
        return new MemberRankResult(
            row.memberId(),
            row.nickname(),
            fileUrlResolver.resolve(row.profileImageUrl()),
            row.reviewCount(),
            row.rankNo(),
            row.grade()
        );
    }

    private RankPrizeResult withResolvedImageUrl(RankPrizeResult row) {
        return new RankPrizeResult(
            row.id(),
            row.prizeRank(),
            row.name(),
            row.brand(),
            fileUrlResolver.resolve(row.imageUrl())
        );
    }

    private RankPrizeManagementResult withResolvedImageUrl(RankPrizeManagementResult row) {
        return new RankPrizeManagementResult(
            row.id(),
            row.periodId(),
            row.prizeRank(),
            row.name(),
            row.brand(),
            row.imageFileId(),
            row.imageFileName(),
            fileUrlResolver.resolve(row.imageUrl())
        );
    }

    private NumberPath<Long> memberProfileImageFileId() {
        return Expressions.numberPath(Long.class, memberJpaEntity, "profileImageFileId");
    }
}
