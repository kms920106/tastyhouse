package com.tastyhouse.infrastructure.rank.query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.rank.domain.model.RankType;
import com.tastyhouse.domain.rank.domain.vo.RankPeriodId;
import com.tastyhouse.domain.rank.domain.vo.RankPrizeId;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity.memberJpaEntity;
import static com.tastyhouse.infrastructure.rank.persistence.QMemberReviewRankJpaEntity.memberReviewRankJpaEntity;
import static com.tastyhouse.infrastructure.rank.persistence.QRankPeriodJpaEntity.rankPeriodJpaEntity;
import static com.tastyhouse.infrastructure.rank.persistence.QRankPrizeJpaEntity.rankPrizeJpaEntity;

/**
 * 랭킹 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로 write
 * 포트({@code RankPeriodRepository}/{@code RankPrizeRepository}/{@code MemberReviewRankRepository})와
 * 역할이 겹치지 않는다. 소비 모듈(web-api/admin-api)의 {@code RankQueryService}가 이 DAO를 주입해
 * 사용하며, 그 덕분에 api 모듈은 QueryDSL을 알지 않는다.
 *
 * <p>도메인당 DAO 1개 원칙에 따라 소비자별 메서드를 이 한 클래스에 둔다. 메서드명에는 admin 마커를
 * 붙이지 않고 순수 동작명을 쓰며, 진행 중 랭킹 조회({@code findActivePrizes})와 기간별 관리 조회
 * ({@code findPrizesByPeriodId})는 시그니처와 반환 타입으로 구분한다. 소프트 삭제 도메인이므로 모든
 * 조회 경로에 {@code deleted.isFalse()} 필터를 유지한다.
 *
 * <p>조인으로 얻은 저장 경로는 {@link FileUrlResolver}로 표시용 URL까지 변환해 Result에 담는다 —
 * {@code @QueryProjection}은 생성자 직접 투영이라 변환을 투영식에 끼울 수 없어, fetch 직후 재조립한다.
 */
