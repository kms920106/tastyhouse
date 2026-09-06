package com.tastyhouse.infrastructure.review.query;

import com.tastyhouse.application.review.port.out.ReviewManagementQueryPort;
import com.tastyhouse.application.review.port.out.ReviewCommentListItemResult;
import com.tastyhouse.application.review.port.out.ReviewListItemResult;
import com.tastyhouse.application.review.port.out.ReviewManagementDetailResult;
import com.tastyhouse.application.review.port.out.ReviewReplyListItemResult;
import com.tastyhouse.application.review.port.out.ReviewSearchCondition;
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

@Repository
public class ReviewManagementQueryDao implements ReviewManagementQueryPort {
    private static final QMemberJpaEntity replyToMember = new QMemberJpaEntity("replyToMember");

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ReviewManagementQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
    public PageResult<ReviewListItemResult> findReviews(ReviewSearchCondition condition, PageQuery pageQuery) {
        JPAQuery<ReviewListItemResult> query = queryFactory
            .select(Projections.constructor(ReviewListItemResult.class,
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

    @Override
    public Optional<ReviewManagementDetailResult> findReviewManagementDetail(ReviewId reviewId) {
        ReviewManagementDetailResult result = queryFactory
            .select(Projections.constructor(ReviewManagementDetailResult.class,
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

    @Override
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

    @Override
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

    private NumberPath<Long> shopStationId() {
        return Expressions.numberPath(Long.class, shopJpaEntity, "stationId");
    }

    private NumberPath<Long> memberProfileImageFileId() {
        return Expressions.numberPath(Long.class, memberJpaEntity, "profileImageFileId");
    }
}
