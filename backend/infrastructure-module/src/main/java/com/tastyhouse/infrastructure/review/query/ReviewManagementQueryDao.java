package com.tastyhouse.infrastructure.review.query;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;
import com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity.memberJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewCommentJpaEntity.reviewCommentJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewImageJpaEntity.reviewImageJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewJpaEntity.reviewJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewReplyJpaEntity.reviewReplyJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QStationJpaEntity.stationJpaEntity;

/**
 * 리뷰 관리(admin) read 어댑터(CQRS query 측).
 *
 * <p>web/공용 조회는 {@code ReviewQueryDao}에 있고, 여기에는 관리 화면 전용 조회만 둔다. 관리 화면은
 * 숨김 처리된 리뷰·댓글·답글까지 모두 봐야 하므로 {@code hidden} 필터를 걸지 않는다는 점이 web 조회와
 * 다르다.
 *
 * <p>댓글·답글 목록은 과거 core 조회 서비스가 도메인 모델을 읽은 뒤 작성자 닉네임을 별도 조회해 맵으로
 * 붙였는데, 여기서는 회원 테이블을 join해 한 번에 투영한다.
 */
@Repository
public class ReviewManagementQueryDao {

    /**
     * 답글의 "누구에게 단 답글인지"(replyTo) 회원을 조인하기 위한 별칭. 작성자 조인과 같은 회원 테이블이라
     * 별칭을 분리해야 한다.
     */
    private static final QMemberJpaEntity replyToMember = new QMemberJpaEntity("replyToMember");

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ReviewManagementQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    /**
     * 리뷰 목록(숨김 포함) — 검색 조건으로 동적 필터링, 최신순.
     */
    public PageResult<ReviewListItemResult> findReviews(ReviewSearchCondition condition, PageQuery pageQuery) {
        JPAQuery<ReviewListItemResult> query = queryFactory
            .select(new QReviewListItemResult(
                reviewJpaEntity.id,
                reviewJpaEntity.shopId,
                reviewJpaEntity.productId,
                reviewJpaEntity.memberId,
                memberJpaEntity.nickname,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content,
                reviewJpaEntity.hidden,
                reviewJpaEntity.ownerOnly,
                reviewJpaEntity.createdAt
            ))
            .from(reviewJpaEntity)
            .innerJoin(memberJpaEntity).on(reviewJpaEntity.memberId.eq(memberJpaEntity.id))
            .where(
                shopIdEq(condition.shopId()),
                productIdEq(condition.productId()),
                memberIdEq(condition.memberId()),
                hiddenEq(condition.hidden()),
                ownerOnlyEq(condition.ownerOnly()),
                contentContains(condition.content()),
                ratingBetween(condition.minRating(), condition.maxRating())
            )
            .orderBy(reviewJpaEntity.createdAt.desc());

        long total = countReviews(condition);

        List<ReviewListItemResult> reviews = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(reviews, total, pageQuery.page(), pageQuery.size());
    }

