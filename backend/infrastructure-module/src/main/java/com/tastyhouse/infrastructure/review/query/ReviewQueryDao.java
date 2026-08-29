package com.tastyhouse.infrastructure.review.query;

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
public class ReviewQueryDao implements ReviewQueryPort {

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
    private final FileUrlResolver fileUrlResolver;

    public ReviewQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    /**
     * 고객 목록·집계에 노출되는 리뷰 조건 — 숨김(관리자 게시중단)과 사장님만보기를 <b>둘 다</b> 제외한다.
     *
     * <p>목록의 where절과 count절이 분리된 곳에서는 <b>양쪽 모두</b>에 걸어야 한다. 한쪽만 고치면
     * {@code totalElements}와 실제 목록 길이가 어긋나 프론트 무한스크롤이 빈 페이지로 깨진다.
     *
     * <p><b>예외 — {@code findMyReviews}(마이페이지)는 이 헬퍼를 쓰지 않는다.</b> 본인 한정 조회라
     * 자기가 쓴 사장님만보기 리뷰는 보여야 하기 때문이다({@code findReviewsByMemberId}(타인 프로필)와
     * 정책이 정반대이므로 두 메서드를 같이 고치지 말 것).
     */
    private BooleanExpression visibleToCustomer() {
        return reviewJpaEntity.hidden.isFalse().and(reviewJpaEntity.ownerOnly.isFalse());
    }

    /**
     * 리뷰 <b>상세</b>의 뷰어 기준 노출 조건 — 사장님만보기 리뷰는 작성자 본인에게만 보인다.
     *
     * <p>비로그인({@code viewerMemberId == null})이거나 타인이면 사장님만보기 리뷰가 조회되지 않아
     * 호출부가 {@code REVIEW_NOT_FOUND}(404)를 낸다. 403을 쓰지 않는 이유는 403이 "그 리뷰가 존재한다"는
     * 사실을 노출하기 때문이다.
     *
     * <p>조건이 OR이라 varargs {@code .where(a, b)}(AND)로는 표현할 수 없어 {@link BooleanExpression}
     * 헬퍼로 만든다({@code BooleanBuilder}는 프로젝트 금지 규약).
     */
    private BooleanExpression visibleToViewer(Long viewerMemberId) {
        return viewerMemberId == null
            ? reviewJpaEntity.ownerOnly.isFalse()
            : reviewJpaEntity.ownerOnly.isFalse().or(reviewJpaEntity.memberId.eq(viewerMemberId));
    }

    /**
     * 베스트 리뷰 목록 — 총점 높은 순 → 최신순. 대표 이미지(정렬값이 가장 작은 리뷰 이미지)를 함께 투영한다.
     */
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

    /**
     * 최신 리뷰 목록 — 작성 최신순.
     */
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

    /**
     * 팔로잉한 회원들이 쓴 최신 리뷰 목록.
     */
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

    /**
     * 가게별 리뷰 목록 — 평점·이미지 유무 필터와 정렬 방식({@link ReviewSortType})을 지원한다.
     */
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

    /**
     * 상품별 리뷰 목록 — 평점·이미지 유무 필터와 정렬 방식을 지원한다.
     */
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

    /**
     * 가게의 특정 평점대 리뷰를 제한 건수만큼 조회한다(평점별 미리보기).
     */
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

    /**
     * 상품의 특정 평점대 리뷰를 제한 건수만큼 조회한다(평점별 미리보기).
     */
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

    /**
     * 리뷰 상세(숨김 제외 + 사장님만보기는 작성자 본인만) — 이미지 URL을 함께 채운다. 없으면 비어 있다.
     *
     * <p>{@code viewerMemberId}는 <b>선택적</b>이다({@code null} = 비로그인). 사장님만보기 리뷰는
     * 작성자 본인일 때만 조회되며, 그 외에는 비어 있는 결과가 돌아가 호출부가 404를 낸다
     * ({@link #visibleToViewer(Long)} 참고).
     */
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

    /**
     * <b>본인</b>이 쓴 리뷰 목록(마이페이지, 대표 이미지 1장) — 최신순.
     *
     * <p><b>사장님만보기 리뷰를 포함한다</b> — 이미 본인 한정 조회이므로 자기가 비공개로 쓴 리뷰도 보여야
     * 하기 때문이다. 아래 {@link #findReviewsByMemberId}(타인 프로필)와 쿼리가 거의 같지만 정책이
     * <b>정반대</b>이므로, 한쪽을 고칠 때 다른 쪽을 함께 고치지 말 것.
     *
     * <p>단 {@code hidden}(관리자 게시중단) 필터는 유지한다 — 게시중단은 정책 위반 제재라 사장님만보기보다
     * 상위이며, 본인에게도 보이지 않는 것이 올바른 동작이다.
     */
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

        // 뱃지 표시용으로 ownerOnly를 함께 뽑는다(마이페이지는 사장님만보기 리뷰를 포함하므로 구분이 필요).
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

    /**
     * <b>타인</b>(특정 회원) 프로필의 리뷰 목록(대표 이미지 1장) — 최신순.
     *
     * <p><b>사장님만보기 리뷰를 제외한다</b> — 작성자 본인에게만 보이는 리뷰이므로 타인 프로필에서는
     * 보이면 안 된다. 위 {@link #findMyReviews}(마이페이지=본인, 포함)와 쿼리가 거의 같지만 정책이
     * <b>정반대</b>이므로, 한쪽을 고칠 때 다른 쪽을 함께 고치지 말 것.
     */
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

        // 타인 프로필은 visibleToCustomer()로 사장님만보기를 이미 걸렀으므로 뱃지는 항상 false다.
        List<MyReviewListItemResult> reviews = pagedReviewIds.stream()
            .map(reviewId -> new MyReviewListItemResult(reviewId, imageUrlMap.get(reviewId), false))
            .collect(Collectors.toList());

