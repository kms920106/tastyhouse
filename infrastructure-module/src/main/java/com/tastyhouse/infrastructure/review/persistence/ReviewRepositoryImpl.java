package com.tastyhouse.infrastructure.review.persistence;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.review.domain.model.Review;
import com.tastyhouse.core.domain.review.domain.repository.ReviewRepository;
import com.tastyhouse.core.domain.review.domain.vo.ReviewId;
import com.tastyhouse.core.domain.rank.application.dto.result.MemberReviewCountResult;
import com.tastyhouse.core.domain.rank.application.dto.result.QMemberReviewCountResult;
import com.tastyhouse.core.domain.review.application.dto.ReviewSearchCondition;
import com.tastyhouse.core.domain.review.application.dto.result.BestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.LatestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.MyReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.QBestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.QLatestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.QReviewDetailResult;
import com.tastyhouse.core.domain.review.application.dto.result.QReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.QReviewManagementDetailResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewDetailResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewManagementDetailResult;
import com.tastyhouse.core.domain.review.application.dto.result.SearchReviewItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.infrastructure.order.persistence.QOrderProductJpaEntity.orderProductJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductJpaEntity.productJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewImageJpaEntity.reviewImageJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewJpaEntity.reviewJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QStationJpaEntity.stationJpaEntity;

/**
 * {@code member}·{@code shop}은 infrastructure-module로 이동한 {@code MemberJpaEntity}·
 * {@code ShopJpaEntity}를 가리킨다. core-module은 infrastructure-module을 의존할 수 없어
 * (의존 방향: infrastructure → core) 생성된 Q타입을 import할 수 없으므로, {@link PathBuilder}로
 * JPA 엔티티명("MemberJpaEntity"/"ShopJpaEntity")을 문자열 참조해 필요한 컬럼만 타입 세이프하게 노출한다.
 */
