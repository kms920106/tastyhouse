package com.tastyhouse.infrastructure.jpa.review.query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLSubQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.review.port.out.BestReviewListItemResult;
import com.tastyhouse.application.review.port.out.LatestReviewListItemResult;
import com.tastyhouse.application.review.port.out.ReviewFeedQueryPort;
import com.tastyhouse.application.review.port.out.ReviewSortSpec;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.infrastructure.jpa.file.query.FileUrlResolver;
import com.tastyhouse.infrastructure.jpa.review.persistence.QReviewCommentJpaEntity;
import com.tastyhouse.infrastructure.jpa.review.persistence.QReviewImageJpaEntity;
import com.tastyhouse.infrastructure.jpa.review.persistence.QReviewLikeJpaEntity;
import com.tastyhouse.infrastructure.jpa.shared.query.IdStringRow;

import static com.tastyhouse.infrastructure.jpa.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.jpa.member.persistence.QMemberJpaEntity.memberJpaEntity;
import static com.tastyhouse.infrastructure.jpa.order.persistence.QOrderProductJpaEntity.orderProductJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductJpaEntity.productJpaEntity;
import static com.tastyhouse.infrastructure.jpa.review.persistence.QReviewImageJpaEntity.reviewImageJpaEntity;
import static com.tastyhouse.infrastructure.jpa.review.persistence.QReviewJpaEntity.reviewJpaEntity;
import static com.tastyhouse.infrastructure.jpa.review.persistence.QReviewOwnerReplyJpaEntity.reviewOwnerReplyJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopJpaEntity.shopJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QStationJpaEntity.stationJpaEntity;

@Repository
class ReviewFeedQueryAdapter implements ReviewFeedQueryPort {

