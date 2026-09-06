package com.tastyhouse.infrastructure.review.query;

import com.tastyhouse.application.review.port.out.ReviewTagQueryPort;
import com.tastyhouse.application.review.port.out.ReviewQueryPort;
import com.tastyhouse.application.review.port.out.BestReviewListItemResult;
import com.tastyhouse.application.review.port.out.LatestReviewListItemResult;
import com.tastyhouse.application.review.port.out.MyReviewListItemResult;
import com.tastyhouse.application.review.port.out.ReviewCommentItemResult;
import com.tastyhouse.application.review.port.out.ReviewDetailResult;
import com.tastyhouse.application.review.port.out.ReviewReplyItemResult;
import com.tastyhouse.application.review.port.out.SearchReviewItemResult;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;
import com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity;
import com.tastyhouse.infrastructure.review.persistence.QReviewCommentJpaEntity;
import com.tastyhouse.infrastructure.review.persistence.QReviewImageJpaEntity;
import com.tastyhouse.infrastructure.review.persistence.QReviewLikeJpaEntity;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity.memberJpaEntity;
import static com.tastyhouse.infrastructure.order.persistence.QOrderJpaEntity.orderJpaEntity;
import static com.tastyhouse.infrastructure.order.persistence.QOrderProductJpaEntity.orderProductJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductJpaEntity.productJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewCommentJpaEntity.reviewCommentJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewImageJpaEntity.reviewImageJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewJpaEntity.reviewJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewLikeJpaEntity.reviewLikeJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewOwnerReplyJpaEntity.reviewOwnerReplyJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewReplyJpaEntity.reviewReplyJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewTagJpaEntity.reviewTagJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QStationJpaEntity.stationJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QTagJpaEntity.tagJpaEntity;

@Repository
public class ReviewQueryDao implements ReviewQueryPort, ReviewTagQueryPort {
    private static final QReviewImageJpaEntity subReviewImage = new QReviewImageJpaEntity("subReviewImage");
    private static final QReviewLikeJpaEntity subReviewLike = new QReviewLikeJpaEntity("subReviewLike");
    private static final QReviewCommentJpaEntity subReviewComment = new QReviewCommentJpaEntity("subReviewComment");
    private static final QReviewLikeJpaEntity sortReviewLike = new QReviewLikeJpaEntity("sortReviewLike");

    private static final QMemberJpaEntity replyToMember = new QMemberJpaEntity("replyToMember");

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ReviewQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    private BooleanExpression visibleToCustomer() {
        return reviewJpaEntity.hidden.isFalse().and(reviewJpaEntity.ownerOnly.isFalse());
    }

    private BooleanExpression visibleToViewer(Long viewerMemberId) {
        return viewerMemberId == null
            ? reviewJpaEntity.ownerOnly.isFalse()
            : reviewJpaEntity.ownerOnly.isFalse().or(reviewJpaEntity.memberId.eq(viewerMemberId));
    }

