package com.tastyhouse.core.repository.review;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.core.types.Projections;
import com.tastyhouse.core.entity.rank.dto.MemberReviewCountDto;
import com.tastyhouse.core.entity.rank.dto.QMemberReviewCountDto;
import com.tastyhouse.core.entity.review.Review;
import com.tastyhouse.core.entity.review.dto.BestReviewListItemDto;
import com.tastyhouse.core.entity.review.dto.LatestReviewListItemDto;
import com.tastyhouse.core.entity.review.dto.MyReviewListItemDto;
import com.tastyhouse.core.entity.review.dto.QBestReviewListItemDto;
import com.tastyhouse.core.entity.review.dto.QLatestReviewListItemDto;
import com.tastyhouse.core.entity.review.dto.ReviewDetailDto;
import com.tastyhouse.core.entity.review.dto.QReviewDetailDto;
import com.tastyhouse.core.entity.review.dto.SearchReviewItemDto;
import com.tastyhouse.core.entity.review.QReviewComment;
import com.tastyhouse.core.entity.review.QReviewImage;
import com.tastyhouse.core.entity.review.QReviewLike;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.entity.order.QOrderItem.orderItem;
import static com.tastyhouse.core.entity.place.QPlace.place;
import static com.tastyhouse.core.entity.place.QPlaceStation.placeStation;
import static com.tastyhouse.core.entity.product.QProduct.product;
import static com.tastyhouse.core.entity.review.QReview.review;
import static com.tastyhouse.core.entity.review.QReviewImage.reviewImage;
import static com.tastyhouse.core.domain.member.domain.model.QMember.member;

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
    public Page<BestReviewListItemDto> findBestReviews(Pageable pageable) {
        JPAQuery<BestReviewListItemDto> query = queryFactory
            .select(new QBestReviewListItemDto(
                review.id,
                uploadedFile.filePath,
                placeStation.stationName,
                place.name,
                orderItem.productName,
                review.totalRating,
                review.content
            ))
            .from(review)
            .innerJoin(place).on(review.placeId.eq(place.id))
            .innerJoin(placeStation).on(place.stationId.eq(placeStation.id))
            .leftJoin(orderItem).on(
                orderItem.orderId.eq(review.orderId)
                .and(orderItem.productId.eq(review.productId))
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
            .where(review.isHidden.eq(false))
            .orderBy(review.totalRating.desc(), review.createdAt.desc());

        long total = query.fetch().size();

        List<BestReviewListItemDto> reviews = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return new PageImpl<>(reviews, pageable, total);
    }

    @Override
    public Page<LatestReviewListItemDto> findLatestReviews(Pageable pageable) {
        JPAQuery<LatestReviewListItemDto> query = queryFactory
            .select(new QLatestReviewListItemDto(
                review.id,
                placeStation.stationName,
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
                        .and(subReviewComment.isHidden.eq(false)))
            ))
            .from(review)
            .innerJoin(place).on(review.placeId.eq(place.id))
            .innerJoin(placeStation).on(place.stationId.eq(placeStation.id))
            .innerJoin(member).on(review.memberId.eq(member.id))
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
            .leftJoin(product).on(review.productId.eq(product.id))
            .where(review.isHidden.eq(false))
            .orderBy(review.createdAt.desc());

        long total = query.fetch().size();

        List<LatestReviewListItemDto> reviews = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        if (!reviews.isEmpty()) {
            List<Long> reviewIds = reviews.stream().map(LatestReviewListItemDto::id).toList();
            Map<Long, List<String>> imageUrlsMap = findImageUrlsByReviewIds(reviewIds);
            reviews = reviews.stream()
                .map(r -> r.withImageUrls(imageUrlsMap.getOrDefault(r.id(), List.of())))
                .collect(Collectors.toList());
        }

        return new PageImpl<>(reviews, pageable, total);
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
    public Page<LatestReviewListItemDto> findLatestReviewsByFollowing(List<Long> followingMemberIds, Pageable pageable) {
        JPAQuery<LatestReviewListItemDto> query = queryFactory
            .select(new QLatestReviewListItemDto(
                review.id,
                placeStation.stationName,
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
                        .and(subReviewComment.isHidden.eq(false)))
            ))
            .from(review)
            .innerJoin(place).on(review.placeId.eq(place.id))
            .innerJoin(placeStation).on(place.stationId.eq(placeStation.id))
            .innerJoin(member).on(review.memberId.eq(member.id))
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
            .leftJoin(product).on(review.productId.eq(product.id))
            .where(
                review.memberId.in(followingMemberIds),
                review.isHidden.eq(false)
            )
            .orderBy(review.createdAt.desc());

        long total = query.fetch().size();

        List<LatestReviewListItemDto> reviews = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        if (!reviews.isEmpty()) {
            List<Long> reviewIds = reviews.stream().map(LatestReviewListItemDto::id).toList();
            Map<Long, List<String>> imageUrlsMap = findImageUrlsByReviewIds(reviewIds);
            reviews = reviews.stream()
                .map(r -> r.withImageUrls(imageUrlsMap.getOrDefault(r.id(), List.of())))
                .collect(Collectors.toList());
        }

        return new PageImpl<>(reviews, pageable, total);
    }

    @Override
    public Page<LatestReviewListItemDto> findLatestReviewsByPlaceId(Long placeId, Integer rating, Pageable pageable, Boolean hasImage, String sortType) {
        var whereClause = review.placeId.eq(placeId).and(review.isHidden.eq(false));
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

        JPAQuery<LatestReviewListItemDto> query = queryFactory
            .select(new QLatestReviewListItemDto(
                review.id,
                placeStation.stationName,
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
                        .and(subReviewComment.isHidden.eq(false)))
            ))
            .from(review)
            .innerJoin(place).on(review.placeId.eq(place.id))
            .innerJoin(placeStation).on(place.stationId.eq(placeStation.id))
            .innerJoin(member).on(review.memberId.eq(member.id))
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
            .leftJoin(product).on(review.productId.eq(product.id))
            .where(whereClause);

        if ("RECOMMENDED".equals(sortType)) {
            query.leftJoin(sortReviewLike).on(sortReviewLike.reviewId.eq(review.id))
                .groupBy(review.id, placeStation.stationName, review.totalRating, review.content,
                    member.id, member.nickname, uploadedFile.filePath, review.createdAt,
                    product.id, product.name)
                .orderBy(sortReviewLike.count().desc(), review.createdAt.desc());
        } else if ("OLDEST".equals(sortType)) {
            query.orderBy(review.createdAt.asc());
        } else {
            query.orderBy(review.createdAt.desc());
        }

        long total = query.fetch().size();

        List<LatestReviewListItemDto> reviews = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        if (!reviews.isEmpty()) {
            List<Long> reviewIds = reviews.stream().map(LatestReviewListItemDto::id).toList();
            Map<Long, List<String>> imageUrlsMap = findImageUrlsByReviewIds(reviewIds);
            reviews = reviews.stream()
                .map(r -> r.withImageUrls(imageUrlsMap.getOrDefault(r.id(), List.of())))
                .collect(Collectors.toList());
        }

        return new PageImpl<>(reviews, pageable, total);
    }

    @Override
    public List<MemberReviewCountDto> countReviewsByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return queryFactory
            .select(new QMemberReviewCountDto(
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
    public Optional<ReviewDetailDto> findReviewDetail(Long reviewId) {
        ReviewDetailDto result = queryFactory
            .select(new QReviewDetailDto(
                review.id,
                place.id,
                place.name,
                placeStation.stationName,
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
            .innerJoin(place).on(review.placeId.eq(place.id))
            .innerJoin(placeStation).on(place.stationId.eq(placeStation.id))
            .innerJoin(member).on(review.memberId.eq(member.id))
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
            .where(
                review.id.eq(reviewId),
                review.isHidden.eq(false)
            )
            .fetchOne();

        if (result != null) {
            List<String> imageUrls = findImageUrlsByReviewId(reviewId);
            result = result.withImageUrls(imageUrls);
        }

        return Optional.ofNullable(result);
    }

    @Override
    public List<LatestReviewListItemDto> findReviewsByPlaceIdAndRating(Long placeId, Integer rating, int limit) {
        var whereClause = review.placeId.eq(placeId).and(review.isHidden.eq(false));

        if (rating == 5) {
            whereClause = whereClause.and(review.totalRating.eq(5.0));
        } else {
            whereClause = whereClause.and(
                review.totalRating.goe(rating.doubleValue())
                    .and(review.totalRating.lt(rating.doubleValue() + 1.0))
            );
        }

        List<LatestReviewListItemDto> reviews = queryFactory
            .select(new QLatestReviewListItemDto(
                review.id,
                placeStation.stationName,
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
                        .and(subReviewComment.isHidden.eq(false)))
            ))
            .from(review)
            .innerJoin(place).on(review.placeId.eq(place.id))
            .innerJoin(placeStation).on(place.stationId.eq(placeStation.id))
            .innerJoin(member).on(review.memberId.eq(member.id))
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
            .leftJoin(product).on(review.productId.eq(product.id))
            .where(whereClause)
            .orderBy(review.createdAt.desc())
            .limit(limit)
            .fetch();

        if (!reviews.isEmpty()) {
            List<Long> reviewIds = reviews.stream().map(LatestReviewListItemDto::id).toList();
            Map<Long, List<String>> imageUrlsMap = findImageUrlsByReviewIds(reviewIds);
            reviews = reviews.stream()
                .map(r -> r.withImageUrls(imageUrlsMap.getOrDefault(r.id(), List.of())))
                .collect(Collectors.toList());
        }

        return reviews;
    }

    @Override
    public Page<LatestReviewListItemDto> findLatestReviewsByProductId(Long productId, Integer rating, Pageable pageable, Boolean hasImage, String sortType) {
        var whereClause = review.productId.eq(productId).and(review.isHidden.eq(false));
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

        JPAQuery<LatestReviewListItemDto> query = queryFactory
            .select(new QLatestReviewListItemDto(
                review.id,
                placeStation.stationName,
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
                        .and(subReviewComment.isHidden.eq(false)))
            ))
            .from(review)
            .innerJoin(place).on(review.placeId.eq(place.id))
            .innerJoin(placeStation).on(place.stationId.eq(placeStation.id))
            .innerJoin(member).on(review.memberId.eq(member.id))
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
            .leftJoin(product).on(review.productId.eq(product.id))
            .where(whereClause);

        if ("RECOMMENDED".equals(sortType)) {
            query.leftJoin(sortReviewLike).on(sortReviewLike.reviewId.eq(review.id))
                .groupBy(review.id, placeStation.stationName, review.totalRating, review.content,
                    member.id, member.nickname, uploadedFile.filePath, review.createdAt,
                    product.id, product.name)
                .orderBy(sortReviewLike.count().desc(), review.createdAt.desc());
        } else if ("OLDEST".equals(sortType)) {
            query.orderBy(review.createdAt.asc());
        } else {
            query.orderBy(review.createdAt.desc());
        }

        long total = query.fetch().size();

        List<LatestReviewListItemDto> reviews = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        if (!reviews.isEmpty()) {
            List<Long> reviewIds = reviews.stream().map(LatestReviewListItemDto::id).toList();
            Map<Long, List<String>> imageUrlsMap = findImageUrlsByReviewIds(reviewIds);
            reviews = reviews.stream()
                .map(r -> r.withImageUrls(imageUrlsMap.getOrDefault(r.id(), List.of())))
                .collect(Collectors.toList());
        }

        return new PageImpl<>(reviews, pageable, total);
    }

    @Override
    public List<LatestReviewListItemDto> findReviewsByProductIdAndRating(Long productId, Integer rating, int limit) {
        var whereClause = review.productId.eq(productId).and(review.isHidden.eq(false));

        if (rating == 5) {
            whereClause = whereClause.and(review.totalRating.eq(5.0));
        } else {
            whereClause = whereClause.and(
                review.totalRating.goe(rating.doubleValue())
                    .and(review.totalRating.lt(rating.doubleValue() + 1.0))
            );
        }

        List<LatestReviewListItemDto> reviews = queryFactory
            .select(new QLatestReviewListItemDto(
                review.id,
                placeStation.stationName,
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
                        .and(subReviewComment.isHidden.eq(false)))
            ))
            .from(review)
            .innerJoin(place).on(review.placeId.eq(place.id))
            .innerJoin(placeStation).on(place.stationId.eq(placeStation.id))
            .innerJoin(member).on(review.memberId.eq(member.id))
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
            .leftJoin(product).on(review.productId.eq(product.id))
            .where(whereClause)
            .orderBy(review.createdAt.desc())
            .limit(limit)
            .fetch();

        if (!reviews.isEmpty()) {
            List<Long> reviewIds = reviews.stream().map(LatestReviewListItemDto::id).toList();
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
    public Page<MyReviewListItemDto> findMyReviews(Long memberId, Pageable pageable) {
        List<Long> allReviewIds = queryFactory
            .select(review.id)
            .from(review)
            .where(
                review.memberId.eq(memberId),
                review.isHidden.eq(false)
            )
            .orderBy(review.createdAt.desc())
            .fetch();

        long total = allReviewIds.size();

        List<Long> pagedReviewIds = queryFactory
            .select(review.id)
            .from(review)
            .where(
                review.memberId.eq(memberId),
                review.isHidden.eq(false)
            )
            .orderBy(review.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Map<Long, String> imageUrlMap = findFirstImageUrlsByReviewIds(pagedReviewIds);

        List<MyReviewListItemDto> reviews = pagedReviewIds.stream()
            .map(reviewId -> new MyReviewListItemDto(reviewId, imageUrlMap.get(reviewId)))
            .collect(Collectors.toList());

        return new PageImpl<>(reviews, pageable, total);
    }

    @Override
    public Page<MyReviewListItemDto> findReviewsByMemberId(Long memberId, Pageable pageable) {
        List<Long> allReviewIds = queryFactory
            .select(review.id)
            .from(review)
            .where(
                review.memberId.eq(memberId),
                review.isHidden.eq(false)
            )
            .orderBy(review.createdAt.desc())
            .fetch();

        long total = allReviewIds.size();

        List<Long> pagedReviewIds = queryFactory
            .select(review.id)
            .from(review)
            .where(
                review.memberId.eq(memberId),
                review.isHidden.eq(false)
            )
            .orderBy(review.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Map<Long, String> imageUrlMap = findFirstImageUrlsByReviewIds(pagedReviewIds);

        List<MyReviewListItemDto> reviews = pagedReviewIds.stream()
            .map(reviewId -> new MyReviewListItemDto(reviewId, imageUrlMap.get(reviewId)))
            .collect(Collectors.toList());

        return new PageImpl<>(reviews, pageable, total);
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
    public Long countByPlaceIdAndIsHiddenFalse(Long placeId) {
        return queryFactory
            .select(review.count())
            .from(review)
            .where(review.placeId.eq(placeId), review.isHidden.eq(false))
            .fetchOne();
    }

    @Override
    public Long countWillRevisit(Long placeId) {
        return queryFactory
            .select(review.count())
            .from(review)
            .where(
                review.placeId.eq(placeId),
                review.isHidden.eq(false),
                review.willRevisit.eq(true)
            )
            .fetchOne();
    }

    @Override
    public Double getAverageTasteRating(Long placeId) {
        return queryFactory
            .select(review.tasteRating.avg())
            .from(review)
            .where(review.placeId.eq(placeId), review.isHidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAverageAmountRating(Long placeId) {
        return queryFactory
            .select(review.amountRating.avg())
            .from(review)
            .where(review.placeId.eq(placeId), review.isHidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAveragePriceRating(Long placeId) {
        return queryFactory
            .select(review.priceRating.avg())
            .from(review)
            .where(review.placeId.eq(placeId), review.isHidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAverageAtmosphereRating(Long placeId) {
        return queryFactory
            .select(review.atmosphereRating.avg())
            .from(review)
            .where(review.placeId.eq(placeId), review.isHidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAverageKindnessRating(Long placeId) {
        return queryFactory
            .select(review.kindnessRating.avg())
            .from(review)
            .where(review.placeId.eq(placeId), review.isHidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAverageHygieneRating(Long placeId) {
        return queryFactory
            .select(review.hygieneRating.avg())
            .from(review)
            .where(review.placeId.eq(placeId), review.isHidden.eq(false))
            .fetchOne();
    }

    @Override
    public Map<Integer, Long> getRatingCounts(Long placeId) {
        List<Tuple> results = queryFactory
            .select(review.totalRating.floor().intValue(), review.count())
            .from(review)
            .where(review.placeId.eq(placeId), review.isHidden.eq(false))
            .groupBy(review.totalRating.floor().intValue())
            .fetch();

        Map<Integer, Long> ratingMap = new HashMap<>();
        for (Tuple row : results) {
            ratingMap.put(row.get(0, Integer.class), row.get(1, Long.class));
        }
        return ratingMap;
    }

    @Override
    public Map<Integer, Long> getMonthlyReviewCounts(Long placeId, int year) {
        List<Tuple> results = queryFactory
            .select(review.createdAt.month(), review.count())
            .from(review)
            .where(
                review.placeId.eq(placeId),
                review.isHidden.eq(false),
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
    public Long countByProductIdAndIsHiddenFalse(Long productId) {
        return queryFactory
            .select(review.count())
            .from(review)
            .where(review.productId.eq(productId), review.isHidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAverageTasteRatingByProductId(Long productId) {
        return queryFactory
            .select(review.tasteRating.avg())
            .from(review)
            .where(review.productId.eq(productId), review.isHidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAverageAmountRatingByProductId(Long productId) {
        return queryFactory
            .select(review.amountRating.avg())
            .from(review)
            .where(review.productId.eq(productId), review.isHidden.eq(false))
            .fetchOne();
    }

    @Override
    public Double getAveragePriceRatingByProductId(Long productId) {
        return queryFactory
            .select(review.priceRating.avg())
            .from(review)
            .where(review.productId.eq(productId), review.isHidden.eq(false))
            .fetchOne();
    }

    @Override
    public Optional<Review> findByIdAndMemberId(Long reviewId, Long memberId) {
        Review result = queryFactory
            .selectFrom(review)
            .where(
                review.id.eq(reviewId),
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
            .where(review.memberId.eq(memberId), review.isHidden.eq(false))
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
    public Optional<Review> findById(Long reviewId) {
        return reviewJpaRepository.findById(reviewId);
    }

    @Override
    public Review save(Review review) {
        return reviewJpaRepository.save(review);
    }

    @Override
    public void deleteById(Long reviewId) {
        reviewJpaRepository.deleteById(reviewId);
    }

    @Override
    public Page<SearchReviewItemDto> searchByKeyword(String keyword, Pageable pageable) {
        Long total = queryFactory
            .select(review.countDistinct())
            .from(review)
            .innerJoin(reviewImage).on(reviewImage.reviewId.eq(review.id))
            .where(
                review.content.containsIgnoreCase(keyword)
                .and(review.isHidden.eq(false))
            )
            .fetchOne();

        if (total == null || total == 0) return new PageImpl<>(List.of(), pageable, 0);

        List<SearchReviewItemDto> content = queryFactory
            .select(Projections.constructor(SearchReviewItemDto.class,
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
                .and(review.isHidden.eq(false))
            )
            .orderBy(review.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return new PageImpl<>(content, pageable, total);
    }
}