    private static final QReviewImageJpaEntity subReviewImage = new QReviewImageJpaEntity("subReviewImage");
    private static final QReviewLikeJpaEntity subReviewLike = new QReviewLikeJpaEntity("subReviewLike");
    private static final QReviewCommentJpaEntity subReviewComment = new QReviewCommentJpaEntity("subReviewComment");
    private static final QReviewLikeJpaEntity sortReviewLike = new QReviewLikeJpaEntity("sortReviewLike");

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ReviewFeedQueryAdapter(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
    public PageResult<BestReviewListItemResult> findBestReviews(PageQuery pageQuery) {
        JPAQuery<BestReviewListItemResult> query = queryFactory
            .select(Projections.constructor(BestReviewListItemResult.class,
                reviewJpaEntity.id,
                fileUrlResolver.urlOf(uploadedFileJpaEntity.filePath),
                stationJpaEntity.stationName,
                shopJpaEntity.name,
                orderProductJpaEntity.name,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content
            ))
            .from(reviewJpaEntity)
            .innerJoin(shopJpaEntity).on(reviewJpaEntity.shopId.eq(shopJpaEntity.id))
            .innerJoin(stationJpaEntity).on(ReviewQueryPredicates.shopStationId().eq(stationJpaEntity.id))
            .leftJoin(orderProductJpaEntity).on(
                Expressions.numberPath(Long.class, orderProductJpaEntity, "orderId").eq(reviewJpaEntity.orderId)
                .and(Expressions.numberPath(Long.class, orderProductJpaEntity, "productId").eq(reviewJpaEntity.productId))
            )
            .leftJoin(reviewImageJpaEntity).on(
                reviewImageJpaEntity.reviewId.eq(reviewJpaEntity.id)
                .and(reviewImageJpaEntity.sort.eq(
                    JPAExpressions
                        .select(subReviewImage.sort.min())
                        .from(subReviewImage)
                        .where(subReviewImage.reviewId.eq(reviewJpaEntity.id))
                ))
            )
            .leftJoin(uploadedFileJpaEntity).on(reviewImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(ReviewQueryPredicates.visibleToCustomer())
            .orderBy(reviewJpaEntity.totalRating.desc(), reviewJpaEntity.createdAt.desc());

        long total = countBestReviews();

        List<BestReviewListItemResult> reviews = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(reviews, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<LatestReviewListItemResult> findLatestReviews(PageQuery pageQuery) {
        JPAQuery<LatestReviewListItemResult> query = selectLatestReviews()
            .where(ReviewQueryPredicates.visibleToCustomer())
            .orderBy(reviewJpaEntity.createdAt.desc());

        long total = countLatestReviews(ReviewQueryPredicates.visibleToCustomer());

        List<LatestReviewListItemResult> reviews = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(attachImageUrls(reviews), total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<LatestReviewListItemResult> findLatestReviewsByFollowing(List<Long> followingMemberIds, PageQuery pageQuery) {
        JPAQuery<LatestReviewListItemResult> query = selectLatestReviews()
            .where(
                reviewJpaEntity.memberId.in(followingMemberIds),
                ReviewQueryPredicates.visibleToCustomer()
            )
            .orderBy(reviewJpaEntity.createdAt.desc());

        long total = countLatestReviews(
            reviewJpaEntity.memberId.in(followingMemberIds)
                .and(ReviewQueryPredicates.visibleToCustomer())
        );

        List<LatestReviewListItemResult> reviews = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(attachImageUrls(reviews), total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<LatestReviewListItemResult> findLatestReviewsByShopId(Long shopId, Integer rating, PageQuery pageQuery, Boolean hasImage, ReviewSortSpec sort) {
        return findLatestReviewsPage(reviewJpaEntity.shopId.eq(shopId), rating, pageQuery, hasImage, sort);
    }

    @Override
    public PageResult<LatestReviewListItemResult> findLatestReviewsByProductId(Long productId, Integer rating, PageQuery pageQuery, Boolean hasImage, ReviewSortSpec sort) {
        return findLatestReviewsPage(reviewJpaEntity.productId.eq(productId), rating, pageQuery, hasImage, sort);
    }

    @Override
    public List<LatestReviewListItemResult> findReviewsByShopIdAndRating(Long shopId, Integer rating, int limit) {
        return findReviewsByRating(reviewJpaEntity.shopId.eq(shopId), rating, limit);
    }

    @Override
    public List<LatestReviewListItemResult> findReviewsByProductIdAndRating(Long productId, Integer rating, int limit) {
        return findReviewsByRating(reviewJpaEntity.productId.eq(productId), rating, limit);
    }

    private PageResult<LatestReviewListItemResult> findLatestReviewsPage(BooleanExpression scope, Integer rating, PageQuery pageQuery, Boolean hasImage, ReviewSortSpec sort) {
        var whereClause = scope.and(ReviewQueryPredicates.visibleToCustomer());
        if (rating != null) {
            whereClause = whereClause.and(ratingBand(rating));
        }

        if (hasImage != null) {
            whereClause = whereClause.and(imageExists(hasImage));
        }

        JPAQuery<LatestReviewListItemResult> query = selectLatestReviews()
            .where(whereClause);

        applySort(query, sort);

        long total = countLatestReviews(whereClause);

        List<LatestReviewListItemResult> reviews = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(attachImageUrls(reviews), total, pageQuery.page(), pageQuery.size());
    }

    private List<LatestReviewListItemResult> findReviewsByRating(BooleanExpression scope, int rating, int limit) {
        var whereClause = scope.and(ReviewQueryPredicates.visibleToCustomer()).and(ratingBand(rating));

        List<LatestReviewListItemResult> reviews = selectLatestReviews()
            .where(whereClause)
            .orderBy(reviewJpaEntity.createdAt.desc())
            .limit(limit)
            .fetch();

        return attachImageUrls(reviews);
    }

    private JPAQuery<LatestReviewListItemResult> selectLatestReviews() {
        return queryFactory
            .select(Projections.constructor(LatestReviewListItemResult.class,
                reviewJpaEntity.id,
                stationJpaEntity.stationName,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content,
                reviewJpaEntity.memberId,
                memberJpaEntity.nickname,
                fileUrlResolver.urlOf(uploadedFileJpaEntity.filePath),
                reviewJpaEntity.createdAt,
                productJpaEntity.id,
                productJpaEntity.name,
                JPAExpressions.select(subReviewLike.count())
                    .from(subReviewLike)
                    .where(subReviewLike.reviewId.eq(reviewJpaEntity.id)),
                JPAExpressions.select(subReviewComment.count())
                    .from(subReviewComment)
                    .where(subReviewComment.reviewId.eq(reviewJpaEntity.id)
                        .and(subReviewComment.hidden.eq(false))),
                reviewOwnerReplyJpaEntity.content,
                reviewOwnerReplyJpaEntity.createdAt
            ))
            .from(reviewJpaEntity)
            .innerJoin(shopJpaEntity).on(reviewJpaEntity.shopId.eq(shopJpaEntity.id))
            .innerJoin(stationJpaEntity).on(ReviewQueryPredicates.shopStationId().eq(stationJpaEntity.id))
            .innerJoin(memberJpaEntity).on(reviewJpaEntity.memberId.eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(ReviewQueryPredicates.memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .leftJoin(productJpaEntity).on(reviewJpaEntity.productId.eq(productJpaEntity.id))
            .leftJoin(reviewOwnerReplyJpaEntity)
            .on(reviewOwnerReplyJpaEntity.reviewId.eq(reviewJpaEntity.id));
    }

    private BooleanExpression ratingBand(int rating) {
        if (rating == 5) {
            return reviewJpaEntity.totalRating.eq(5.0);
        }
        return reviewJpaEntity.totalRating.goe((double) rating)
            .and(reviewJpaEntity.totalRating.lt((double) rating + 1.0));
    }

    private BooleanExpression imageExists(boolean hasImage) {
        JPQLSubQuery<Integer> imageOfReview = JPAExpressions
            .selectOne()
            .from(subReviewImage)
            .where(subReviewImage.reviewId.eq(reviewJpaEntity.id));
        return hasImage ? imageOfReview.exists() : imageOfReview.notExists();
    }

    private List<LatestReviewListItemResult> attachImageUrls(List<LatestReviewListItemResult> reviews) {
        if (reviews.isEmpty()) {
            return reviews;
        }

        List<Long> reviewIds = reviews.stream().map(LatestReviewListItemResult::id).toList();
        Map<Long, List<String>> imageUrlsMap = findImageUrlsByReviewIds(reviewIds);
        return reviews.stream()
            .map(r -> r.withImageUrls(imageUrlsMap.getOrDefault(r.id(), List.of())))
            .collect(Collectors.toList());
    }

    private long countBestReviews() {
        Long total = queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .innerJoin(shopJpaEntity).on(reviewJpaEntity.shopId.eq(shopJpaEntity.id))
            .innerJoin(stationJpaEntity).on(ReviewQueryPredicates.shopStationId().eq(stationJpaEntity.id))
            .where(ReviewQueryPredicates.visibleToCustomer())
            .fetchOne();

        return total == null ? 0L : total;
    }

    private long countLatestReviews(Predicate whereClause) {
        Long total = queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .innerJoin(shopJpaEntity).on(reviewJpaEntity.shopId.eq(shopJpaEntity.id))
            .innerJoin(stationJpaEntity).on(ReviewQueryPredicates.shopStationId().eq(stationJpaEntity.id))
            .innerJoin(memberJpaEntity).on(reviewJpaEntity.memberId.eq(memberJpaEntity.id))
            .where(whereClause)
            .fetchOne();

        return total == null ? 0L : total;
    }

    private void applySort(JPAQuery<LatestReviewListItemResult> query, ReviewSortSpec sort) {
        OrderSpecifier<LocalDateTime> createdAtOrder = sort.createdAtAscending()
            ? reviewJpaEntity.createdAt.asc()
            : reviewJpaEntity.createdAt.desc();
        if (sort.byLikeCount()) {
            query.leftJoin(sortReviewLike).on(sortReviewLike.reviewId.eq(reviewJpaEntity.id))
                .groupBy(reviewJpaEntity.id, stationJpaEntity.stationName, reviewJpaEntity.totalRating, reviewJpaEntity.content,
                    memberJpaEntity.id, memberJpaEntity.nickname, uploadedFileJpaEntity.filePath, reviewJpaEntity.createdAt,
                    productJpaEntity.id, productJpaEntity.name,
                    reviewOwnerReplyJpaEntity.content, reviewOwnerReplyJpaEntity.createdAt)
                .orderBy(sortReviewLike.count().desc(), createdAtOrder);
            return;
        }
        query.orderBy(createdAtOrder);
    }

    private Map<Long, List<String>> findImageUrlsByReviewIds(List<Long> reviewIds) {
        List<IdStringRow> results = queryFactory
            .select(Projections.constructor(IdStringRow.class, reviewImageJpaEntity.reviewId, uploadedFileJpaEntity.filePath))
            .from(reviewImageJpaEntity)
            .innerJoin(uploadedFileJpaEntity).on(reviewImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(reviewImageJpaEntity.reviewId.in(reviewIds))
            .orderBy(reviewImageJpaEntity.sort.asc())
            .fetch();

        return results.stream()
            .filter(row -> row.id() != null)
            .collect(Collectors.groupingBy(
                row -> Objects.requireNonNull(row.id()),
                Collectors.mapping(
                    row -> fileUrlResolver.resolve(Objects.toString(row.value(), "")),
                    Collectors.toList()
                )
            ));
    }
}