    @Override
    public PageResult<BestReviewListItemResult> findBestReviews(PageQuery pageQuery) {
        JPAQuery<BestReviewListItemResult> query = queryFactory
            .select(Projections.constructor(BestReviewListItemResult.class,
                reviewJpaEntity.id,
                uploadedFileJpaEntity.filePath,
                stationJpaEntity.stationName,
                shopJpaEntity.name,
                orderProductJpaEntity.name,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content
            ))
            .from(reviewJpaEntity)
            .innerJoin(shopJpaEntity).on(reviewJpaEntity.shopId.eq(shopJpaEntity.id))
            .innerJoin(stationJpaEntity).on(shopStationId().eq(stationJpaEntity.id))
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
            .where(visibleToCustomer())
            .orderBy(reviewJpaEntity.totalRating.desc(), reviewJpaEntity.createdAt.desc());

        long total = countBestReviews();

        List<BestReviewListItemResult> reviews = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();

        return PageResult.of(reviews, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<LatestReviewListItemResult> findLatestReviews(PageQuery pageQuery) {
        JPAQuery<LatestReviewListItemResult> query = queryFactory
            .select(Projections.constructor(LatestReviewListItemResult.class,
                reviewJpaEntity.id,
                stationJpaEntity.stationName,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content,
                reviewJpaEntity.memberId,
                memberJpaEntity.nickname,
                uploadedFileJpaEntity.filePath,
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
            .innerJoin(stationJpaEntity).on(shopStationId().eq(stationJpaEntity.id))
            .innerJoin(memberJpaEntity).on(reviewJpaEntity.memberId.eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .leftJoin(productJpaEntity).on(reviewJpaEntity.productId.eq(productJpaEntity.id))
            .leftJoin(reviewOwnerReplyJpaEntity)
            .on(reviewOwnerReplyJpaEntity.reviewId.eq(reviewJpaEntity.id))
            .where(visibleToCustomer())
            .orderBy(reviewJpaEntity.createdAt.desc());

        long total = countLatestReviews(visibleToCustomer());

        List<LatestReviewListItemResult> reviews = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        if (!reviews.isEmpty()) {
            List<Long> reviewIds = reviews.stream().map(LatestReviewListItemResult::id).toList();
            Map<Long, List<String>> imageUrlsMap = findImageUrlsByReviewIds(reviewIds);
            reviews = reviews.stream()
                .map(r -> r.withImageUrls(imageUrlsMap.getOrDefault(r.id(), List.of())))
                .map(this::withResolvedImageUrl)
                .collect(Collectors.toList());
        }

        return PageResult.of(reviews, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<LatestReviewListItemResult> findLatestReviewsByFollowing(List<Long> followingMemberIds, PageQuery pageQuery) {
        JPAQuery<LatestReviewListItemResult> query = queryFactory
            .select(Projections.constructor(LatestReviewListItemResult.class,
                reviewJpaEntity.id,
                stationJpaEntity.stationName,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content,
                reviewJpaEntity.memberId,
                memberJpaEntity.nickname,
                uploadedFileJpaEntity.filePath,
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
            .innerJoin(stationJpaEntity).on(shopStationId().eq(stationJpaEntity.id))
            .innerJoin(memberJpaEntity).on(reviewJpaEntity.memberId.eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .leftJoin(productJpaEntity).on(reviewJpaEntity.productId.eq(productJpaEntity.id))
            .leftJoin(reviewOwnerReplyJpaEntity)
            .on(reviewOwnerReplyJpaEntity.reviewId.eq(reviewJpaEntity.id))
            .where(
                reviewJpaEntity.memberId.in(followingMemberIds),
                visibleToCustomer()
            )
            .orderBy(reviewJpaEntity.createdAt.desc());

        long total = countLatestReviews(
            reviewJpaEntity.memberId.in(followingMemberIds)
                .and(visibleToCustomer())
        );

        List<LatestReviewListItemResult> reviews = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        if (!reviews.isEmpty()) {
            List<Long> reviewIds = reviews.stream().map(LatestReviewListItemResult::id).toList();
            Map<Long, List<String>> imageUrlsMap = findImageUrlsByReviewIds(reviewIds);
            reviews = reviews.stream()
                .map(r -> r.withImageUrls(imageUrlsMap.getOrDefault(r.id(), List.of())))
                .map(this::withResolvedImageUrl)
                .collect(Collectors.toList());
        }

        return PageResult.of(reviews, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<LatestReviewListItemResult> findLatestReviewsByShopId(Long shopId, Integer rating, PageQuery pageQuery, Boolean hasImage, ReviewSortType sortType) {
        var whereClause = reviewJpaEntity.shopId.eq(shopId).and(visibleToCustomer());
        if (rating != null) {
            if (rating == 5) {
                whereClause = whereClause.and(reviewJpaEntity.totalRating.eq(5.0));
            } else {
                whereClause = whereClause.and(
                    reviewJpaEntity.totalRating.goe(rating.doubleValue())
                        .and(reviewJpaEntity.totalRating.lt(rating.doubleValue() + 1.0))
                );
            }
        }

        if (hasImage != null) {
            if (hasImage) {
                whereClause = whereClause.and(
                    JPAExpressions
                        .selectOne()
                        .from(subReviewImage)
                        .where(subReviewImage.reviewId.eq(reviewJpaEntity.id))
                        .exists()
                );
            } else {
                whereClause = whereClause.and(
                    JPAExpressions
                        .selectOne()
                        .from(subReviewImage)
                        .where(subReviewImage.reviewId.eq(reviewJpaEntity.id))
                        .notExists()
                );
            }
        }

        JPAQuery<LatestReviewListItemResult> query = queryFactory
            .select(Projections.constructor(LatestReviewListItemResult.class,
                reviewJpaEntity.id,
                stationJpaEntity.stationName,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content,
                reviewJpaEntity.memberId,
                memberJpaEntity.nickname,
                uploadedFileJpaEntity.filePath,
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
            .innerJoin(stationJpaEntity).on(shopStationId().eq(stationJpaEntity.id))
            .innerJoin(memberJpaEntity).on(reviewJpaEntity.memberId.eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .leftJoin(productJpaEntity).on(reviewJpaEntity.productId.eq(productJpaEntity.id))
            .leftJoin(reviewOwnerReplyJpaEntity)
            .on(reviewOwnerReplyJpaEntity.reviewId.eq(reviewJpaEntity.id))
            .where(whereClause);

        applySort(query, sortType);

        long total = countLatestReviews(whereClause);

        List<LatestReviewListItemResult> reviews = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        if (!reviews.isEmpty()) {
            List<Long> reviewIds = reviews.stream().map(LatestReviewListItemResult::id).toList();
            Map<Long, List<String>> imageUrlsMap = findImageUrlsByReviewIds(reviewIds);
            reviews = reviews.stream()
                .map(r -> r.withImageUrls(imageUrlsMap.getOrDefault(r.id(), List.of())))
                .map(this::withResolvedImageUrl)
                .collect(Collectors.toList());
        }

        return PageResult.of(reviews, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<LatestReviewListItemResult> findLatestReviewsByProductId(Long productId, Integer rating, PageQuery pageQuery, Boolean hasImage, ReviewSortType sortType) {
        var whereClause = reviewJpaEntity.productId.eq(productId).and(visibleToCustomer());
        if (rating != null) {
            if (rating == 5) {
                whereClause = whereClause.and(reviewJpaEntity.totalRating.eq(5.0));
            } else {
                whereClause = whereClause.and(
                    reviewJpaEntity.totalRating.goe(rating.doubleValue())
                        .and(reviewJpaEntity.totalRating.lt(rating.doubleValue() + 1.0))
                );
            }
        }

        if (hasImage != null) {
            if (hasImage) {
                whereClause = whereClause.and(
                    JPAExpressions
                        .selectOne()
                        .from(subReviewImage)
                        .where(subReviewImage.reviewId.eq(reviewJpaEntity.id))
                        .exists()
                );
            } else {
                whereClause = whereClause.and(
                    JPAExpressions
                        .selectOne()
                        .from(subReviewImage)
                        .where(subReviewImage.reviewId.eq(reviewJpaEntity.id))
                        .notExists()
                );
            }
        }

        JPAQuery<LatestReviewListItemResult> query = queryFactory
            .select(Projections.constructor(LatestReviewListItemResult.class,
                reviewJpaEntity.id,
                stationJpaEntity.stationName,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content,
                reviewJpaEntity.memberId,
                memberJpaEntity.nickname,
                uploadedFileJpaEntity.filePath,
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
            .innerJoin(stationJpaEntity).on(shopStationId().eq(stationJpaEntity.id))
            .innerJoin(memberJpaEntity).on(reviewJpaEntity.memberId.eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .leftJoin(productJpaEntity).on(reviewJpaEntity.productId.eq(productJpaEntity.id))
            .leftJoin(reviewOwnerReplyJpaEntity)
            .on(reviewOwnerReplyJpaEntity.reviewId.eq(reviewJpaEntity.id))
            .where(whereClause);

        applySort(query, sortType);

        long total = countLatestReviews(whereClause);

        List<LatestReviewListItemResult> reviews = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        if (!reviews.isEmpty()) {
            List<Long> reviewIds = reviews.stream().map(LatestReviewListItemResult::id).toList();
            Map<Long, List<String>> imageUrlsMap = findImageUrlsByReviewIds(reviewIds);
            reviews = reviews.stream()
                .map(r -> r.withImageUrls(imageUrlsMap.getOrDefault(r.id(), List.of())))
                .map(this::withResolvedImageUrl)
                .collect(Collectors.toList());
        }

        return PageResult.of(reviews, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public List<LatestReviewListItemResult> findReviewsByShopIdAndRating(Long shopId, Integer rating, int limit) {
        var whereClause = reviewJpaEntity.shopId.eq(shopId).and(visibleToCustomer());

        if (rating == 5) {
            whereClause = whereClause.and(reviewJpaEntity.totalRating.eq(5.0));
        } else {
            whereClause = whereClause.and(
                reviewJpaEntity.totalRating.goe(rating.doubleValue())
                    .and(reviewJpaEntity.totalRating.lt(rating.doubleValue() + 1.0))
            );
        }

        List<LatestReviewListItemResult> reviews = queryFactory
            .select(Projections.constructor(LatestReviewListItemResult.class,
                reviewJpaEntity.id,
                stationJpaEntity.stationName,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content,
                reviewJpaEntity.memberId,
                memberJpaEntity.nickname,
                uploadedFileJpaEntity.filePath,
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
            .innerJoin(stationJpaEntity).on(shopStationId().eq(stationJpaEntity.id))
            .innerJoin(memberJpaEntity).on(reviewJpaEntity.memberId.eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .leftJoin(productJpaEntity).on(reviewJpaEntity.productId.eq(productJpaEntity.id))
            .leftJoin(reviewOwnerReplyJpaEntity)
            .on(reviewOwnerReplyJpaEntity.reviewId.eq(reviewJpaEntity.id))
            .where(whereClause)
            .orderBy(reviewJpaEntity.createdAt.desc())
            .limit(limit)
            .fetch();

        if (!reviews.isEmpty()) {
            List<Long> reviewIds = reviews.stream().map(LatestReviewListItemResult::id).toList();
            Map<Long, List<String>> imageUrlsMap = findImageUrlsByReviewIds(reviewIds);
            reviews = reviews.stream()
                .map(r -> r.withImageUrls(imageUrlsMap.getOrDefault(r.id(), List.of())))
                .map(this::withResolvedImageUrl)
                .collect(Collectors.toList());
        }

        return reviews;
    }

    @Override
    public List<LatestReviewListItemResult> findReviewsByProductIdAndRating(Long productId, Integer rating, int limit) {
        var whereClause = reviewJpaEntity.productId.eq(productId).and(visibleToCustomer());

        if (rating == 5) {
            whereClause = whereClause.and(reviewJpaEntity.totalRating.eq(5.0));
        } else {
            whereClause = whereClause.and(
                reviewJpaEntity.totalRating.goe(rating.doubleValue())
                    .and(reviewJpaEntity.totalRating.lt(rating.doubleValue() + 1.0))
            );
        }

        List<LatestReviewListItemResult> reviews = queryFactory
            .select(Projections.constructor(LatestReviewListItemResult.class,
                reviewJpaEntity.id,
                stationJpaEntity.stationName,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content,
                reviewJpaEntity.memberId,
                memberJpaEntity.nickname,
                uploadedFileJpaEntity.filePath,
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
            .innerJoin(stationJpaEntity).on(shopStationId().eq(stationJpaEntity.id))
            .innerJoin(memberJpaEntity).on(reviewJpaEntity.memberId.eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .leftJoin(productJpaEntity).on(reviewJpaEntity.productId.eq(productJpaEntity.id))
            .leftJoin(reviewOwnerReplyJpaEntity)
            .on(reviewOwnerReplyJpaEntity.reviewId.eq(reviewJpaEntity.id))
            .where(whereClause)
            .orderBy(reviewJpaEntity.createdAt.desc())
            .limit(limit)
            .fetch();

        if (!reviews.isEmpty()) {
            List<Long> reviewIds = reviews.stream().map(LatestReviewListItemResult::id).toList();
            Map<Long, List<String>> imageUrlsMap = findImageUrlsByReviewIds(reviewIds);
            reviews = reviews.stream()
                .map(r -> r.withImageUrls(imageUrlsMap.getOrDefault(r.id(), List.of())))
                .map(this::withResolvedImageUrl)
                .collect(Collectors.toList());
        }

        return reviews;
    }

    @Override
    public Optional<ReviewDetailResult> findReviewDetail(ReviewId reviewId, Long viewerMemberId) {
        ReviewDetailResult result = queryFactory
            .select(Projections.constructor(ReviewDetailResult.class,
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
                reviewJpaEntity.memberId,
                memberJpaEntity.nickname,
                uploadedFileJpaEntity.filePath,
                reviewJpaEntity.createdAt,
                reviewJpaEntity.ownerOnly,
                reviewOwnerReplyJpaEntity.content,
                reviewOwnerReplyJpaEntity.createdAt,
                orderJpaEntity.orderMethod,
                reviewJpaEntity.deliveryRating,
                reviewJpaEntity.deliveryComment
            ))
            .from(reviewJpaEntity)
            .innerJoin(shopJpaEntity).on(reviewJpaEntity.shopId.eq(shopJpaEntity.id))
            .innerJoin(stationJpaEntity).on(shopStationId().eq(stationJpaEntity.id))
            .innerJoin(memberJpaEntity).on(reviewJpaEntity.memberId.eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .leftJoin(reviewOwnerReplyJpaEntity)
            .on(reviewOwnerReplyJpaEntity.reviewId.eq(reviewJpaEntity.id))
            .leftJoin(orderJpaEntity).on(reviewJpaEntity.orderId.eq(orderJpaEntity.id))
            .where(
                reviewJpaEntity.id.eq(reviewId.value()),
                reviewJpaEntity.hidden.isFalse(),
                visibleToViewer(viewerMemberId)
            )
            .fetchOne();

        if (result != null) {
            List<String> imageUrls = findImageUrlsByReviewId(reviewId.value());
            result = withResolvedImageUrl(result.withImageUrls(imageUrls));
        }

        return Optional.ofNullable(result);
    }

    @Override
    public PageResult<MyReviewListItemResult> findMyReviews(Long memberId, PageQuery pageQuery) {
        List<Long> allReviewIds = queryFactory
            .select(reviewJpaEntity.id)
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.memberId.eq(memberId),
                reviewJpaEntity.hidden.eq(false)
            )
            .orderBy(reviewJpaEntity.createdAt.desc())
            .fetch();

        long total = allReviewIds.size();

        List<Tuple> pagedRows = queryFactory
            .select(reviewJpaEntity.id, reviewJpaEntity.ownerOnly)
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.memberId.eq(memberId),
                reviewJpaEntity.hidden.eq(false)
            )
            .orderBy(reviewJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        List<Long> pagedReviewIds = pagedRows.stream()
            .map(row -> row.get(reviewJpaEntity.id))
            .toList();

        Map<Long, String> imageUrlMap = findFirstImageUrlsByReviewIds(pagedReviewIds);

        List<MyReviewListItemResult> reviews = pagedRows.stream()
            .map(row -> {
                Long reviewId = row.get(reviewJpaEntity.id);
                return new MyReviewListItemResult(
                    reviewId,
                    imageUrlMap.get(reviewId),
                    Boolean.TRUE.equals(row.get(reviewJpaEntity.ownerOnly))
                );
            })
            .collect(Collectors.toList());

        return PageResult.of(reviews, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<MyReviewListItemResult> findReviewsByMemberId(Long memberId, PageQuery pageQuery) {
        List<Long> allReviewIds = queryFactory
            .select(reviewJpaEntity.id)
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.memberId.eq(memberId),
                visibleToCustomer()
            )
            .orderBy(reviewJpaEntity.createdAt.desc())
            .fetch();

        long total = allReviewIds.size();

        List<Long> pagedReviewIds = queryFactory
            .select(reviewJpaEntity.id)
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.memberId.eq(memberId),
                visibleToCustomer()
            )
            .orderBy(reviewJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        Map<Long, String> imageUrlMap = findFirstImageUrlsByReviewIds(pagedReviewIds);

        List<MyReviewListItemResult> reviews = pagedReviewIds.stream()
            .map(reviewId -> new MyReviewListItemResult(reviewId, imageUrlMap.get(reviewId), false))
            .collect(Collectors.toList());

        return PageResult.of(reviews, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<SearchReviewItemResult> searchByKeyword(String keyword, PageQuery pageQuery) {
        Long total = queryFactory
            .select(reviewJpaEntity.countDistinct())
            .from(reviewJpaEntity)
            .innerJoin(reviewImageJpaEntity).on(reviewImageJpaEntity.reviewId.eq(reviewJpaEntity.id))
            .where(
                reviewJpaEntity.content.containsIgnoreCase(keyword)
                .and(visibleToCustomer())
            )
            .fetchOne();

        if (total == null || total == 0) return PageResult.empty(pageQuery.page(), pageQuery.size());

        List<SearchReviewItemResult> content = queryFactory
            .select(Projections.constructor(SearchReviewItemResult.class,
                reviewJpaEntity.id,
                uploadedFileJpaEntity.filePath
            ))
            .from(reviewJpaEntity)
            .innerJoin(reviewImageJpaEntity).on(
                reviewImageJpaEntity.reviewId.eq(reviewJpaEntity.id)
                .and(reviewImageJpaEntity.sort.eq(
                    JPAExpressions.select(subReviewImage.sort.min())
                        .from(subReviewImage)
                        .where(subReviewImage.reviewId.eq(reviewJpaEntity.id))
                ))
            )
            .innerJoin(uploadedFileJpaEntity).on(reviewImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(
                reviewJpaEntity.content.containsIgnoreCase(keyword)
                .and(visibleToCustomer())
            )
            .orderBy(reviewJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public boolean existsByOrderIdAndProductIdAndMemberId(Long orderId, Long productId, Long memberId) {
        return queryFactory
            .selectOne()
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.orderId.eq(orderId),
                reviewJpaEntity.productId.eq(productId),
                reviewJpaEntity.memberId.eq(memberId)
            )
            .fetchFirst() != null;
    }

    @Override
    public Set<Long> findReviewedProductIds(Long orderId, Long memberId, Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Set.of();
        }

        return queryFactory
            .select(reviewJpaEntity.productId)
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.orderId.eq(orderId),
                reviewJpaEntity.productId.in(productIds),
                reviewJpaEntity.memberId.eq(memberId)
            )
            .fetch()
            .stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Optional<Long> findProductIdByReviewId(Long reviewId) {
        return Optional.ofNullable(queryFactory
            .select(reviewJpaEntity.productId)
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.id.eq(reviewId))
            .fetchOne());
    }

    @Override
    public boolean existsLike(ReviewId reviewId, Long memberId) {
        return queryFactory
            .selectOne()
            .from(reviewLikeJpaEntity)
            .where(
                reviewLikeJpaEntity.reviewId.eq(reviewId.value()),
                reviewLikeJpaEntity.memberId.eq(memberId)
            )
            .fetchFirst() != null;
    }

    @Override
    public List<ReviewCommentItemResult> findComments(ReviewId reviewId) {
        return queryFactory
            .select(Projections.constructor(ReviewCommentItemResult.class,
                reviewCommentJpaEntity.id,
                reviewCommentJpaEntity.reviewId,
                reviewCommentJpaEntity.memberId,
                memberJpaEntity.nickname,
                uploadedFileJpaEntity.filePath,
                reviewCommentJpaEntity.content,
                reviewCommentJpaEntity.createdAt
            ))
            .from(reviewCommentJpaEntity)
            .leftJoin(memberJpaEntity)
            .on(reviewCommentJpaEntity.memberId.eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .where(reviewCommentJpaEntity.reviewId.eq(reviewId.value()))
            .orderBy(reviewCommentJpaEntity.createdAt.desc())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
    }

    @Override
    public List<ReviewReplyItemResult> findVisibleReplies(List<ReviewCommentId> commentIds) {
        if (commentIds.isEmpty()) {
            return List.of();
        }

        List<Long> ids = commentIds.stream().map(ReviewCommentId::value).toList();

        return queryFactory
            .select(Projections.constructor(ReviewReplyItemResult.class,
                reviewReplyJpaEntity.id,
                reviewReplyJpaEntity.commentId,
                reviewReplyJpaEntity.memberId,
                memberJpaEntity.nickname,
                uploadedFileJpaEntity.filePath,
                reviewReplyJpaEntity.replyToMemberId,
                replyToMember.nickname,
                reviewReplyJpaEntity.content,
                reviewReplyJpaEntity.createdAt
            ))
            .from(reviewReplyJpaEntity)
            .leftJoin(memberJpaEntity)
            .on(reviewReplyJpaEntity.memberId.eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .leftJoin(replyToMember)
            .on(reviewReplyJpaEntity.replyToMemberId.eq(replyToMember.id))
            .where(
                reviewReplyJpaEntity.commentId.in(ids),
                reviewReplyJpaEntity.hidden.eq(false)
            )
            .orderBy(reviewReplyJpaEntity.createdAt.asc())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
    }

    @Override
    public List<Long> findTagIdsByReviewId(Long reviewId) {
        return queryFactory
            .select(reviewTagJpaEntity.tagId)
            .from(reviewTagJpaEntity)
            .where(reviewTagJpaEntity.reviewId.eq(reviewId))
            .fetch();
    }

    @Override
    public List<String> findTagNamesByIds(List<Long> tagIds) {
        if (tagIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
            .select(tagJpaEntity.tagName)
            .from(tagJpaEntity)
            .where(tagJpaEntity.id.in(tagIds))
            .fetch();
    }

    private long countBestReviews() {
        Long total = queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .innerJoin(shopJpaEntity).on(reviewJpaEntity.shopId.eq(shopJpaEntity.id))
            .innerJoin(stationJpaEntity).on(shopStationId().eq(stationJpaEntity.id))
            .where(visibleToCustomer())
            .fetchOne();

        return total == null ? 0L : total;
    }

    private long countLatestReviews(Predicate whereClause) {
        Long total = queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .innerJoin(shopJpaEntity).on(reviewJpaEntity.shopId.eq(shopJpaEntity.id))
            .innerJoin(stationJpaEntity).on(shopStationId().eq(stationJpaEntity.id))
            .innerJoin(memberJpaEntity).on(reviewJpaEntity.memberId.eq(memberJpaEntity.id))
            .where(whereClause)
            .fetchOne();

        return total == null ? 0L : total;
    }

    private void applySort(JPAQuery<LatestReviewListItemResult> query, ReviewSortType sortType) {
        switch (sortType) {
            case RECOMMENDED -> query.leftJoin(sortReviewLike).on(sortReviewLike.reviewId.eq(reviewJpaEntity.id))
                .groupBy(reviewJpaEntity.id, stationJpaEntity.stationName, reviewJpaEntity.totalRating, reviewJpaEntity.content,
                    memberJpaEntity.id, memberJpaEntity.nickname, uploadedFileJpaEntity.filePath, reviewJpaEntity.createdAt,
                    productJpaEntity.id, productJpaEntity.name,
                    reviewOwnerReplyJpaEntity.content, reviewOwnerReplyJpaEntity.createdAt)
                .orderBy(sortReviewLike.count().desc(), reviewJpaEntity.createdAt.desc());
            case OLDEST -> query.orderBy(reviewJpaEntity.createdAt.asc());
            case LATEST -> query.orderBy(reviewJpaEntity.createdAt.desc());
        }
    }

    private Map<Long, List<String>> findImageUrlsByReviewIds(List<Long> reviewIds) {
        List<Tuple> results = queryFactory
            .select(reviewImageJpaEntity.reviewId, uploadedFileJpaEntity.filePath)
            .from(reviewImageJpaEntity)
            .innerJoin(uploadedFileJpaEntity).on(reviewImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(reviewImageJpaEntity.reviewId.in(reviewIds))
            .orderBy(reviewImageJpaEntity.sort.asc())
            .fetch();

        return results.stream()
            .filter(tuple -> tuple.get(reviewImageJpaEntity.reviewId) != null)
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(reviewImageJpaEntity.reviewId)),
                Collectors.mapping(
                    tuple -> fileUrlResolver.resolve(Objects.toString(tuple.get(uploadedFileJpaEntity.filePath), "")),
                    Collectors.toList()
                )
            ));
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

    private Map<Long, String> findFirstImageUrlsByReviewIds(List<Long> reviewIds) {
        if (reviewIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> results = queryFactory
            .select(reviewImageJpaEntity.reviewId, uploadedFileJpaEntity.filePath)
            .from(reviewImageJpaEntity)
            .innerJoin(uploadedFileJpaEntity).on(reviewImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(
                reviewImageJpaEntity.reviewId.in(reviewIds),
                reviewImageJpaEntity.sort.eq(
                    JPAExpressions
                        .select(subReviewImage.sort.min())
                        .from(subReviewImage)
                        .where(subReviewImage.reviewId.eq(reviewImageJpaEntity.reviewId))
                )
            )
            .fetch();

        Map<Long, String> filePathByReviewId = results.stream()
            .filter(tuple -> tuple.get(reviewImageJpaEntity.reviewId) != null && tuple.get(uploadedFileJpaEntity.filePath) != null)
            .collect(Collectors.toMap(
                tuple -> Objects.requireNonNull(tuple.get(reviewImageJpaEntity.reviewId)),
                tuple -> Objects.requireNonNull(tuple.get(uploadedFileJpaEntity.filePath)),
                (existing, replacement) -> existing
            ));

        return fileUrlResolver.resolveAll(filePathByReviewId);
    }

    private SearchReviewItemResult withResolvedImageUrl(SearchReviewItemResult row) {
        return new SearchReviewItemResult(
            row.id(),
            fileUrlResolver.resolve(row.imageUrl())
        );
    }

    private BestReviewListItemResult withResolvedImageUrl(BestReviewListItemResult row) {
        return new BestReviewListItemResult(
            row.id(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.stationName(),
            row.shopName(),
            row.productName(),
            row.totalRating(),
            row.content()
        );
    }

    private LatestReviewListItemResult withResolvedImageUrl(LatestReviewListItemResult row) {
        return new LatestReviewListItemResult(
            row.id(),
            row.imageUrls(),
            row.stationName(),
            row.totalRating(),
            row.content(),
            row.memberId(),
            row.memberNickname(),
            fileUrlResolver.resolve(row.memberProfileImageUrl()),
            row.createdAt(),
            row.productId(),
            row.productName(),
            row.likeCount(),
            row.commentCount(),
            row.ownerReplyContent(),
            row.ownerReplyCreatedAt()
        );
    }

    private ReviewDetailResult withResolvedImageUrl(ReviewDetailResult row) {
        return new ReviewDetailResult(
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
            row.memberId(),
            row.memberNickname(),
            fileUrlResolver.resolve(row.memberProfileImageUrl()),
            row.createdAt(),
            row.ownerOnly(),
            row.imageUrls(),
            row.tagNames(),
            row.ownerReplyContent(),
            row.ownerReplyCreatedAt(),
            row.orderMethod(),
            row.deliveryRating(),
            row.deliveryComment()
        );
    }

    private ReviewCommentItemResult withResolvedImageUrl(ReviewCommentItemResult row) {
        return new ReviewCommentItemResult(
            row.id(),
            row.reviewId(),
            row.memberId(),
            row.memberNickname(),
            fileUrlResolver.resolve(row.memberProfileImageUrl()),
            row.content(),
            row.createdAt()
        );
    }

    private ReviewReplyItemResult withResolvedImageUrl(ReviewReplyItemResult row) {
        return new ReviewReplyItemResult(
            row.id(),
            row.commentId(),
            row.memberId(),
            row.memberNickname(),
            fileUrlResolver.resolve(row.memberProfileImageUrl()),
            row.replyToMemberId(),
            row.replyToMemberNickname(),
            row.content(),
            row.createdAt()
        );
    }

    private NumberPath<Long> shopStationId() {
        return Expressions.numberPath(Long.class, shopJpaEntity, "stationId");
    }

    private NumberPath<Long> memberProfileImageFileId() {
        return Expressions.numberPath(Long.class, memberJpaEntity, "profileImageFileId");
    }
}