    /**
     * 리뷰 상세(숨김 포함) — 이미지 URL을 함께 채운다. 없으면 비어 있다.
     */
    public Optional<ReviewManagementDetailResult> findReviewManagementDetail(ReviewId reviewId) {
        ReviewManagementDetailResult result = queryFactory
            .select(new QReviewManagementDetailResult(
                reviewJpaEntity.id,
                shopJpaEntity.id,
                shopJpaEntity.name,
                stationJpaEntity.stationName,
                reviewJpaEntity.content,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.tasteRating,
                reviewJpaEntity.amountRating,
                reviewJpaEntity.priceRating,
                reviewJpaEntity.atmosphereRating,
                reviewJpaEntity.kindnessRating,
                reviewJpaEntity.hygieneRating,
                reviewJpaEntity.willRevisit,
                reviewJpaEntity.hidden,
                reviewJpaEntity.ownerOnly,
                reviewJpaEntity.memberId,
                memberJpaEntity.nickname,
                uploadedFileJpaEntity.filePath,
                reviewJpaEntity.createdAt
            ))
            .from(reviewJpaEntity)
            .innerJoin(shopJpaEntity).on(reviewJpaEntity.shopId.eq(shopJpaEntity.id))
            .innerJoin(stationJpaEntity).on(shopStationId().eq(stationJpaEntity.id))
            .innerJoin(memberJpaEntity).on(reviewJpaEntity.memberId.eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .where(reviewJpaEntity.id.eq(reviewId.value()))
            .fetchOne();

        if (result != null) {
            List<String> imageUrls = findImageUrlsByReviewId(reviewId.value());
            result = withResolvedImageUrl(result.withImageUrls(imageUrls));
        }

        return Optional.ofNullable(result);
    }

    /**
     * 리뷰의 댓글 목록(숨김 포함) — 최신순. 작성자 닉네임을 회원 테이블 join으로 함께 투영한다.
     */
    public List<ReviewCommentListItemResult> findCommentsIncludingHidden(ReviewId reviewId) {
        return queryFactory
            .select(Projections.constructor(ReviewCommentListItemResult.class,
                reviewCommentJpaEntity.id,
                reviewCommentJpaEntity.memberId,
                memberJpaEntity.nickname,
                reviewCommentJpaEntity.content,
                reviewCommentJpaEntity.hidden,
                reviewCommentJpaEntity.createdAt
            ))
            .from(reviewCommentJpaEntity)
            .leftJoin(memberJpaEntity).on(reviewCommentJpaEntity.memberId.eq(memberJpaEntity.id))
            .where(reviewCommentJpaEntity.reviewId.eq(reviewId.value()))
            .orderBy(reviewCommentJpaEntity.createdAt.desc())
            .fetch();
    }

    /**
     * 여러 댓글에 달린 답글 목록(숨김 포함) — 작성순. 입력이 비어 있으면 조회하지 않고 빈 목록을 돌려준다.
     *
     * <p>답글 대상 회원(replyTo)은 없을 수 있어 leftJoin으로 붙인다.
     */
    public List<ReviewReplyListItemResult> findRepliesIncludingHidden(List<ReviewCommentId> commentIds) {
        if (commentIds.isEmpty()) {
            return List.of();
        }

        List<Long> ids = commentIds.stream().map(ReviewCommentId::value).toList();

        return queryFactory
            .select(Projections.constructor(ReviewReplyListItemResult.class,
                reviewReplyJpaEntity.id,
                reviewReplyJpaEntity.commentId,
                reviewReplyJpaEntity.memberId,
                memberJpaEntity.nickname,
                reviewReplyJpaEntity.replyToMemberId,
                replyToMember.nickname,
                reviewReplyJpaEntity.content,
                reviewReplyJpaEntity.hidden,
                reviewReplyJpaEntity.createdAt
            ))
            .from(reviewReplyJpaEntity)
            .leftJoin(memberJpaEntity).on(reviewReplyJpaEntity.memberId.eq(memberJpaEntity.id))
            .leftJoin(replyToMember).on(reviewReplyJpaEntity.replyToMemberId.eq(replyToMember.id))
            .where(reviewReplyJpaEntity.commentId.in(ids))
            .orderBy(reviewReplyJpaEntity.createdAt.asc())
            .fetch();
    }

    /**
     * 리뷰 관리 목록의 총 건수 — 목록 쿼리와 같은 {@code innerJoin}(member)·같은 where를 재현한다.
     *
     * <p>회원 조인은 {@code innerJoin}이라 짝이 없는 리뷰를 제외하므로 리뷰 테이블만 세면 값이 달라진다.
     * 조인이 1:1(회원 PK 동등)이라 행이 늘지 않아 {@code countDistinct}는 필요 없다.
     */
    private long countReviews(ReviewSearchCondition condition) {
        Long total = queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .innerJoin(memberJpaEntity).on(reviewJpaEntity.memberId.eq(memberJpaEntity.id))
            .where(
                shopIdEq(condition.shopId()),
                productIdEq(condition.productId()),
                memberIdEq(condition.memberId()),
                hiddenEq(condition.hidden()),
                ownerOnlyEq(condition.ownerOnly()),
                contentContains(condition.content()),
                ratingBetween(condition.minRating(), condition.maxRating())
            )
            .fetchOne();

        return total == null ? 0L : total;
    }

    private BooleanExpression shopIdEq(Long shopId) {
        return shopId != null ? reviewJpaEntity.shopId.eq(shopId) : null;
    }

    private BooleanExpression productIdEq(Long productId) {
        return productId != null ? reviewJpaEntity.productId.eq(productId) : null;
    }

    private BooleanExpression memberIdEq(Long memberId) {
        return memberId != null ? reviewJpaEntity.memberId.eq(memberId) : null;
    }

    private BooleanExpression hiddenEq(Boolean hidden) {
        return hidden != null ? reviewJpaEntity.hidden.eq(hidden) : null;
    }

    /**
     * 사장님만보기 필터. {@code null}이면 조건 없음(전체)이며 {@code hidden}과 동일한 패턴이다.
     *
     * <p>관리자는 두 축 모두 전량 열람이 기본이므로 필터를 <b>강제하지 않고</b> 검색 수단으로만 제공한다.
     */
    private BooleanExpression ownerOnlyEq(Boolean ownerOnly) {
        return ownerOnly != null ? reviewJpaEntity.ownerOnly.eq(ownerOnly) : null;
    }

    private BooleanExpression contentContains(String content) {
        return StringUtils.hasText(content) ? reviewJpaEntity.content.containsIgnoreCase(content) : null;
    }

    private BooleanExpression ratingBetween(Double minRating, Double maxRating) {
        if (minRating != null && maxRating != null) {
            return reviewJpaEntity.totalRating.between(minRating, maxRating);
        }
        if (minRating != null) {
            return reviewJpaEntity.totalRating.goe(minRating);
        }
        if (maxRating != null) {
            return reviewJpaEntity.totalRating.loe(maxRating);
        }
        return null;
    }

    /**
     * 단일 리뷰의 이미지 URL 목록(정렬값 오름차순). 저장 경로는 {@link FileUrlResolver}로 표시용 URL까지
     * 변환한 뒤 돌려준다.
     */
    private List<String> findImageUrlsByReviewId(Long reviewId) {
        List<String> filePaths = queryFactory
            .select(uploadedFileJpaEntity.filePath)
            .from(reviewImageJpaEntity)
            .innerJoin(uploadedFileJpaEntity).on(reviewImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(reviewImageJpaEntity.reviewId.eq(reviewId))
            .orderBy(reviewImageJpaEntity.sort.asc())
            .fetch();

        return fileUrlResolver.resolveAll(filePaths);
    }

    /**
     * 투영된 저장 경로를 표시용 URL로 바꿔 재조립한다. {@code @QueryProjection}은 생성자 직접 투영이라
     * 변환을 투영식에 넣을 수 없어 fetch 직후 호출한다.
     */
    private ReviewManagementDetailResult withResolvedImageUrl(ReviewManagementDetailResult row) {
        return new ReviewManagementDetailResult(
            row.id(),
            row.shopId(),
            row.shopName(),
            row.stationName(),
            row.content(),
            row.totalRating(),
            row.tasteRating(),
            row.amountRating(),
            row.priceRating(),
            row.atmosphereRating(),
            row.kindnessRating(),
            row.hygieneRating(),
            row.willRevisit(),
            row.hidden(),
            row.ownerOnly(),
            row.memberId(),
            row.memberNickname(),
            fileUrlResolver.resolve(row.memberProfileImageUrl()),
            row.createdAt(),
            row.imageUrls(),
            row.tagNames()
        );
    }

    /**
     * {@code @Convert} VO 컬럼인 {@code SHOP.station_id}를 raw {@code Long}으로 비교하기 위한
     * path(shop 도메인의 크로스 참조).
     */
    private NumberPath<Long> shopStationId() {
        return Expressions.numberPath(Long.class, shopJpaEntity, "stationId");
    }

    /**
     * {@code @Convert} VO 컬럼인 {@code MEMBER.profile_image_file_id}를 raw {@code Long}으로 비교하기
     * 위한 path(member 도메인의 크로스 참조).
     */
    private NumberPath<Long> memberProfileImageFileId() {
        return Expressions.numberPath(Long.class, memberJpaEntity, "profileImageFileId");
    }

}
