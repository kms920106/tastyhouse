package com.tastyhouse.infrastructure.review.query;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.review.domain.model.ReviewSortType;
import com.tastyhouse.domain.review.domain.vo.ReviewCommentId;
import com.tastyhouse.domain.review.domain.vo.ReviewId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity;
import com.tastyhouse.infrastructure.review.persistence.QReviewCommentJpaEntity;
import com.tastyhouse.infrastructure.review.persistence.QReviewImageJpaEntity;
import com.tastyhouse.infrastructure.review.persistence.QReviewLikeJpaEntity;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity.memberJpaEntity;
import static com.tastyhouse.infrastructure.order.persistence.QOrderProductJpaEntity.orderProductJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductJpaEntity.productJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewCommentJpaEntity.reviewCommentJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewImageJpaEntity.reviewImageJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewJpaEntity.reviewJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewLikeJpaEntity.reviewLikeJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewReplyJpaEntity.reviewReplyJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewTagJpaEntity.reviewTagJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QStationJpaEntity.stationJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QTagJpaEntity.tagJpaEntity;

/**
 * 리뷰 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로 write
 * 포트({@code ReviewRepository})와 역할이 겹치지 않는다. 소비 모듈(web-api)의 리뷰 조회 서비스가 이
 * DAO를 주입해 사용하며, 그 덕분에 api 모듈은 QueryDSL을 알지 않는다.
 *
 * <p>admin(관리) 화면 전용 조회는 {@code ReviewManagementQueryDao}로, 집계·통계 조회는
 * {@code ReviewStatisticsQueryDao}로 분리했다. 여기에는 web/공용 목록·상세 조회만 둔다.
 */
@Repository
@RequiredArgsConstructor
public class ReviewQueryDao {

    private static final QReviewImageJpaEntity subReviewImage = new QReviewImageJpaEntity("subReviewImage");
    private static final QReviewLikeJpaEntity subReviewLike = new QReviewLikeJpaEntity("subReviewLike");
    private static final QReviewCommentJpaEntity subReviewComment = new QReviewCommentJpaEntity("subReviewComment");
    private static final QReviewLikeJpaEntity sortReviewLike = new QReviewLikeJpaEntity("sortReviewLike");

    /**
     * 답글의 "누구에게 단 답글인지"(replyTo) 회원을 조인하기 위한 별칭. 작성자 조인과 같은 회원 테이블이라
     * 별칭을 분리해야 한다.
     */
    private static final QMemberJpaEntity replyToMember = new QMemberJpaEntity("replyToMember");

    private final JPAQueryFactory queryFactory;

    /**
     * 베스트 리뷰 목록 — 총점 높은 순 → 최신순. 대표 이미지(정렬값이 가장 작은 리뷰 이미지)를 함께 투영한다.
     */
    public PageResult<BestReviewListItemResult> findBestReviews(PageQuery pageQuery) {
        JPAQuery<BestReviewListItemResult> query = queryFactory
            .select(new QBestReviewListItemResult(
                reviewJpaEntity.id,
                uploadedFileJpaEntity.filePath,
                stationJpaEntity.stationName,
                shopJpaEntity.name,
                orderProductJpaEntity.name,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content
            ))
            .from(reviewJpaEntity)
            .innerJoin(shopJpaEntity).on(reviewShopId().eq(shopJpaEntity.id))
            .innerJoin(stationJpaEntity).on(shopStationId().eq(stationJpaEntity.id))
            .leftJoin(orderProductJpaEntity).on(
                Expressions.numberPath(Long.class, orderProductJpaEntity, "orderId").eq(reviewOrderId())
                .and(Expressions.numberPath(Long.class, orderProductJpaEntity, "productId").eq(reviewProductId()))
            )
            .leftJoin(reviewImageJpaEntity).on(
                imageReviewId().eq(reviewJpaEntity.id)
                .and(reviewImageJpaEntity.sort.eq(
                    JPAExpressions
                        .select(subReviewImage.sort.min())
                        .from(subReviewImage)
                        .where(subImageReviewId().eq(reviewJpaEntity.id))
                ))
            )
            .leftJoin(uploadedFileJpaEntity).on(imageImageFileId().eq(uploadedFileJpaEntity.id))
            .where(reviewJpaEntity.hidden.eq(false))
            .orderBy(reviewJpaEntity.totalRating.desc(), reviewJpaEntity.createdAt.desc());

        long total = query.fetch().size();

        List<BestReviewListItemResult> reviews = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(reviews, total, pageQuery.page(), pageQuery.size());
    }