        return PageResult.of(reviews, total, pageQuery.page(), pageQuery.size());
    }

    /**
     * 키워드로 리뷰를 검색한다(이미지가 있는 리뷰만, 대표 이미지 1장).
     */
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

    /**
     * 해당 주문·상품에 대해 그 회원이 이미 리뷰를 썼는지(중복 작성 차단용).
     */
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

    /**
     * 한 주문 안에서 그 회원이 이미 리뷰를 쓴 상품 식별자 집합.
     *
     * <p>주문 상세의 주문상품마다 {@link #existsByOrderIdAndProductIdAndMemberId}를 호출하면 상품 수만큼
     * 쿼리가 나가므로(N+1), 상품 식별자를 모아 {@code IN} 한 번으로 조회하고 소비 모듈이 메모리에서
     * 판정하도록 한다. 입력이 비어 있으면 조회하지 않는다.
     */
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

    /**
     * 리뷰가 가리키는 상품 식별자. 리뷰 상세와 상품 정보를 함께 보여주는 화면에서 쓴다.
     */
    @Override
    public Optional<Long> findProductIdByReviewId(Long reviewId) {
        return Optional.ofNullable(queryFactory
            .select(reviewJpaEntity.productId)
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.id.eq(reviewId))
            .fetchOne());
    }

    /**
     * 회원이 해당 리뷰에 좋아요를 눌렀는지 여부.
     */
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

    /**
     * 리뷰의 댓글 목록(숨김 포함) — 최신순.
     *
     * <p>관리 화면용 {@code ReviewManagementQueryDao#findCommentsIncludingHidden}과 달리 작성자 프로필
     * 이미지 경로까지 함께 투영한다(web 응답이 프로필 이미지 URL을 포함하기 때문). 숨김 댓글도 그대로
     * 내려주는 것은 기존 web 동작을 보존하기 위함이다 — 답글만 숨김을 제외한다.
     */
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

    /**
     * 여러 댓글에 달린 답글 목록(숨김 제외) — 작성순. 입력이 비어 있으면 조회하지 않고 빈 목록을 돌려준다.
     *
     * <p>작성자 프로필 이미지 경로를 함께 투영하며, 답글 대상 회원(replyTo)은 없을 수 있어 leftJoin으로
     * 붙인다.
     */
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

    /**
     * 리뷰에 달린 태그 ID 목록.
     */
    @Override
    public List<Long> findTagIdsByReviewId(Long reviewId) {
        return queryFactory
            .select(reviewTagJpaEntity.tagId)
            .from(reviewTagJpaEntity)
            .where(reviewTagJpaEntity.reviewId.eq(reviewId))
            .fetch();
    }

    /**
     * 태그 ID 목록에 해당하는 태그명 목록. 입력이 비어 있으면 조회하지 않고 빈 목록을 돌려준다.
     */
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

    /**
     * 베스트 리뷰 총 건수.
     *
     * <p>목록 쿼리는 대표 이미지·파일·주문상품을 {@code leftJoin}하지만, 이미지 조인은 "정렬값이 가장 작은
     * 1장"으로, 주문상품 조인은 (orderId, productId) 복합 동등으로 각각 리뷰당 최대 1행으로 좁혀지므로
     * 행이 늘지 않는다. shop·station은 {@code innerJoin}이라 총 건수에 영향을 주므로 그대로 재현한다.
     */
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

    /**
     * {@code LatestReviewListItemResult} 목록 계열(최신·팔로잉·가게별·상품별)의 공통 총 건수.
     *
     * <p>목록 쿼리와 동일한 {@code innerJoin}(shop·station·member)을 재현해야 총 건수가 일치한다 —
     * inner join은 짝이 없는 리뷰를 제외하므로 리뷰 테이블만 세면 값이 달라진다. 반면 프로필 이미지
     * {@code leftJoin}과 좋아요·댓글 수 스칼라 서브쿼리는 행 수를 바꾸지 않아 재현하지 않는다.
     *
     * <p>정렬(특히 추천순의 {@code groupBy})은 총 건수와 무관하므로 count 쿼리에는 적용하지 않는다.
     * 추천순은 {@code groupBy(reviewJpaEntity.id, ...)}로 리뷰당 1행이 되므로 여기서 세는 리뷰 건수와
     * 결과가 같다(기존 {@code fetch().size()}와 등가).
     */
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

    /**
     * 리뷰 목록 정렬 적용 — 가게별·상품별 목록이 공유한다.
     *
     * <p>추천순은 좋아요 수 집계가 필요해 별칭 조인 + {@code groupBy}가 따라붙고, 동수일 때는 최신순으로
     * 갈린다. 정렬 후보는 도메인 enum({@link ReviewSortType})이 소유하며 승격은 소비 모듈 Service가 한다.
     */
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

    /**
     * 여러 리뷰의 이미지 URL을 리뷰 ID별로 묶어 조회한다(정렬값 오름차순). 저장 경로는
     * {@link FileUrlResolver}로 표시용 URL까지 변환한 뒤 돌려준다.
     */
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
     * 여러 리뷰의 대표 이미지(정렬값이 가장 작은 1장) URL을 리뷰 ID별로 조회한다. 저장 경로는
     * {@link FileUrlResolver}로 표시용 URL까지 변환한 뒤 돌려준다.
     */
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

    /**
     * 투영된 저장 경로를 표시용 URL로 바꿔 재조립한다. {@code Projections.constructor}는 생성자 직접 투영이라
     * 변환을 투영식에 넣을 수 없어 fetch 직후 호출한다.
     */
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

    // ----------------------------------------------------- 크로스 도메인 @Convert VO 컬럼 우회

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