@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private static final QReviewImageJpaEntity subReviewImage = new QReviewImageJpaEntity("subReviewImage");
    private static final QReviewLikeJpaEntity subReviewLike = new QReviewLikeJpaEntity("subReviewLike");
    private static final QReviewCommentJpaEntity subReviewComment = new QReviewCommentJpaEntity("subReviewComment");
    private static final QReviewLikeJpaEntity sortReviewLike = new QReviewLikeJpaEntity("sortReviewLike");

    private static final PathBuilder<Object> member = new PathBuilder<>(Object.class, "MemberJpaEntity");
    private static final NumberPath<Long> memberIdCol = member.getNumber("id", Long.class);
    private static final StringPath memberNicknameCol = member.getString("nickname");
    private static final NumberPath<Long> memberProfileImageFileIdCol = member.getNumber("profileImageFileId", Long.class);

    private static final PathBuilder<Object> shop = new PathBuilder<>(Object.class, "ShopJpaEntity");
    private static final NumberPath<Long> shopIdCol = shop.getNumber("id", Long.class);
    private static final StringPath shopNameCol = shop.getString("name");
    private static final NumberPath<Long> shopStationIdCol = shop.getNumber("stationId", Long.class);

    private final JPAQueryFactory queryFactory;
    private final ReviewJpaRepository reviewJpaRepository;

    @Override
    public PageResult<BestReviewListItemResult> findBestReviews(PageQuery pageQuery) {
        JPAQuery<BestReviewListItemResult> query = queryFactory
            .select(new QBestReviewListItemResult(
                reviewJpaEntity.id,
                uploadedFile.filePath,
                stationJpaEntity.stationName,
                shopNameCol,
                orderProductJpaEntity.name,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content
            ))
            .from(reviewJpaEntity)
            .innerJoin(shop).on(reviewJpaEntity.shopId.eq(shopIdCol))
            .innerJoin(stationJpaEntity).on(shopStationIdCol.eq(stationJpaEntity.id))
            .leftJoin(orderProductJpaEntity).on(
                orderProductJpaEntity.orderId.eq(reviewJpaEntity.orderId)
                .and(orderProductJpaEntity.productId.eq(reviewJpaEntity.productId))
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
            .leftJoin(uploadedFile).on(reviewImageJpaEntity.imageFileId.eq(uploadedFile.id))
            .where(reviewJpaEntity.hidden.eq(false))
            .orderBy(reviewJpaEntity.totalRating.desc(), reviewJpaEntity.createdAt.desc());

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
                reviewJpaEntity.id,
                stationJpaEntity.stationName,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content,
                reviewJpaEntity.memberId,
                memberNicknameCol,
                uploadedFile.filePath,
                reviewJpaEntity.createdAt,
                productJpaEntity.id,
                productJpaEntity.name,
                JPAExpressions.select(subReviewLike.count())
                    .from(subReviewLike)
                    .where(subReviewLike.reviewId.eq(reviewJpaEntity.id)),
                JPAExpressions.select(subReviewComment.count())
                    .from(subReviewComment)
                    .where(subReviewComment.reviewId.eq(reviewJpaEntity.id)
                    .and(subReviewComment.hidden.eq(false)))
            ))
            .from(reviewJpaEntity)
            .innerJoin(shop).on(reviewJpaEntity.shopId.eq(shopIdCol))
            .innerJoin(stationJpaEntity).on(shopStationIdCol.eq(stationJpaEntity.id))
            .innerJoin(member).on(Expressions.numberPath(Long.class, reviewJpaEntity, "memberId").eq(memberIdCol))
            .leftJoin(uploadedFile).on(memberProfileImageFileIdCol.eq(uploadedFile.id))
            .leftJoin(productJpaEntity).on(reviewJpaEntity.productId.eq(productJpaEntity.id))
            .where(reviewJpaEntity.hidden.eq(false))
            .orderBy(reviewJpaEntity.createdAt.desc());

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
            .select(reviewImageJpaEntity.reviewId, uploadedFile.filePath)
            .from(reviewImageJpaEntity)
            .innerJoin(uploadedFile).on(reviewImageJpaEntity.imageFileId.eq(uploadedFile.id))
            .where(reviewImageJpaEntity.reviewId.in(reviewIds))
            .orderBy(reviewImageJpaEntity.sort.asc())
            .fetch();

        return results.stream()
            .filter(tuple -> tuple.get(reviewImageJpaEntity.reviewId) != null)
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(reviewImageJpaEntity.reviewId)),
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
                reviewJpaEntity.id,
                stationJpaEntity.stationName,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content,
                reviewJpaEntity.memberId,
                memberNicknameCol,
                uploadedFile.filePath,
                reviewJpaEntity.createdAt,
                productJpaEntity.id,
                productJpaEntity.name,
                JPAExpressions.select(subReviewLike.count())
                    .from(subReviewLike)
                    .where(subReviewLike.reviewId.eq(reviewJpaEntity.id)),
                JPAExpressions.select(subReviewComment.count())
                    .from(subReviewComment)
                    .where(subReviewComment.reviewId.eq(reviewJpaEntity.id)
                        .and(subReviewComment.hidden.eq(false)))
            ))
            .from(reviewJpaEntity)
            .innerJoin(shop).on(reviewJpaEntity.shopId.eq(shopIdCol))
            .innerJoin(stationJpaEntity).on(shopStationIdCol.eq(stationJpaEntity.id))
            .innerJoin(member).on(Expressions.numberPath(Long.class, reviewJpaEntity, "memberId").eq(memberIdCol))
            .leftJoin(uploadedFile).on(memberProfileImageFileIdCol.eq(uploadedFile.id))
            .leftJoin(productJpaEntity).on(reviewJpaEntity.productId.eq(productJpaEntity.id))
            .where(
                Expressions.numberPath(Long.class, reviewJpaEntity, "memberId").in(followingMemberIds),
                reviewJpaEntity.hidden.eq(false)
            )
            .orderBy(reviewJpaEntity.createdAt.desc());

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
        var whereClause = reviewJpaEntity.shopId.eq(shopId).and(reviewJpaEntity.hidden.eq(false));
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
            .select(new QLatestReviewListItemResult(
                reviewJpaEntity.id,
                stationJpaEntity.stationName,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content,
                reviewJpaEntity.memberId,
                memberNicknameCol,
                uploadedFile.filePath,
                reviewJpaEntity.createdAt,
                productJpaEntity.id,
                productJpaEntity.name,
                JPAExpressions.select(subReviewLike.count())
                    .from(subReviewLike)
                    .where(subReviewLike.reviewId.eq(reviewJpaEntity.id)),
                JPAExpressions.select(subReviewComment.count())
                    .from(subReviewComment)
                    .where(subReviewComment.reviewId.eq(reviewJpaEntity.id)
                    .and(subReviewComment.hidden.eq(false)))
            ))
            .from(reviewJpaEntity)
            .innerJoin(shop).on(reviewJpaEntity.shopId.eq(shopIdCol))
            .innerJoin(stationJpaEntity).on(shopStationIdCol.eq(stationJpaEntity.id))
            .innerJoin(member).on(Expressions.numberPath(Long.class, reviewJpaEntity, "memberId").eq(memberIdCol))
            .leftJoin(uploadedFile).on(memberProfileImageFileIdCol.eq(uploadedFile.id))
            .leftJoin(productJpaEntity).on(reviewJpaEntity.productId.eq(productJpaEntity.id))
            .where(whereClause);

        if ("RECOMMENDED".equals(sortType)) {
            query.leftJoin(sortReviewLike).on(sortReviewLike.reviewId.eq(reviewJpaEntity.id))
                .groupBy(reviewJpaEntity.id, stationJpaEntity.stationName, reviewJpaEntity.totalRating, reviewJpaEntity.content,
                    memberIdCol, memberNicknameCol, uploadedFile.filePath, reviewJpaEntity.createdAt,
                    productJpaEntity.id, productJpaEntity.name)
                .orderBy(sortReviewLike.count().desc(), reviewJpaEntity.createdAt.desc());
        } else if ("OLDEST".equals(sortType)) {
            query.orderBy(reviewJpaEntity.createdAt.asc());
        } else {
            query.orderBy(reviewJpaEntity.createdAt.desc());
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
        var memberIdPath = Expressions.numberPath(Long.class, reviewJpaEntity, "memberId");

        return queryFactory
            .select(new QMemberReviewCountResult(
                reviewJpaEntity.memberId,
                reviewJpaEntity.count(),
                reviewJpaEntity.createdAt.max()
            ))
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.createdAt.goe(startDate),
                reviewJpaEntity.createdAt.lt(endDate)
            )
            .groupBy(memberIdPath)
            .orderBy(
                reviewJpaEntity.count().desc(),
                reviewJpaEntity.createdAt.max().asc(),
                memberIdPath.asc()
            )
            .fetch();
    }

    @Override
    public Optional<ReviewDetailResult> findReviewDetail(ReviewId reviewId) {
        ReviewDetailResult result = queryFactory
            .select(new QReviewDetailResult(
                reviewJpaEntity.id,
                shopIdCol,
                shopNameCol,
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
                memberNicknameCol,
                uploadedFile.filePath,
                reviewJpaEntity.createdAt
            ))
            .from(reviewJpaEntity)
            .innerJoin(shop).on(reviewJpaEntity.shopId.eq(shopIdCol))
            .innerJoin(stationJpaEntity).on(shopStationIdCol.eq(stationJpaEntity.id))
            .innerJoin(member).on(Expressions.numberPath(Long.class, reviewJpaEntity, "memberId").eq(memberIdCol))
            .leftJoin(uploadedFile).on(memberProfileImageFileIdCol.eq(uploadedFile.id))
            .where(
                reviewJpaEntity.id.eq(reviewId.value()),
                reviewJpaEntity.hidden.eq(false)
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
        var whereClause = reviewJpaEntity.shopId.eq(shopId).and(reviewJpaEntity.hidden.eq(false));

        if (rating == 5) {
            whereClause = whereClause.and(reviewJpaEntity.totalRating.eq(5.0));
        } else {
            whereClause = whereClause.and(
                reviewJpaEntity.totalRating.goe(rating.doubleValue())
                    .and(reviewJpaEntity.totalRating.lt(rating.doubleValue() + 1.0))
            );
        }

        List<LatestReviewListItemResult> reviews = queryFactory
            .select(new QLatestReviewListItemResult(
                reviewJpaEntity.id,
                stationJpaEntity.stationName,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content,
                reviewJpaEntity.memberId,
                memberNicknameCol,
                uploadedFile.filePath,
                reviewJpaEntity.createdAt,
                productJpaEntity.id,
                productJpaEntity.name,
                JPAExpressions.select(subReviewLike.count())
                    .from(subReviewLike)
                    .where(subReviewLike.reviewId.eq(reviewJpaEntity.id)),
                JPAExpressions.select(subReviewComment.count())
                    .from(subReviewComment)
                    .where(subReviewComment.reviewId.eq(reviewJpaEntity.id)
                        .and(subReviewComment.hidden.eq(false)))
            ))
            .from(reviewJpaEntity)
            .innerJoin(shop).on(reviewJpaEntity.shopId.eq(shopIdCol))
            .innerJoin(stationJpaEntity).on(shopStationIdCol.eq(stationJpaEntity.id))
            .innerJoin(member).on(Expressions.numberPath(Long.class, reviewJpaEntity, "memberId").eq(memberIdCol))
            .leftJoin(uploadedFile).on(memberProfileImageFileIdCol.eq(uploadedFile.id))
            .leftJoin(productJpaEntity).on(reviewJpaEntity.productId.eq(productJpaEntity.id))
            .where(whereClause)
            .orderBy(reviewJpaEntity.createdAt.desc())
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
        var whereClause = reviewJpaEntity.productId.eq(productId).and(reviewJpaEntity.hidden.eq(false));
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
            .select(new QLatestReviewListItemResult(
                reviewJpaEntity.id,
                stationJpaEntity.stationName,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content,
                reviewJpaEntity.memberId,
                memberNicknameCol,
                uploadedFile.filePath,
                reviewJpaEntity.createdAt,
                productJpaEntity.id,
                productJpaEntity.name,
                JPAExpressions.select(subReviewLike.count())
                    .from(subReviewLike)
                    .where(subReviewLike.reviewId.eq(reviewJpaEntity.id)),
                JPAExpressions.select(subReviewComment.count())
                    .from(subReviewComment)
                    .where(subReviewComment.reviewId.eq(reviewJpaEntity.id)
                        .and(subReviewComment.hidden.eq(false)))
            ))
            .from(reviewJpaEntity)
            .innerJoin(shop).on(reviewJpaEntity.shopId.eq(shopIdCol))
            .innerJoin(stationJpaEntity).on(shopStationIdCol.eq(stationJpaEntity.id))
            .innerJoin(member).on(Expressions.numberPath(Long.class, reviewJpaEntity, "memberId").eq(memberIdCol))
            .leftJoin(uploadedFile).on(memberProfileImageFileIdCol.eq(uploadedFile.id))
            .leftJoin(productJpaEntity).on(reviewJpaEntity.productId.eq(productJpaEntity.id))
            .where(whereClause);

        if ("RECOMMENDED".equals(sortType)) {
            query.leftJoin(sortReviewLike).on(sortReviewLike.reviewId.eq(reviewJpaEntity.id))
                .groupBy(reviewJpaEntity.id, stationJpaEntity.stationName, reviewJpaEntity.totalRating, reviewJpaEntity.content,
                    memberIdCol, memberNicknameCol, uploadedFile.filePath, reviewJpaEntity.createdAt,
                    productJpaEntity.id, productJpaEntity.name)
                .orderBy(sortReviewLike.count().desc(), reviewJpaEntity.createdAt.desc());
        } else if ("OLDEST".equals(sortType)) {
            query.orderBy(reviewJpaEntity.createdAt.asc());
        } else {
            query.orderBy(reviewJpaEntity.createdAt.desc());
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
        var whereClause = reviewJpaEntity.productId.eq(productId).and(reviewJpaEntity.hidden.eq(false));

        if (rating == 5) {
            whereClause = whereClause.and(reviewJpaEntity.totalRating.eq(5.0));
        } else {
            whereClause = whereClause.and(
                reviewJpaEntity.totalRating.goe(rating.doubleValue())
                    .and(reviewJpaEntity.totalRating.lt(rating.doubleValue() + 1.0))
            );
        }

        List<LatestReviewListItemResult> reviews = queryFactory
            .select(new QLatestReviewListItemResult(
                reviewJpaEntity.id,
                stationJpaEntity.stationName,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content,
                reviewJpaEntity.memberId,
                memberNicknameCol,
                uploadedFile.filePath,
                reviewJpaEntity.createdAt,
                productJpaEntity.id,
                productJpaEntity.name,
                JPAExpressions.select(subReviewLike.count())
                    .from(subReviewLike)
                    .where(subReviewLike.reviewId.eq(reviewJpaEntity.id)),
                JPAExpressions.select(subReviewComment.count())
                    .from(subReviewComment)
                    .where(subReviewComment.reviewId.eq(reviewJpaEntity.id)
                        .and(subReviewComment.hidden.eq(false)))
            ))
            .from(reviewJpaEntity)
            .innerJoin(shop).on(reviewJpaEntity.shopId.eq(shopIdCol))
            .innerJoin(stationJpaEntity).on(shopStationIdCol.eq(stationJpaEntity.id))
            .innerJoin(member).on(Expressions.numberPath(Long.class, reviewJpaEntity, "memberId").eq(memberIdCol))
            .leftJoin(uploadedFile).on(memberProfileImageFileIdCol.eq(uploadedFile.id))
            .leftJoin(productJpaEntity).on(reviewJpaEntity.productId.eq(productJpaEntity.id))
            .where(whereClause)
            .orderBy(reviewJpaEntity.createdAt.desc())
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
            .from(reviewImageJpaEntity)
            .innerJoin(uploadedFile).on(reviewImageJpaEntity.imageFileId.eq(uploadedFile.id))
            .where(reviewImageJpaEntity.reviewId.eq(reviewId))
            .orderBy(reviewImageJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public PageResult<MyReviewListItemResult> findMyReviews(MemberId memberId, PageQuery pageQuery) {
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

        List<Long> pagedReviewIds = queryFactory
            .select(reviewJpaEntity.id)
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.memberId.eq(memberId),
                reviewJpaEntity.hidden.eq(false)
            )
            .orderBy(reviewJpaEntity.createdAt.desc())
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
    public PageResult<MyReviewListItemResult> findReviewsByMemberId(MemberId memberId, PageQuery pageQuery) {
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

        List<Long> pagedReviewIds = queryFactory
            .select(reviewJpaEntity.id)
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.memberId.eq(memberId),
                reviewJpaEntity.hidden.eq(false)
            )
            .orderBy(reviewJpaEntity.createdAt.desc())
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
            .select(reviewImageJpaEntity.reviewId, uploadedFile.filePath)
            .from(reviewImageJpaEntity)
            .innerJoin(uploadedFile).on(reviewImageJpaEntity.imageFileId.eq(uploadedFile.id))
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

        return results.stream()
            .filter(tuple -> tuple.get(reviewImageJpaEntity.reviewId) != null && tuple.get(uploadedFile.filePath) != null)
            .collect(Collectors.toMap(
                tuple -> Objects.requireNonNull(tuple.get(reviewImageJpaEntity.reviewId)),
                tuple -> Objects.requireNonNull(tuple.get(uploadedFile.filePath)),
                (existing, replacement) -> existing
            ));
    }

    @Override
    public Long countByShopIdAndHiddenFalse(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    @Override
    public Long countWillRevisit(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.shopId.eq(shopId),
                reviewJpaEntity.hidden.eq(false),
                reviewJpaEntity.willRevisit.eq(true)
            )
            .fetchOne();
    }

    @Override
    public Double getAverageTasteRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.tasteRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAverageAmountRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.amountRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAveragePriceRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.priceRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAverageAtmosphereRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.atmosphereRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAverageKindnessRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.kindnessRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAverageHygieneRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.hygieneRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    @Override
    public Map<Integer, Long> getRatingCounts(Long shopId) {
        List<Tuple> results = queryFactory
            .select(reviewJpaEntity.totalRating.floor().intValue(), reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), reviewJpaEntity.hidden.eq(false))
            .groupBy(reviewJpaEntity.totalRating.floor().intValue())
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
            .select(reviewJpaEntity.createdAt.month(), reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.shopId.eq(shopId),
                reviewJpaEntity.hidden.eq(false),
                reviewJpaEntity.createdAt.year().eq(year)
            )
            .groupBy(reviewJpaEntity.createdAt.month())
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
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.productId.eq(productId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAverageTasteRatingByProductId(Long productId) {
        return queryFactory
            .select(reviewJpaEntity.tasteRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.productId.eq(productId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAverageAmountRatingByProductId(Long productId) {
        return queryFactory
            .select(reviewJpaEntity.amountRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.productId.eq(productId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAveragePriceRatingByProductId(Long productId) {
        return queryFactory
            .select(reviewJpaEntity.priceRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.productId.eq(productId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    @Override
    public Optional<Review> findByIdAndMemberId(ReviewId reviewId, MemberId memberId) {
        ReviewJpaEntity result = queryFactory
            .selectFrom(reviewJpaEntity)
            .where(
                reviewJpaEntity.id.eq(reviewId.value()),
                reviewJpaEntity.memberId.eq(memberId)
            )
            .fetchOne();
        return Optional.ofNullable(result).map(ReviewMapper::toDomain);
    }

    @Override
    public long countVisibleReviewsByMemberId(MemberId memberId) {
        Long count = queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.memberId.eq(memberId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
        return count != null ? count : 0L;
    }

    @Override
    public boolean existsByOrderIdAndProductIdAndMemberId(Long orderId, Long productId, MemberId memberId) {
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
    public Optional<Review> findById(ReviewId reviewId) {
        return reviewJpaRepository.findById(reviewId.value()).map(ReviewMapper::toDomain);
    }

    @Override
    public Review save(Review review) {
        if (review.getId() == null) {
            ReviewJpaEntity saved = reviewJpaRepository.save(ReviewMapper.toEntity(review));
            return ReviewMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ReviewJpaEntity entity = reviewJpaRepository.findById(review.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 리뷰입니다: " + review.getId()));
        ReviewMapper.applyChanges(entity, review);
        return ReviewMapper.toDomain(entity);
    }

    @Override
    public void deleteById(ReviewId reviewId) {
        reviewJpaRepository.deleteById(reviewId.value());
    }

    @Override
    public PageResult<ReviewListItemResult> findReviews(ReviewSearchCondition condition, PageQuery pageQuery) {
        JPAQuery<ReviewListItemResult> query = queryFactory
            .select(new QReviewListItemResult(
                reviewJpaEntity.id,
                reviewJpaEntity.shopId,
                reviewJpaEntity.productId,
                reviewJpaEntity.memberId,
                memberNicknameCol,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content,
                reviewJpaEntity.hidden,
                reviewJpaEntity.createdAt
            ))
            .from(reviewJpaEntity)
            .innerJoin(member).on(Expressions.numberPath(Long.class, reviewJpaEntity, "memberId").eq(memberIdCol))
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

    @Override
    public Optional<ReviewManagementDetailResult> findReviewManagementDetail(ReviewId reviewId) {
        ReviewManagementDetailResult result = queryFactory
            .select(new QReviewManagementDetailResult(
                reviewJpaEntity.id,
                shopIdCol,
                shopNameCol,
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
                memberNicknameCol,
                uploadedFile.filePath,
                reviewJpaEntity.createdAt
            ))
            .from(reviewJpaEntity)
            .innerJoin(shop).on(reviewJpaEntity.shopId.eq(shopIdCol))
            .innerJoin(stationJpaEntity).on(shopStationIdCol.eq(stationJpaEntity.id))
            .innerJoin(member).on(Expressions.numberPath(Long.class, reviewJpaEntity, "memberId").eq(memberIdCol))
            .leftJoin(uploadedFile).on(memberProfileImageFileIdCol.eq(uploadedFile.id))
            .where(reviewJpaEntity.id.eq(reviewId.value()))
            .fetchOne();

        if (result != null) {
            List<String> imageUrls = findImageUrlsByReviewId(reviewId.value());
            result = result.withImageUrls(imageUrls);
        }

        return Optional.ofNullable(result);
    }

    @Override
    public PageResult<SearchReviewItemResult> searchByKeyword(String keyword, PageQuery pageQuery) {
        Long total = queryFactory
            .select(reviewJpaEntity.countDistinct())
            .from(reviewJpaEntity)
            .innerJoin(reviewImageJpaEntity).on(reviewImageJpaEntity.reviewId.eq(reviewJpaEntity.id))
            .where(
                reviewJpaEntity.content.containsIgnoreCase(keyword)
                .and(reviewJpaEntity.hidden.eq(false))
            )
            .fetchOne();

        if (total == null || total == 0) return PageResult.empty(pageQuery.page(), pageQuery.size());

        List<SearchReviewItemResult> content = queryFactory
            .select(Projections.constructor(SearchReviewItemResult.class,
                reviewJpaEntity.id,
                uploadedFile.filePath
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
            .innerJoin(uploadedFile).on(reviewImageJpaEntity.imageFileId.eq(uploadedFile.id))
            .where(
                reviewJpaEntity.content.containsIgnoreCase(keyword)
                .and(reviewJpaEntity.hidden.eq(false))
            )
            .orderBy(reviewJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }
}