    /**
     * 최신 리뷰 목록 — 작성 최신순.
     */
    public PageResult<LatestReviewListItemResult> findLatestReviews(PageQuery pageQuery) {
        JPAQuery<LatestReviewListItemResult> query = queryFactory
            .select(new QLatestReviewListItemResult(
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
                    .where(subLikeReviewId().eq(reviewJpaEntity.id)),
                JPAExpressions.select(subReviewComment.count())
                    .from(subReviewComment)
                    .where(subCommentReviewId().eq(reviewJpaEntity.id)
                    .and(subReviewComment.hidden.eq(false)))
            ))
            .from(reviewJpaEntity)
            .innerJoin(shopJpaEntity).on(reviewShopId().eq(shopJpaEntity.id))
            .innerJoin(stationJpaEntity).on(shopStationId().eq(stationJpaEntity.id))
            .innerJoin(memberJpaEntity).on(Expressions.numberPath(Long.class, reviewJpaEntity, "memberId").eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .leftJoin(productJpaEntity).on(reviewProductId().eq(productJpaEntity.id))
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

    /**
     * 팔로잉한 회원들이 쓴 최신 리뷰 목록.
     */
    public PageResult<LatestReviewListItemResult> findLatestReviewsByFollowing(List<Long> followingMemberIds, PageQuery pageQuery) {
        JPAQuery<LatestReviewListItemResult> query = queryFactory
            .select(new QLatestReviewListItemResult(
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
                    .where(subLikeReviewId().eq(reviewJpaEntity.id)),
                JPAExpressions.select(subReviewComment.count())
                    .from(subReviewComment)
                    .where(subCommentReviewId().eq(reviewJpaEntity.id)
                        .and(subReviewComment.hidden.eq(false)))
            ))
            .from(reviewJpaEntity)
            .innerJoin(shopJpaEntity).on(reviewShopId().eq(shopJpaEntity.id))
            .innerJoin(stationJpaEntity).on(shopStationId().eq(stationJpaEntity.id))
            .innerJoin(memberJpaEntity).on(Expressions.numberPath(Long.class, reviewJpaEntity, "memberId").eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .leftJoin(productJpaEntity).on(reviewProductId().eq(productJpaEntity.id))
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

    /**
     * 가게별 리뷰 목록 — 평점·이미지 유무 필터와 정렬 방식({@link ReviewSortType})을 지원한다.
     */
    public PageResult<LatestReviewListItemResult> findLatestReviewsByShopId(Long shopId, Integer rating, PageQuery pageQuery, Boolean hasImage, ReviewSortType sortType) {
        var whereClause = reviewShopId().eq(shopId).and(reviewJpaEntity.hidden.eq(false));
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
                        .where(subImageReviewId().eq(reviewJpaEntity.id))
                        .exists()
                );
            } else {
                whereClause = whereClause.and(
                    JPAExpressions
                        .selectOne()
                        .from(subReviewImage)
                        .where(subImageReviewId().eq(reviewJpaEntity.id))
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
                memberJpaEntity.nickname,
                uploadedFileJpaEntity.filePath,
                reviewJpaEntity.createdAt,
                productJpaEntity.id,
                productJpaEntity.name,
                JPAExpressions.select(subReviewLike.count())
                    .from(subReviewLike)
                    .where(subLikeReviewId().eq(reviewJpaEntity.id)),
                JPAExpressions.select(subReviewComment.count())
                    .from(subReviewComment)
                    .where(subCommentReviewId().eq(reviewJpaEntity.id)
                    .and(subReviewComment.hidden.eq(false)))
            ))
            .from(reviewJpaEntity)
            .innerJoin(shopJpaEntity).on(reviewShopId().eq(shopJpaEntity.id))
            .innerJoin(stationJpaEntity).on(shopStationId().eq(stationJpaEntity.id))
            .innerJoin(memberJpaEntity).on(Expressions.numberPath(Long.class, reviewJpaEntity, "memberId").eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .leftJoin(productJpaEntity).on(reviewProductId().eq(productJpaEntity.id))
            .where(whereClause);

        applySort(query, sortType);

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

    /**
     * 상품별 리뷰 목록 — 평점·이미지 유무 필터와 정렬 방식을 지원한다.
     */
    public PageResult<LatestReviewListItemResult> findLatestReviewsByProductId(Long productId, Integer rating, PageQuery pageQuery, Boolean hasImage, ReviewSortType sortType) {
        var whereClause = reviewProductId().eq(productId).and(reviewJpaEntity.hidden.eq(false));
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
                        .where(subImageReviewId().eq(reviewJpaEntity.id))
                        .exists()
                );
            } else {
                whereClause = whereClause.and(
                    JPAExpressions
                        .selectOne()
                        .from(subReviewImage)
                        .where(subImageReviewId().eq(reviewJpaEntity.id))
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
                memberJpaEntity.nickname,
                uploadedFileJpaEntity.filePath,
                reviewJpaEntity.createdAt,
                productJpaEntity.id,
                productJpaEntity.name,
                JPAExpressions.select(subReviewLike.count())
                    .from(subReviewLike)
                    .where(subLikeReviewId().eq(reviewJpaEntity.id)),
                JPAExpressions.select(subReviewComment.count())
                    .from(subReviewComment)
                    .where(subCommentReviewId().eq(reviewJpaEntity.id)
                        .and(subReviewComment.hidden.eq(false)))
            ))
            .from(reviewJpaEntity)
            .innerJoin(shopJpaEntity).on(reviewShopId().eq(shopJpaEntity.id))
            .innerJoin(stationJpaEntity).on(shopStationId().eq(stationJpaEntity.id))
            .innerJoin(memberJpaEntity).on(Expressions.numberPath(Long.class, reviewJpaEntity, "memberId").eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .leftJoin(productJpaEntity).on(reviewProductId().eq(productJpaEntity.id))
            .where(whereClause);

        applySort(query, sortType);

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

    /**
     * 가게의 특정 평점대 리뷰를 제한 건수만큼 조회한다(평점별 미리보기).
     */
    public List<LatestReviewListItemResult> findReviewsByShopIdAndRating(Long shopId, Integer rating, int limit) {
        var whereClause = reviewShopId().eq(shopId).and(reviewJpaEntity.hidden.eq(false));

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
                memberJpaEntity.nickname,
                uploadedFileJpaEntity.filePath,
                reviewJpaEntity.createdAt,
                productJpaEntity.id,
                productJpaEntity.name,
                JPAExpressions.select(subReviewLike.count())
                    .from(subReviewLike)
                    .where(subLikeReviewId().eq(reviewJpaEntity.id)),
                JPAExpressions.select(subReviewComment.count())
                    .from(subReviewComment)
                    .where(subCommentReviewId().eq(reviewJpaEntity.id)
                        .and(subReviewComment.hidden.eq(false)))
            ))
            .from(reviewJpaEntity)
            .innerJoin(shopJpaEntity).on(reviewShopId().eq(shopJpaEntity.id))
            .innerJoin(stationJpaEntity).on(shopStationId().eq(stationJpaEntity.id))
            .innerJoin(memberJpaEntity).on(Expressions.numberPath(Long.class, reviewJpaEntity, "memberId").eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .leftJoin(productJpaEntity).on(reviewProductId().eq(productJpaEntity.id))
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

    /**
     * 상품의 특정 평점대 리뷰를 제한 건수만큼 조회한다(평점별 미리보기).
     */
    public List<LatestReviewListItemResult> findReviewsByProductIdAndRating(Long productId, Integer rating, int limit) {
        var whereClause = reviewProductId().eq(productId).and(reviewJpaEntity.hidden.eq(false));

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
                memberJpaEntity.nickname,
                uploadedFileJpaEntity.filePath,
                reviewJpaEntity.createdAt,
                productJpaEntity.id,
                productJpaEntity.name,
                JPAExpressions.select(subReviewLike.count())
                    .from(subReviewLike)
                    .where(subLikeReviewId().eq(reviewJpaEntity.id)),
                JPAExpressions.select(subReviewComment.count())
                    .from(subReviewComment)
                    .where(subCommentReviewId().eq(reviewJpaEntity.id)
                        .and(subReviewComment.hidden.eq(false)))
            ))
            .from(reviewJpaEntity)
            .innerJoin(shopJpaEntity).on(reviewShopId().eq(shopJpaEntity.id))
            .innerJoin(stationJpaEntity).on(shopStationId().eq(stationJpaEntity.id))
            .innerJoin(memberJpaEntity).on(Expressions.numberPath(Long.class, reviewJpaEntity, "memberId").eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .leftJoin(productJpaEntity).on(reviewProductId().eq(productJpaEntity.id))
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

    /**
     * 리뷰 상세(숨김 제외) — 이미지 URL을 함께 채운다. 없으면 비어 있다.
     */
    public Optional<ReviewDetailResult> findReviewDetail(ReviewId reviewId) {
        ReviewDetailResult result = queryFactory
            .select(new QReviewDetailResult(
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
                reviewJpaEntity.createdAt
            ))
            .from(reviewJpaEntity)
            .innerJoin(shopJpaEntity).on(reviewShopId().eq(shopJpaEntity.id))
            .innerJoin(stationJpaEntity).on(shopStationId().eq(stationJpaEntity.id))
            .innerJoin(memberJpaEntity).on(Expressions.numberPath(Long.class, reviewJpaEntity, "memberId").eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
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

    /**
     * 내가 쓴 리뷰 목록(대표 이미지 1장) — 최신순.
     */
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

    /**
     * 특정 회원이 쓴 리뷰 목록(대표 이미지 1장) — 최신순.
     */
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

    /**
     * 키워드로 리뷰를 검색한다(이미지가 있는 리뷰만, 대표 이미지 1장).
     */
    public PageResult<SearchReviewItemResult> searchByKeyword(String keyword, PageQuery pageQuery) {
        Long total = queryFactory
            .select(reviewJpaEntity.countDistinct())
            .from(reviewJpaEntity)
            .innerJoin(reviewImageJpaEntity).on(imageReviewId().eq(reviewJpaEntity.id))
            .where(
                reviewJpaEntity.content.containsIgnoreCase(keyword)
                .and(reviewJpaEntity.hidden.eq(false))
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
                imageReviewId().eq(reviewJpaEntity.id)
                .and(reviewImageJpaEntity.sort.eq(
                    JPAExpressions.select(subReviewImage.sort.min())
                        .from(subReviewImage)
                        .where(subImageReviewId().eq(reviewJpaEntity.id))
                ))
            )
            .innerJoin(uploadedFileJpaEntity).on(imageImageFileId().eq(uploadedFileJpaEntity.id))
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

    /**
     * 해당 주문·상품에 대해 그 회원이 이미 리뷰를 썼는지(중복 작성 차단용).
     */
    public boolean existsByOrderIdAndProductIdAndMemberId(Long orderId, Long productId, MemberId memberId) {
        return queryFactory
            .selectOne()
            .from(reviewJpaEntity)
            .where(
                reviewOrderId().eq(orderId),
                reviewProductId().eq(productId),
                reviewJpaEntity.memberId.eq(memberId)
            )
            .fetchFirst() != null;
    }

    /**
     * 리뷰가 가리키는 상품 식별자. 리뷰 상세와 상품 정보를 함께 보여주는 화면에서 쓴다.
     */
    public Optional<Long> findProductIdByReviewId(Long reviewId) {
        return Optional.ofNullable(queryFactory
            .select(reviewProductId())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.id.eq(reviewId))
            .fetchOne());
    }

    /**
     * 회원이 해당 리뷰에 좋아요를 눌렀는지 여부.
     */
    public boolean existsLike(ReviewId reviewId, MemberId memberId) {
        return queryFactory
            .selectOne()
            .from(reviewLikeJpaEntity)
            .where(
                likeReviewId().eq(reviewId.value()),
                reviewLikeJpaEntity.memberId.eq(memberId)
            )
            .fetchFirst() != null;
    }

    /**
     * 리뷰의 댓글 목록(숨김 포함) — 최신순.
     *
     * <p>관리 화면용 {@code ReviewManagementQueryDao#findCommentsIncludingHidden}과 달리 작성자 프로필
     * 이미지 경로까지 함께 투영한다(web 응답이 프로필 이미지 URL을 포함하기 때문). 숨김 댓글도 그대로
     * 내려주는 것은 기존 web 동작을 보존하기 위함이다 — 답글만 숨김을 제외한다.
     */
    public List<ReviewCommentItemResult> findComments(ReviewId reviewId) {
        return queryFactory
            .select(new QReviewCommentItemResult(
                reviewCommentJpaEntity.id,
                commentReviewId(),
                reviewCommentJpaEntity.memberId,
                memberJpaEntity.nickname,
                uploadedFileJpaEntity.filePath,
                reviewCommentJpaEntity.content,
                reviewCommentJpaEntity.createdAt
            ))
            .from(reviewCommentJpaEntity)
            .leftJoin(memberJpaEntity)
            .on(Expressions.numberPath(Long.class, reviewCommentJpaEntity, "memberId").eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .where(commentReviewId().eq(reviewId.value()))
            .orderBy(reviewCommentJpaEntity.createdAt.desc())
            .fetch();
    }

    /**
     * 여러 댓글에 달린 답글 목록(숨김 제외) — 작성순. 입력이 비어 있으면 조회하지 않고 빈 목록을 돌려준다.
     *
     * <p>작성자 프로필 이미지 경로를 함께 투영하며, 답글 대상 회원(replyTo)은 없을 수 있어 leftJoin으로
     * 붙인다.
     */
    public List<ReviewReplyItemResult> findVisibleReplies(List<ReviewCommentId> commentIds) {
        if (commentIds.isEmpty()) {
            return List.of();
        }

        List<Long> ids = commentIds.stream().map(ReviewCommentId::value).toList();

        return queryFactory
            .select(new QReviewReplyItemResult(
                reviewReplyJpaEntity.id,
                replyCommentId(),
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
            .on(Expressions.numberPath(Long.class, reviewReplyJpaEntity, "memberId").eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .leftJoin(replyToMember)
            .on(Expressions.numberPath(Long.class, reviewReplyJpaEntity, "replyToMemberId").eq(replyToMember.id))
            .where(
                replyCommentId().in(ids),
                reviewReplyJpaEntity.hidden.eq(false)
            )
            .orderBy(reviewReplyJpaEntity.createdAt.asc())
            .fetch();
    }

    /**
     * 리뷰에 달린 태그 ID 목록.
     */
    public List<Long> findTagIdsByReviewId(Long reviewId) {
        return queryFactory
            .select(tagTagId())
            .from(reviewTagJpaEntity)
            .where(tagReviewId().eq(reviewId))
            .fetch();
    }

    /**
     * 태그 ID 목록에 해당하는 태그명 목록. 입력이 비어 있으면 조회하지 않고 빈 목록을 돌려준다.
     */
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

    /**
     * 리뷰 목록 정렬 적용 — 가게별·상품별 목록이 공유한다.
     *
     * <p>추천순은 좋아요 수 집계가 필요해 별칭 조인 + {@code groupBy}가 따라붙고, 동수일 때는 최신순으로
     * 갈린다. 정렬 후보는 도메인 enum({@link ReviewSortType})이 소유하며 승격은 소비 모듈 Service가 한다.
     */
    private void applySort(JPAQuery<LatestReviewListItemResult> query, ReviewSortType sortType) {
        switch (sortType) {
            case RECOMMENDED -> query.leftJoin(sortReviewLike).on(sortLikeReviewId().eq(reviewJpaEntity.id))
                .groupBy(reviewJpaEntity.id, stationJpaEntity.stationName, reviewJpaEntity.totalRating, reviewJpaEntity.content,
                    memberJpaEntity.id, memberJpaEntity.nickname, uploadedFileJpaEntity.filePath, reviewJpaEntity.createdAt,
                    productJpaEntity.id, productJpaEntity.name)
                .orderBy(sortReviewLike.count().desc(), reviewJpaEntity.createdAt.desc());
            case OLDEST -> query.orderBy(reviewJpaEntity.createdAt.asc());
            case LATEST -> query.orderBy(reviewJpaEntity.createdAt.desc());
        }
    }

    /**
     * 여러 리뷰의 이미지 URL을 리뷰 ID별로 묶어 조회한다(정렬값 오름차순).
     */
    private Map<Long, List<String>> findImageUrlsByReviewIds(List<Long> reviewIds) {
        List<Tuple> results = queryFactory
            .select(imageReviewId(), uploadedFileJpaEntity.filePath)
            .from(reviewImageJpaEntity)
            .innerJoin(uploadedFileJpaEntity).on(imageImageFileId().eq(uploadedFileJpaEntity.id))
            .where(imageReviewId().in(reviewIds))
            .orderBy(reviewImageJpaEntity.sort.asc())
            .fetch();

        return results.stream()
            .filter(tuple -> tuple.get(imageReviewId()) != null)
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(imageReviewId())),
                Collectors.mapping(
                    tuple -> Objects.toString(tuple.get(uploadedFileJpaEntity.filePath), ""),
                    Collectors.toList()
                )
            ));
    }

    /**
     * 단일 리뷰의 이미지 URL 목록(정렬값 오름차순).
     */
    private List<String> findImageUrlsByReviewId(Long reviewId) {
        return queryFactory
            .select(uploadedFileJpaEntity.filePath)
            .from(reviewImageJpaEntity)
            .innerJoin(uploadedFileJpaEntity).on(imageImageFileId().eq(uploadedFileJpaEntity.id))
            .where(imageReviewId().eq(reviewId))
            .orderBy(reviewImageJpaEntity.sort.asc())
            .fetch();
    }

    /**
     * 여러 리뷰의 대표 이미지(정렬값이 가장 작은 1장) URL을 리뷰 ID별로 조회한다.
     */
    private Map<Long, String> findFirstImageUrlsByReviewIds(List<Long> reviewIds) {
        if (reviewIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> results = queryFactory
            .select(imageReviewId(), uploadedFileJpaEntity.filePath)
            .from(reviewImageJpaEntity)
            .innerJoin(uploadedFileJpaEntity).on(imageImageFileId().eq(uploadedFileJpaEntity.id))
            .where(
                imageReviewId().in(reviewIds),
                reviewImageJpaEntity.sort.eq(
                    JPAExpressions
                        .select(subReviewImage.sort.min())
                        .from(subReviewImage)
                        .where(subImageReviewId().eq(imageReviewId()))
                )
            )
            .fetch();

        return results.stream()
            .filter(tuple -> tuple.get(imageReviewId()) != null && tuple.get(uploadedFileJpaEntity.filePath) != null)
            .collect(Collectors.toMap(
                tuple -> Objects.requireNonNull(tuple.get(imageReviewId())),
                tuple -> Objects.requireNonNull(tuple.get(uploadedFileJpaEntity.filePath)),
                (existing, replacement) -> existing
            ));
    }

    // ----------------------------------------------------- @Convert VO 컬럼 우회
    // 아래 필드들은 @Convert로 도메인 VO(ShopId/ProductId/ReviewId/ReviewCommentId/MemberId/
    // UploadedFileId/TagId)에 매핑되어 QueryDSL이 VO 타입 path를 생성한다. query DAO 계층은 항상
    // raw Long을 쓰므로 Expressions.numberPath로 우회한다.

    private NumberPath<Long> reviewShopId() {
        return Expressions.numberPath(Long.class, reviewJpaEntity, "shopId");
    }

    private NumberPath<Long> reviewProductId() {
        return Expressions.numberPath(Long.class, reviewJpaEntity, "productId");
    }

    private NumberPath<Long> reviewOrderId() {
        return Expressions.numberPath(Long.class, reviewJpaEntity, "orderId");
    }

    private NumberPath<Long> subLikeReviewId() {
        return Expressions.numberPath(Long.class, subReviewLike, "reviewId");
    }

    private NumberPath<Long> subCommentReviewId() {
        return Expressions.numberPath(Long.class, subReviewComment, "reviewId");
    }

    private NumberPath<Long> sortLikeReviewId() {
        return Expressions.numberPath(Long.class, sortReviewLike, "reviewId");
    }

    private NumberPath<Long> imageReviewId() {
        return Expressions.numberPath(Long.class, reviewImageJpaEntity, "reviewId");
    }

    private NumberPath<Long> imageImageFileId() {
        return Expressions.numberPath(Long.class, reviewImageJpaEntity, "imageFileId");
    }

    private NumberPath<Long> subImageReviewId() {
        return Expressions.numberPath(Long.class, subReviewImage, "reviewId");
    }

    private NumberPath<Long> likeReviewId() {
        return Expressions.numberPath(Long.class, reviewLikeJpaEntity, "reviewId");
    }

    private NumberPath<Long> commentReviewId() {
        return Expressions.numberPath(Long.class, reviewCommentJpaEntity, "reviewId");
    }

    private NumberPath<Long> replyCommentId() {
        return Expressions.numberPath(Long.class, reviewReplyJpaEntity, "commentId");
    }

    private NumberPath<Long> tagReviewId() {
        return Expressions.numberPath(Long.class, reviewTagJpaEntity, "reviewId");
    }

    private NumberPath<Long> tagTagId() {
        return Expressions.numberPath(Long.class, reviewTagJpaEntity, "tagId");
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