@Repository
public class RankQueryDao {

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public RankQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    /**
     * 현재 노출 중인 랭킹의 기간 — 시작일이 가장 늦은 1건. 노출 중인 기간이 없으면 비어 있다.
     */
    public Optional<RankDurationResult> findActiveDuration() {
        RankDurationResult result = queryFactory
            .select(new QRankDurationResult(
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

    /**
     * 노출 중인 랭킹 기간에 걸린 등수별 경품 목록(web) — 첨부 이미지 경로를 함께 가져온다.
     */
    public List<RankPrizeResult> findActivePrizes() {
        return queryFactory
            .select(new QRankPrizeResult(
                rankPrizeJpaEntity.id,
                rankPrizeJpaEntity.prizeRank,
                rankPrizeJpaEntity.name,
                rankPrizeJpaEntity.brand,
                uploadedFileJpaEntity.filePath
            ))
            .from(rankPeriodJpaEntity)
            .innerJoin(rankPrizeJpaEntity).on(prizeRankId().eq(rankPeriodJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(prizeImageFileId().eq(uploadedFileJpaEntity.id))
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

    /**
     * 기준일의 회원 랭킹 목록(순위 오름차순 상위 {@code limit}명) — web 랭킹 화면과 admin 관리 목록이
     * 같은 필드 셋을 소비한다.
     */
    public List<MemberRankResult> findMemberRanks(RankType rankType, LocalDate baseDate, int limit) {
        return queryFactory
            .select(memberRankProjection())
            .from(memberReviewRankJpaEntity)
            .innerJoin(memberJpaEntity).on(memberIdPath().eq(memberJpaEntity.id))
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

    /**
     * 기준일의 특정 회원 랭킹 1건 — 랭킹에 들지 못한 회원이면 비어 있다(소비 측에서 0위 응답으로 대체).
     */
    public Optional<MemberRankResult> findMemberRank(MemberId memberId, RankType rankType, LocalDate baseDate) {
        MemberRankResult result = queryFactory
            .select(memberRankProjection())
            .from(memberReviewRankJpaEntity)
            .innerJoin(memberJpaEntity).on(memberIdPath().eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .where(
                memberReviewRankJpaEntity.memberId.eq(memberId),
                memberReviewRankJpaEntity.rankType.eq(rankType),
                memberReviewRankJpaEntity.baseDate.eq(baseDate)
            )
            .fetchOne();

        return Optional.ofNullable(result).map(this::withResolvedImageUrl);
    }

    /**
     * 랭킹 기간 관리 목록(시작일 내림차순) — admin 기간 관리 화면이 소비한다.
     */
    public List<RankPeriodResult> findAllPeriods() {
        return queryFactory
            .select(rankPeriodProjection())
            .from(rankPeriodJpaEntity)
            .where(rankPeriodJpaEntity.deleted.isFalse())
            .orderBy(rankPeriodJpaEntity.startAt.desc())
            .fetch();
    }

    /**
     * 랭킹 기간 상세 1건 — admin 기간 상세 화면이 소비한다.
     */
    public Optional<RankPeriodResult> findPeriodById(RankPeriodId id) {
        RankPeriodResult result = queryFactory
            .select(rankPeriodProjection())
            .from(rankPeriodJpaEntity)
            .where(rankPeriodJpaEntity.id.eq(id.value()), rankPeriodJpaEntity.deleted.isFalse())
            .fetchOne();

        return Optional.ofNullable(result);
    }

    /**
     * 특정 기간의 등수별 경품 관리 목록(등수 오름차순) — admin 경품 관리 화면이 소비한다.
     */
    public List<RankPrizeManagementResult> findPrizesByPeriodId(RankPeriodId periodId) {
        return queryFactory
            .select(rankPrizeManagementProjection())
            .from(rankPrizeJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(prizeImageFileId()))
            .where(prizeRankId().eq(periodId.value()), rankPrizeJpaEntity.deleted.isFalse())
            .orderBy(rankPrizeJpaEntity.prizeRank.asc())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
    }

    /**
     * 랭킹 경품 상세 1건 — admin 경품 상세 화면이 소비한다.
     */
    public Optional<RankPrizeManagementResult> findPrizeById(RankPrizeId id) {
        RankPrizeManagementResult result = queryFactory
            .select(rankPrizeManagementProjection())
            .from(rankPrizeJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(prizeImageFileId()))
            .where(rankPrizeJpaEntity.id.eq(id.value()), rankPrizeJpaEntity.deleted.isFalse())
            .fetchOne();

        return Optional.ofNullable(result).map(this::withResolvedImageUrl);
    }

    private QMemberRankResult memberRankProjection() {
        return new QMemberRankResult(
            memberReviewRankJpaEntity.memberId,
            memberJpaEntity.nickname,
            uploadedFileJpaEntity.filePath,
            memberReviewRankJpaEntity.reviewCount,
            memberReviewRankJpaEntity.rankNo,
            memberJpaEntity.memberGrade
        );
    }

    private QRankPeriodResult rankPeriodProjection() {
        return new QRankPeriodResult(
            rankPeriodJpaEntity.id,
            rankPeriodJpaEntity.startAt,
            rankPeriodJpaEntity.endAt,
            rankPeriodJpaEntity.visible,
            rankPeriodJpaEntity.createdAt,
            rankPeriodJpaEntity.updatedAt
        );
    }

    private QRankPrizeManagementResult rankPrizeManagementProjection() {
        return new QRankPrizeManagementResult(
            rankPrizeJpaEntity.id,
            prizeRankId(),
            rankPrizeJpaEntity.prizeRank,
            rankPrizeJpaEntity.name,
            rankPrizeJpaEntity.brand,
            prizeImageFileId(),
            uploadedFileJpaEntity.originalFilename,
            uploadedFileJpaEntity.filePath
        );
    }

    /**
     * 투영된 저장 경로를 표시용 URL로 바꿔 재조립한다. 아래 세 메서드는 {@code @QueryProjection}이
     * 생성자 직접 투영이라 변환을 투영식에 넣을 수 없어 fetch 직후 호출한다.
     */
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

    /**
     * {@code @Convert} VO 컬럼인 {@code RANK_PRIZE.rank_id}를 raw {@code Long}으로 비교·투영하기 위한 path.
     */
    private NumberPath<Long> prizeRankId() {
        return Expressions.numberPath(Long.class, rankPrizeJpaEntity, "rankId");
    }

    /**
     * {@code @Convert} VO 컬럼인 {@code RANK_PRIZE.image_file_id}를 raw {@code Long}으로 비교·투영하기 위한 path.
     */
    private NumberPath<Long> prizeImageFileId() {
        return Expressions.numberPath(Long.class, rankPrizeJpaEntity, "imageFileId");
    }

    /**
     * {@code memberId}는 {@code @Convert}로 {@code MemberId} VO에 매핑돼 있어 raw {@code Long} PK와
     * 직접 비교할 수 없으므로, join 조건에서는 원시 경로로 우회한다.
     */
    private NumberPath<Long> memberIdPath() {
        return Expressions.numberPath(Long.class, memberReviewRankJpaEntity, "memberId");
    }

    /**
     * {@code @Convert} VO 컬럼인 {@code MEMBER.profile_image_file_id}를 raw {@code Long}으로 비교하기
     * 위한 path(member 도메인의 크로스 참조).
     */
    private NumberPath<Long> memberProfileImageFileId() {
        return Expressions.numberPath(Long.class, memberJpaEntity, "profileImageFileId");
    }
}
