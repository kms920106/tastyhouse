package com.tastyhouse.infrastructure.review.query;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.review.domain.vo.ReviewCommentId;
import com.tastyhouse.domain.review.domain.vo.ReviewId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
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
@RequiredArgsConstructor
public class ReviewManagementQueryDao {

    /**
     * 답글의 "누구에게 단 답글인지"(replyTo) 회원을 조인하기 위한 별칭. 작성자 조인과 같은 회원 테이블이라
     * 별칭을 분리해야 한다.
     */
    private static final QMemberJpaEntity replyToMember = new QMemberJpaEntity("replyToMember");

    private final JPAQueryFactory queryFactory;

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
                reviewJpaEntity.createdAt
            ))
            .from(reviewJpaEntity)
            .innerJoin(memberJpaEntity).on(Expressions.numberPath(Long.class, reviewJpaEntity, "memberId").eq(memberJpaEntity.id))
            .where(
                shopIdEq(condition.shopId()),
                productIdEq(condition.productId()),
                memberIdEq(condition.memberId()),
                hiddenEq(condition.hidden()),
                contentContains(condition.content()),
                ratingBetween(condition.minRating(), condition.maxRating())
            )
            .orderBy(reviewJpaEntity.createdAt.desc());

        long total = query.fetch().size();

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
                reviewJpaEntity.memberId,
                memberJpaEntity.nickname,
                uploadedFileJpaEntity.filePath,
                reviewJpaEntity.createdAt
            ))
            .from(reviewJpaEntity)
            .innerJoin(shopJpaEntity).on(reviewJpaEntity.shopId.eq(shopJpaEntity.id))
            .innerJoin(stationJpaEntity).on(shopJpaEntity.stationId.eq(stationJpaEntity.id))
            .innerJoin(memberJpaEntity).on(Expressions.numberPath(Long.class, reviewJpaEntity, "memberId").eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberJpaEntity.profileImageFileId.eq(uploadedFileJpaEntity.id))
            .where(reviewJpaEntity.id.eq(reviewId.value()))
            .fetchOne();

        if (result != null) {
            List<String> imageUrls = findImageUrlsByReviewId(reviewId.value());
            result = result.withImageUrls(imageUrls);
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
            .leftJoin(memberJpaEntity).on(commentMemberIdPath().eq(memberJpaEntity.id))
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
            .leftJoin(memberJpaEntity).on(replyMemberIdPath().eq(memberJpaEntity.id))
            .leftJoin(replyToMember).on(replyToMemberIdPath().eq(replyToMember.id))
            .where(reviewReplyJpaEntity.commentId.in(ids))
            .orderBy(reviewReplyJpaEntity.createdAt.asc())
            .fetch();
    }

    private BooleanExpression shopIdEq(Long shopId) {
        return shopId != null ? reviewJpaEntity.shopId.eq(shopId) : null;
    }

    private BooleanExpression productIdEq(Long productId) {
        return productId != null ? reviewJpaEntity.productId.eq(productId) : null;
    }

    private BooleanExpression memberIdEq(Long memberId) {
        return memberId != null ? Expressions.numberPath(Long.class, reviewJpaEntity, "memberId").eq(memberId) : null;
    }

    private BooleanExpression hiddenEq(Boolean hidden) {
        return hidden != null ? reviewJpaEntity.hidden.eq(hidden) : null;
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
     * 단일 리뷰의 이미지 URL 목록(정렬값 오름차순).
     */
    private List<String> findImageUrlsByReviewId(Long reviewId) {
        return queryFactory
            .select(uploadedFileJpaEntity.filePath)
            .from(reviewImageJpaEntity)
            .innerJoin(uploadedFileJpaEntity).on(reviewImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(reviewImageJpaEntity.reviewId.eq(reviewId))
            .orderBy(reviewImageJpaEntity.sort.asc())
            .fetch();
    }

    /**
     * 댓글의 {@code memberId}는 {@code @Convert}로 {@code MemberId} VO에 매핑돼 있어 회원 테이블의 raw
     * {@code Long} PK와 직접 join할 수 없다. 같은 컬럼을 {@code Long} 경로로 다시 노출해 join에 쓴다.
     */
    private NumberPath<Long> commentMemberIdPath() {
        return Expressions.numberPath(Long.class, reviewCommentJpaEntity, "memberId");
    }

    /**
     * 답글 작성자 {@code memberId}의 join 전용 {@code Long} 경로.
     */
    private NumberPath<Long> replyMemberIdPath() {
        return Expressions.numberPath(Long.class, reviewReplyJpaEntity, "memberId");
    }

    /**
     * 답글 대상 회원 {@code replyToMemberId}의 join 전용 {@code Long} 경로.
     */
    private NumberPath<Long> replyToMemberIdPath() {
        return Expressions.numberPath(Long.class, reviewReplyJpaEntity, "replyToMemberId");
    }
}
