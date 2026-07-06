package com.tastyhouse.core.domain.review.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.rank.application.dto.result.MemberReviewCountResult;
import com.tastyhouse.core.domain.rank.application.dto.result.QMemberReviewCountResult;
import com.tastyhouse.core.domain.review.application.dto.result.BestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.LatestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.MyReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.QBestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.QLatestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.QReviewDetailResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewDetailResult;
import com.tastyhouse.core.domain.review.application.dto.result.SearchReviewItemResult;
import com.tastyhouse.core.domain.review.domain.model.QReviewComment;
import com.tastyhouse.core.domain.review.domain.model.QReviewImage;
import com.tastyhouse.core.domain.review.domain.model.QReviewLike;
import com.tastyhouse.core.domain.review.domain.model.Review;
import com.tastyhouse.core.domain.review.domain.repository.ReviewRepository;
import com.tastyhouse.core.domain.review.domain.vo.ReviewId;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.domain.member.domain.model.QMember.member;
import static com.tastyhouse.core.domain.order.domain.model.QOrderProduct.orderProduct;
import static com.tastyhouse.core.domain.product.domain.model.QProduct.product;
import static com.tastyhouse.core.domain.review.domain.model.QReview.review;
import static com.tastyhouse.core.domain.review.domain.model.QReviewImage.reviewImage;
import static com.tastyhouse.core.domain.shop.domain.model.QShop.shop;
import static com.tastyhouse.core.domain.shop.domain.model.QStation.station;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private static final QReviewImage subReviewImage = new QReviewImage("subReviewImage");
    private static final QReviewLike subReviewLike = new QReviewLike("subReviewLike");
    private static final QReviewComment subReviewComment = new QReviewComment("subReviewComment");
    private static final QReviewLike sortReviewLike = new QReviewLike("sortReviewLike");

    private final JPAQueryFactory queryFactory;
    private final ReviewJpaRepository reviewJpaRepository;

    @Override
    public PageResult<BestReviewListItemResult> findBestReviews(PageQuery pageQuery) {
        JPAQuery<BestReviewListItemResult> query = queryFactory
            .select(new QBestReviewListItemResult(
                review.id,
                uploadedFile.filePath,
                station.stationName,
                shop.name,
                orderProduct.name,
                review.totalRating,
                review.content
            ))
            .from(review)
            .innerJoin(shop).on(review.shopId.eq(shop.id))
            .innerJoin(station).on(shop.stationId.eq(station.id))
            .leftJoin(orderProduct).on(
                orderProduct.orderId.eq(review.orderId)
                .and(orderProduct.productId.eq(review.productId))
            )
            .leftJoin(reviewImage).on(
                reviewImage.reviewId.eq(review.id)
                .and(reviewImage.sort.eq(
                    JPAExpressions
                        .select(subReviewImage.sort.min())
                        .from(subReviewImage)
                        .where(subReviewImage.reviewId.eq(review.id))
                ))
            )
            .leftJoin(uploadedFile).on(reviewImage.imageFileId.eq(uploadedFile.id))
            .where(review.hidden.eq(false))
            .orderBy(review.totalRating.desc(), review.createdAt.desc());

        long total = query.fetch().size();

        List<BestReviewListItemResult> reviews = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(reviews, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<LatestReviewListItemResult> findLatestReviews(PageQuery pageQuery) {
        JPAQuery<LatestReviewListItemResult> query = queryFactory
            .select(new QLatestReviewListItemResult(
                review.id,
                station.stationName,
                review.totalRating,
                review.content,
                member.id,
                member.nickname,
                uploadedFile.filePath,
                review.createdAt,
                product.id,
                product.name,
                JPAExpressions.select(subReviewLike.count())
                    .from(subReviewLike)
                    .where(subReviewLike.reviewId.eq(review.id)),
                JPAExpressions.select(subReviewComment.count())
                    .from(subReviewComment)
                    .where(subReviewComment.reviewId.eq(review.id)
                    .and(subReviewComment.hidden.eq(false)))
            ))
            .from(review)
            .innerJoin(shop).on(review.shopId.eq(shop.id))
            .innerJoin(station).on(shop.stationId.eq(station.id))
            .innerJoin(member).on(review.memberId.eq(member.id))
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
            .leftJoin(product).on(review.productId.eq(product.id))
            .where(review.hidden.eq(false))
            .orderBy(review.createdAt.desc());

        long total = query.fetch().size();

        List<LatestReviewListItemResult> reviews = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        if (!reviews.isEmpty()) {
            List<Long> reviewIds = reviews.stream().map(LatestReviewListItemResult::id).toList();
            Map<Long, List<String>> imageUrlsMap = findImageUrlsByReviewIds(reviewIds);
            reviews = reviews.stream()
                .map(r -> r.withImageUrls(imageUrlsMap.getOrDefault(r.id(), List.of())))
                .collect(Collectors.toList());
        }

        return PageResult.of(reviews, total, pageQuery.page(), pageQuery.size());
    }

    private Map<Long, List<String>> findImageUrlsByReviewIds(List<Long> reviewIds) {
        List<Tuple> results = queryFactory
            .select(reviewImage.reviewId, uploadedFile.filePath)
            .from(reviewImage)
            .innerJoin(uploadedFile).on(reviewImage.imageFileId.eq(uploadedFile.id))
            .where(reviewImage.reviewId.in(reviewIds))
            .orderBy(reviewImage.sort.asc())
            .fetch();

        return results.stream()
            .filter(tuple -> tuple.get(reviewImage.reviewId) != null)
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(reviewImage.reviewId)),
                Collectors.mapping(
                    tuple -> Objects.toString(tuple.get(uploadedFile.filePath), ""),
                    Collectors.toList()
                )
            ));
    }

    @Override
    public PageResult<LatestReviewListItemResult> findLatestReviewsByFollowing(List<Long> followingMemberIds, PageQuery pageQuery) {
        JPAQuery<LatestReviewListItemResult> query = queryFactory
            .select(new QLatestReviewListItemResult(
                review.id,
                station.stationName,
                review.totalRating,
                review.content,
                member.id,
                member.nickname,
                uploadedFile.filePath,
                review.createdAt,
                product.id,
                product.name,
                JPAExpressions.select(subReviewLike.count())
                    .from(subReviewLike)
                    .where(subReviewLike.reviewId.eq(review.id)),
                JPAExpressions.select(subReviewComment.count())
                    .from(subReviewComment)
                    .where(subReviewComment.reviewId.eq(review.id)
                        .and(subReviewComment.hidden.eq(false)))
            ))
            .from(review)
            .innerJoin(shop).on(review.shopId.eq(shop.id))
            .innerJoin(station).on(shop.stationId.eq(station.id))
            .innerJoin(member).on(review.memberId.eq(member.id))
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
            .leftJoin(product).on(review.productId.eq(product.id))
            .where(
                review.memberId.in(followingMemberIds),
                review.hidden.eq(false)
            )
            .orderBy(review.createdAt.desc());

        long total = query.fetch().size();

        List<LatestReviewListItemResult> reviews = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        if (!reviews.isEmpty()) {
            List<Long> reviewIds = reviews.stream().map(LatestReviewListItemResult::id).toList();
            Map<Long, List<String>> imageUrlsMap = findImageUrlsByReviewIds(reviewIds);
            reviews = reviews.stream()
                .map(r -> r.withImageUrls(imageUrlsMap.getOrDefault(r.id(), List.of())))
                .collect(Collectors.toList());
        }

        return PageResult.of(reviews, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<LatestReviewListItemResult> findLatestReviewsByShopId(Long shopId, Integer rating, PageQuery pageQuery, Boolean hasImage, String sortType) {
        var whereClause = review.shopId.eq(shopId).and(review.hidden.eq(false));
        if (rating != null) {
            if (rating == 5) {
                whereClause = whereClause.and(review.totalRating.eq(5.0));
            } else {
                whereClause = whereClause.and(
                    review.totalRating.goe(rating.doubleValue())
                        .and(review.totalRating.lt(rating.doubleValue() + 1.0))
                );
            }
        }

        if (hasImage != null) {
            if (hasImage) {
                whereClause = whereClause.and(
                    JPAExpressions
                        .selectOne()
                        .from(subReviewImage)
                        .where(subReviewImage.reviewId.eq(review.id))
                        .exists()
                );
            } else {
                whereClause = whereClause.and(
                    JPAExpressions
                        .selectOne()
                        .from(subReviewImage)
                        .where(subReviewImage.reviewId.eq(review.id))
                        .notExists()
                );
            }
        }

        JPAQuery<LatestReviewListItemResult> query = queryFactory
            .select(new QLatestReviewListItemResult(
                review.id,
                station.stationName,
                review.totalRating,
                review.content,
                member.id,
                member.nickname,
                uploadedFile.filePath,
                review.createdAt,
                product.id,
                product.name,
                JPAExpressions.select(subReviewLike.count())
                    .from(subReviewLike)
                    .where(subReviewLike.reviewId.eq(review.id)),
                JPAExpressions.select(subReviewComment.count())
                    .from(subReviewComment)
                    .where(subReviewComment.reviewId.eq(review.id)
                    .and(subReviewComment.hidden.eq(false)))
            ))
            .from(review)
            .innerJoin(shop).on(review.shopId.eq(shop.id))
            .innerJoin(station).on(shop.stationId.eq(station.id))
            .innerJoin(member).on(review.memberId.eq(member.id))
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
            .leftJoin(product).on(review.productId.eq(product.id))
            .where(whereClause);

        if ("RECOMMENDED".equals(sortType)) {
            query.leftJoin(sortReviewLike).on(sortReviewLike.reviewId.eq(review.id))
                .groupBy(review.id, station.stationName, review.totalRating, review.content,
                    member.id, member.nickname, uploadedFile.filePath, review.createdAt,
                    product.id, product.name)
                .orderBy(sortReviewLike.count().desc(), review.createdAt.desc());
        } else if ("OLDEST".equals(sortType)) {
            query.orderBy(review.createdAt.asc());
        } else {
            query.orderBy(review.createdAt.desc());
        }

        long total = query.fetch().size();

        List<LatestReviewListItemResult> reviews = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        if (!reviews.isEmpty()) {
            List<Long> reviewIds = reviews.stream().map(LatestReviewListItemResult::id).toList();
            Map<Long, List<String>> imageUrlsMap = findImageUrlsByReviewIds(reviewIds);
            reviews = reviews.stream()
                .map(r -> r.withImageUrls(imageUrlsMap.getOrDefault(r.id(), List.of())))
                .collect(Collectors.toList());
        }

        return PageResult.of(reviews, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public List<MemberReviewCountResult> countReviewsByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return queryFactory
            .select(new QMemberReviewCountResult(
                review.memberId,
                review.count(),
                review.createdAt.max()
            ))
            .from(review)
            .where(
                review.createdAt.goe(startDate),
                review.createdAt.lt(endDate)
            )
            .groupBy(review.memberId)
            .orderBy(
                review.count().desc(),
                review.createdAt.max().asc(),
                review.memberId.asc()
            )
            .fetch();
    }

    @Override
    public Optional<ReviewDetailResult> findReviewDetail(ReviewId reviewId) {
        ReviewDetailResult result = queryFactory
            .select(new QReviewDetailResult(
                review.id,
                shop.id,
                shop.name,
                station.stationName,
                review.content,
                review.totalRating,
                review.tasteRating,
                review.amountRating,
                review.priceRating,
                review.atmosphereRating,
                review.kindnessRating,
                review.hygieneRating,
                review.willRevisit,
                member.id,
                member.nickname,
                uploadedFile.filePath,
                review.createdAt
            ))
            .from(review)
            .innerJoin(shop).on(review.shopId.eq(shop.id))
            .innerJoin(station).on(shop.stationId.eq(station.id))
            .innerJoin(member).on(review.memberId.eq(member.id))
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
            .where(
                review.id.eq(reviewId.value()),
                review.hidden.eq(false)
            )
            .fetchOne();

        if (result != null) {
            List<String> imageUrls = findImageUrlsByReviewId(reviewId.value());
            result = result.withImageUrls(imageUrls);
        }

        return Optional.ofNullable(result);
    }

    @Override
    public List<LatestReviewListItemResult> findReviewsByShopIdAndRating(Long shopId, Integer rating, int limit) {
        var whereClause = review.shopId.eq(shopId).and(review.hidden.eq(false));

        if (rating == 5) {
            whereClause = whereClause.and(review.totalRating.eq(5.0));
        } else {
            whereClause = whereClause.and(
                review.totalRating.goe(rating.doubleValue())
                    .and(review.totalRating.lt(rating.doubleValue() + 1.0))
            );
        }

        List<LatestReviewListItemResult> reviews = queryFactory
            .select(new QLatestReviewListItemResult(
                review.id,
                station.stationName,
                review.totalRating,
                review.content,
                member.id,
                member.nickname,
                uploadedFile.filePath,
                review.createdAt,
                product.id,
                product.name,
                JPAExpressions.select(subReviewLike.count())
                    .from(subReviewLike)
                    .where(subReviewLike.reviewId.eq(review.id)),
                JPAExpressions.select(subReviewComment.count())
                    .from(subReviewComment)
                    .where(subReviewComment.reviewId.eq(review.id)
                        .and(subReviewComment.hidden.eq(false)))
            ))
            .from(review)
            .innerJoin(shop).on(review.shopId.eq(shop.id))
            .innerJoin(station).on(shop.stationId.eq(station.id))
            .innerJoin(member).on(review.memberId.eq(member.id))
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
            .leftJoin(product).on(review.productId.eq(product.id))
            .where(whereClause)
            .orderBy(review.createdAt.desc())
            .limit(limit)
            .fetch();

        if (!reviews.isEmpty()) {
            List<Long> reviewIds = reviews.stream().map(LatestReviewListItemResult::id).toList();
            Map<Long, List<String>> imageUrlsMap = findImageUrlsByReviewIds(reviewIds);
            reviews = reviews.stream()
                .map(r -> r.withImageUrls(imageUrlsMap.getOrDefault(r.id(), List.of())))
                .collect(Collectors.toList());
        }

        return reviews;
    }

    @Override
    public PageResult<LatestReviewListItemResult> findLatestReviewsByProductId(Long productId, Integer rating, PageQuery pageQuery, Boolean hasImage, String sortType) {
        var whereClause = review.productId.eq(productId).and(review.hidden.eq(false));
        if (rating != null) {
            if (rating == 5) {
                whereClause = whereClause.and(review.totalRating.eq(5.0));
            } else {
                whereClause = whereClause.and(
                    review.totalRating.goe(rating.doubleValue())
                        .and(review.totalRating.lt(rating.doubleValue() + 1.0))
                );
            }
        }

        if (hasImage != null) {
            if (hasImage) {
                whereClause = whereClause.and(
                    JPAExpressions
                        .selectOne()
                        .from(subReviewImage)
                        .where(subReviewImage.reviewId.eq(review.id))
                        .exists()
                );
            } else {
                whereClause = whereClause.and(
                    JPAExpressions
                        .selectOne()
                        .from(subReviewImage)
                        .where(subReviewImage.reviewId.eq(review.id))
                        .notExists()
                );
            }
        }

        JPAQuery<LatestReviewListItemResult> query = queryFactory
            .select(new QLatestReviewListItemResult(
                review.id,
                station.stationName,
                review.totalRating,
                review.content,
                member.id,
                member.nickname,
                uploadedFile.filePath,
                review.createdAt,
                product.id,
                product.name,
                JPAExpressions.select(subReviewLike.count())
                    .from(subReviewLike)
                    .where(subReviewLike.reviewId.eq(review.id)),
                JPAExpressions.select(subReviewComment.count())
                    .from(subReviewComment)
                    .where(subReviewComment.reviewId.eq(review.id)
                        .and(subReviewComment.hidden.eq(false)))
            ))
            .from(review)
            .innerJoin(shop).on(review.shopId.eq(shop.id))
            .innerJoin(station).on(shop.stationId.eq(station.id))
            .innerJoin(member).on(review.memberId.eq(member.id))
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
            .leftJoin(product).on(review.productId.eq(product.id))
            .where(whereClause);

        if ("RECOMMENDED".equals(sortType)) {
            query.leftJoin(sortReviewLike).on(sortReviewLike.reviewId.eq(review.id))
                .groupBy(review.id, station.stationName, review.totalRating, review.content,
                    member.id, member.nickname, uploadedFile.filePath, review.createdAt,
                    product.id, product.name)
                .orderBy(sortReviewLike.count().desc(), review.createdAt.desc());
        } else if ("OLDEST".equals(sortType)) {
            query.orderBy(review.createdAt.asc());
        } else {
            query.orderBy(review.createdAt.desc());
        }

        long total = query.fetch().size();

        List<LatestReviewListItemResult> reviews = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        if (!reviews.isEmpty()) {
            List<Long> reviewIds = reviews.stream().map(LatestReviewListItemResult::id).toList();
            Map<Long, List<String>> imageUrlsMap = findImageUrlsByReviewIds(reviewIds);
            reviews = reviews.stream()
                .map(r -> r.withImageUrls(imageUrlsMap.getOrDefault(r.id(), List.of())))
                .collect(Collectors.toList());
        }

        return PageResult.of(reviews, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public List<LatestReviewListItemResult> findReviewsByProductIdAndRating(Long productId, Integer rating, int limit) {
        var whereClause = review.productId.eq(productId).and(review.hidden.eq(false));

        if (rating == 5) {
            whereClause = whereClause.and(review.totalRating.eq(5.0));
        } else {
            whereClause = whereClause.and(
                review.totalRating.goe(rating.doubleValue())
                    .and(review.totalRating.lt(rating.doubleValue() + 1.0))
            );
        }

        List<LatestReviewListItemResult> reviews = queryFactory
            .select(new QLatestReviewListItemResult(
                review.id,
                station.stationName,
                review.totalRating,
                review.content,
                member.id,
                member.nickname,
                uploadedFile.filePath,
                review.createdAt,
                product.id,
                product.name,
                JPAExpressions.select(subReviewLike.count())
                    .from(subReviewLike)
                    .where(subReviewLike.reviewId.eq(review.id)),
                JPAExpressions.select(subReviewComment.count())
                    .from(subReviewComment)
                    .where(subReviewComment.reviewId.eq(review.id)
                        .and(subReviewComment.hidden.eq(false)))
            ))
            .from(review)
            .innerJoin(shop).on(review.shopId.eq(shop.id))
            .innerJoin(station).on(shop.stationId.eq(station.id))
            .innerJoin(member).on(review.memberId.eq(member.id))
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
            .leftJoin(product).on(review.productId.eq(product.id))
            .where(whereClause)
            .orderBy(review.createdAt.desc())
            .limit(limit)
            .fetch();

        if (!reviews.isEmpty()) {
            List<Long> reviewIds = reviews.stream().map(LatestReviewListItemResult::id).toList();
            Map<Long, List<String>> imageUrlsMap = findImageUrlsByReviewIds(reviewIds);
            reviews = reviews.stream()
                .map(r -> r.withImageUrls(imageUrlsMap.getOrDefault(r.id(), List.of())))
                .collect(Collectors.toList());
        }

        return reviews;
    }

    private List<String> findImageUrlsByReviewId(Long reviewId) {
        return queryFactory
            .select(uploadedFile.filePath)
            .from(reviewImage)
            .innerJoin(uploadedFile).on(reviewImage.imageFileId.eq(uploadedFile.id))
            .where(reviewImage.reviewId.eq(reviewId))
            .orderBy(reviewImage.sort.asc())
            .fetch();
    }

    @Override
    public PageResult<MyReviewListItemResult> findMyReviews(Long memberId, PageQuery pageQuery) {
        List<Long> allReviewIds = queryFactory
            .select(review.id)
            .from(review)
            .where(
                review.memberId.eq(memberId),
                review.hidden.eq(false)
            )
            .orderBy(review.createdAt.desc())
            .fetch();

        long total = allReviewIds.size();

        List<Long> pagedReviewIds = queryFactory
            .select(review.id)
            .from(review)
            .where(
                review.memberId.eq(memberId),
                review.hidden.eq(false)
            )
            .orderBy(review.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        Map<Long, String> imageUrlMap = findFirstImageUrlsByReviewIds(pagedReviewIds);

        List<MyReviewListItemResult> reviews = pagedReviewIds.stream()
            .map(reviewId -> new MyReviewListItemResult(reviewId, imageUrlMap.get(reviewId)))
            .collect(Collectors.toList());

        return PageResult.of(reviews, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<MyReviewListItemResult> findReviewsByMemberId(Long memberId, PageQuery pageQuery) {
        List<Long> allReviewIds = queryFactory
            .select(review.id)
            .from(review)
            .where(
                review.memberId.eq(memberId),
                review.hidden.eq(false)
            )
            .orderBy(review.createdAt.desc())
            .fetch();

        long total = allReviewIds.size();

        List<Long> pagedReviewIds = queryFactory
            .select(review.id)
            .from(review)
            .where(
                review.memberId.eq(memberId),
                review.hidden.eq(false)
            )
            .orderBy(review.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        Map<Long, String> imageUrlMap = findFirstImageUrlsByReviewIds(pagedReviewIds);

        List<MyReviewListItemResult> reviews = pagedReviewIds.stream()
            .map(reviewId -> new MyReviewListItemResult(reviewId, imageUrlMap.get(reviewId)))
            .collect(Collectors.toList());

        return PageResult.of(reviews, total, pageQuery.page(), pageQuery.size());
    }

    private Map<Long, String> findFirstImageUrlsByReviewIds(List<Long> reviewIds) {
        if (reviewIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> results = queryFactory
            .select(reviewImage.reviewId, uploadedFile.filePath)
            .from(reviewImage)
            .innerJoin(uploadedFile).on(reviewImage.imageFileId.eq(uploadedFile.id))
            .where(
                reviewImage.reviewId.in(reviewIds),
                reviewImage.sort.eq(
                    JPAExpressions
                        .select(subReviewImage.sort.min())
                        .from(subReviewImage)
                        .where(subReviewImage.reviewId.eq(reviewImage.reviewId))
                )
            )
            .fetch();

        return results.stream()
            .filter(tuple -> tuple.get(reviewImage.reviewId) != null && tuple.get(uploadedFile.filePath) != null)
            .collect(Collectors.toMap(
                tuple -> Objects.requireNonNull(tuple.get(reviewImage.reviewId)),
                tuple -> Objects.requireNonNull(tuple.get(uploadedFile.filePath)),
                (existing, replacement) -> existing
            ));
    }

    @Override
    public Long countByShopIdAndHiddenFalse(Long shopId) {
        return queryFactory
            .select(review.count())
            .from(review)
            .where(review.shopId.eq(shopId), review.hidden.eq(false))
            .fetchOne();
    }

    @Override
    public Long countWillRevisit(Long shopId) {
        return queryFactory
            .select(review.count())
            .from(review)
            .where(
                review.shopId.eq(shopId),
                review.hidden.eq(false),
                review.willRevisit.eq(true)
            )
            .fetchOne();
    }

    @Override
    public Double getAverageTasteRating(Long shopId) {
        return queryFactory
            .select(review.tasteRating.avg())
            .from(review)
            .where(review.shopId.eq(shopId), review.hidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAverageAmountRating(Long shopId) {
        return queryFactory
            .select(review.amountRating.avg())
            .from(review)
            .where(review.shopId.eq(shopId), review.hidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAveragePriceRating(Long shopId) {
        return queryFactory
            .select(review.priceRating.avg())
            .from(review)
            .where(review.shopId.eq(shopId), review.hidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAverageAtmosphereRating(Long shopId) {
        return queryFactory
            .select(review.atmosphereRating.avg())
            .from(review)
            .where(review.shopId.eq(shopId), review.hidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAverageKindnessRating(Long shopId) {
        return queryFactory
            .select(review.kindnessRating.avg())
            .from(review)
            .where(review.shopId.eq(shopId), review.hidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAverageHygieneRating(Long shopId) {
        return queryFactory
            .select(review.hygieneRating.avg())
            .from(review)
            .where(review.shopId.eq(shopId), review.hidden.eq(false))
            .fetchOne();
    }

    @Override
    public Map<Integer, Long> getRatingCounts(Long shopId) {
        List<Tuple> results = queryFactory
            .select(review.totalRating.floor().intValue(), review.count())
            .from(review)
            .where(review.shopId.eq(shopId), review.hidden.eq(false))
            .groupBy(review.totalRating.floor().intValue())
            .fetch();

        Map<Integer, Long> ratingMap = new HashMap<>();
        for (Tuple row : results) {
            ratingMap.put(row.get(0, Integer.class), row.get(1, Long.class));
        }
        return ratingMap;
    }

    @Override
    public Map<Integer, Long> getMonthlyReviewCounts(Long shopId, int year) {
        List<Tuple> results = queryFactory
            .select(review.createdAt.month(), review.count())
            .from(review)
            .where(
                review.shopId.eq(shopId),
                review.hidden.eq(false),
                review.createdAt.year().eq(year)
            )
            .groupBy(review.createdAt.month())
            .fetch();

        Map<Integer, Long> monthlyMap = new HashMap<>();
        for (Tuple row : results) {
            monthlyMap.put(row.get(0, Integer.class), row.get(1, Long.class));
        }
        return monthlyMap;
    }

    @Override
    public Long countByProductIdAndHiddenFalse(Long productId) {
        return queryFactory
            .select(review.count())
            .from(review)
            .where(review.productId.eq(productId), review.hidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAverageTasteRatingByProductId(Long productId) {
        return queryFactory
            .select(review.tasteRating.avg())
            .from(review)
            .where(review.productId.eq(productId), review.hidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAverageAmountRatingByProductId(Long productId) {
        return queryFactory
            .select(review.amountRating.avg())
            .from(review)
            .where(review.productId.eq(productId), review.hidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAveragePriceRatingByProductId(Long productId) {
        return queryFactory
            .select(review.priceRating.avg())
            .from(review)
            .where(review.productId.eq(productId), review.hidden.eq(false))
            .fetchOne();
    }

    @Override
    public Optional<Review> findByIdAndMemberId(ReviewId reviewId, Long memberId) {
        Review result = queryFactory
            .selectFrom(review)
            .where(
                review.id.eq(reviewId.value()),
                review.memberId.eq(memberId)
            )
            .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public long countVisibleReviewsByMemberId(Long memberId) {
        Long count = queryFactory
            .select(review.count())
            .from(review)
            .where(review.memberId.eq(memberId), review.hidden.eq(false))
            .fetchOne();
        return count != null ? count : 0L;
    }

    @Override
    public boolean existsByOrderIdAndProductIdAndMemberId(Long orderId, Long productId, Long memberId) {
        return queryFactory
            .selectOne()
            .from(review)
            .where(
                review.orderId.eq(orderId),
                review.productId.eq(productId),
                review.memberId.eq(memberId)
            )
            .fetchFirst() != null;
    }

    @Override
    public Optional<Review> findById(ReviewId reviewId) {
        return reviewJpaRepository.findById(reviewId.value());
    }

    @Override
    public Review save(Review review) {
        return reviewJpaRepository.save(review);
    }

    @Override
    public void deleteById(ReviewId reviewId) {
        reviewJpaRepository.deleteById(reviewId.value());
    }

    @Override
    public PageResult<SearchReviewItemResult> searchByKeyword(String keyword, PageQuery pageQuery) {
        Long total = queryFactory
            .select(review.countDistinct())
            .from(review)
            .innerJoin(reviewImage).on(reviewImage.reviewId.eq(review.id))
            .where(
                review.content.containsIgnoreCase(keyword)
                .and(review.hidden.eq(false))
            )
            .fetchOne();

        if (total == null || total == 0) return PageResult.empty(pageQuery.page(), pageQuery.size());

        List<SearchReviewItemResult> content = queryFactory
            .select(Projections.constructor(SearchReviewItemResult.class,
                review.id,
                uploadedFile.filePath
            ))
            .from(review)
            .innerJoin(reviewImage).on(
                reviewImage.reviewId.eq(review.id)
                .and(reviewImage.sort.eq(
                    JPAExpressions.select(subReviewImage.sort.min())
                        .from(subReviewImage)
                        .where(subReviewImage.reviewId.eq(review.id))
                ))
            )
            .innerJoin(uploadedFile).on(reviewImage.imageFileId.eq(uploadedFile.id))
            .where(
                review.content.containsIgnoreCase(keyword)
                .and(review.hidden.eq(false))
            )
            .orderBy(review.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }
}
