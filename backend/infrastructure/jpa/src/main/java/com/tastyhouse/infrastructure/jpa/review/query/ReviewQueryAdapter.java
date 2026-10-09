package com.tastyhouse.infrastructure.jpa.review.query;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.review.port.out.MyReviewListItemResult;
import com.tastyhouse.application.review.port.out.ReviewCommentItemResult;
import com.tastyhouse.application.review.port.out.ReviewDetailResult;
import com.tastyhouse.application.review.port.out.ReviewQueryPort;
import com.tastyhouse.application.review.port.out.ReviewReplyItemResult;
import com.tastyhouse.application.review.port.out.ReviewTagQueryPort;
import com.tastyhouse.application.review.port.out.SearchReviewItemResult;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.infrastructure.jpa.file.query.FileUrlResolver;
import com.tastyhouse.infrastructure.jpa.member.persistence.QMemberJpaEntity;
import com.tastyhouse.infrastructure.jpa.review.persistence.QReviewImageJpaEntity;
import com.tastyhouse.infrastructure.jpa.shared.query.IdStringRow;

import static com.tastyhouse.infrastructure.jpa.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.jpa.member.persistence.QMemberJpaEntity.memberJpaEntity;
import static com.tastyhouse.infrastructure.jpa.order.persistence.QOrderJpaEntity.orderJpaEntity;
import static com.tastyhouse.infrastructure.jpa.review.persistence.QReviewCommentJpaEntity.reviewCommentJpaEntity;
import static com.tastyhouse.infrastructure.jpa.review.persistence.QReviewImageJpaEntity.reviewImageJpaEntity;
import static com.tastyhouse.infrastructure.jpa.review.persistence.QReviewJpaEntity.reviewJpaEntity;
import static com.tastyhouse.infrastructure.jpa.review.persistence.QReviewLikeJpaEntity.reviewLikeJpaEntity;
import static com.tastyhouse.infrastructure.jpa.review.persistence.QReviewOwnerReplyJpaEntity.reviewOwnerReplyJpaEntity;
import static com.tastyhouse.infrastructure.jpa.review.persistence.QReviewReplyJpaEntity.reviewReplyJpaEntity;
import static com.tastyhouse.infrastructure.jpa.review.persistence.QReviewTagJpaEntity.reviewTagJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopJpaEntity.shopJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QStationJpaEntity.stationJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QTagJpaEntity.tagJpaEntity;

@Repository
class ReviewQueryAdapter implements ReviewQueryPort, ReviewTagQueryPort {

    private static final QReviewImageJpaEntity subReviewImage = new QReviewImageJpaEntity("subReviewImage");

    private static final QMemberJpaEntity replyToMember = new QMemberJpaEntity("replyToMember");

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ReviewQueryAdapter(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    private BooleanExpression visibleToViewer(Long viewerMemberId) {
        return viewerMemberId == null
            ? reviewJpaEntity.ownerOnly.isFalse()
            : reviewJpaEntity.ownerOnly.isFalse().or(reviewJpaEntity.memberId.eq(viewerMemberId));
    }

    @Override
    public Optional<ReviewDetailResult> findReviewDetail(Long reviewId, Long viewerMemberId) {
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
                fileUrlResolver.urlOf(uploadedFileJpaEntity.filePath),
                reviewJpaEntity.createdAt,
                reviewJpaEntity.ownerOnly,
                reviewOwnerReplyJpaEntity.content,
                reviewOwnerReplyJpaEntity.createdAt,
                orderJpaEntity.orderMethod.stringValue(),
                reviewJpaEntity.deliveryRating,
                reviewJpaEntity.deliveryComment
            ))
            .from(reviewJpaEntity)
            .innerJoin(shopJpaEntity).on(reviewJpaEntity.shopId.eq(shopJpaEntity.id))
            .innerJoin(stationJpaEntity).on(ReviewQueryPredicates.shopStationId().eq(stationJpaEntity.id))
            .innerJoin(memberJpaEntity).on(reviewJpaEntity.memberId.eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(ReviewQueryPredicates.memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .leftJoin(reviewOwnerReplyJpaEntity)
            .on(reviewOwnerReplyJpaEntity.reviewId.eq(reviewJpaEntity.id))
            .leftJoin(orderJpaEntity).on(reviewJpaEntity.orderId.eq(orderJpaEntity.id))
            .where(
                reviewJpaEntity.id.eq(reviewId),
                reviewJpaEntity.hidden.isFalse(),
                visibleToViewer(viewerMemberId)
            )
            .fetchOne();

        if (result != null) {
            List<String> imageUrls = findImageUrlsByReviewId(reviewId);
            result = result.withImageUrls(imageUrls);
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

        List<MyReviewRow> pagedRows = queryFactory
            .select(Projections.constructor(MyReviewRow.class, reviewJpaEntity.id, reviewJpaEntity.ownerOnly))
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
            .map(MyReviewRow::id)
            .toList();

        Map<Long, String> imageUrlMap = findFirstImageUrlsByReviewIds(pagedReviewIds);

        List<MyReviewListItemResult> reviews = pagedRows.stream()
            .map(row -> {
                Long reviewId = row.id();
                return new MyReviewListItemResult(
                    reviewId,
                    imageUrlMap.get(reviewId),
                    Boolean.TRUE.equals(row.ownerOnly())
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
                ReviewQueryPredicates.visibleToCustomer()
            )
            .orderBy(reviewJpaEntity.createdAt.desc())
            .fetch();

        long total = allReviewIds.size();

        List<Long> pagedReviewIds = queryFactory
            .select(reviewJpaEntity.id)
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.memberId.eq(memberId),
                ReviewQueryPredicates.visibleToCustomer()
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
                .and(ReviewQueryPredicates.visibleToCustomer())
            )
            .fetchOne();

        if (total == null || total == 0) return PageResult.empty(pageQuery.page(), pageQuery.size());

        List<SearchReviewItemResult> content = queryFactory
            .select(Projections.constructor(SearchReviewItemResult.class,
                reviewJpaEntity.id,
                fileUrlResolver.urlOf(uploadedFileJpaEntity.filePath)
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
                .and(ReviewQueryPredicates.visibleToCustomer())
            )
            .orderBy(reviewJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

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
        Long result = queryFactory
            .select(reviewJpaEntity.productId)
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.id.eq(reviewId))
            .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public boolean existsLike(Long reviewId, Long memberId) {
        return queryFactory
            .selectOne()
            .from(reviewLikeJpaEntity)
            .where(
                reviewLikeJpaEntity.reviewId.eq(reviewId),
                reviewLikeJpaEntity.memberId.eq(memberId)
            )
            .fetchFirst() != null;
    }

    @Override
    public List<ReviewCommentItemResult> findComments(Long reviewId) {
        return queryFactory
            .select(Projections.constructor(ReviewCommentItemResult.class,
                reviewCommentJpaEntity.id,
                reviewCommentJpaEntity.reviewId,
                reviewCommentJpaEntity.memberId,
                memberJpaEntity.nickname,
                fileUrlResolver.urlOf(uploadedFileJpaEntity.filePath),
                reviewCommentJpaEntity.content,
                reviewCommentJpaEntity.createdAt
            ))
            .from(reviewCommentJpaEntity)
            .leftJoin(memberJpaEntity)
            .on(reviewCommentJpaEntity.memberId.eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(ReviewQueryPredicates.memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .where(reviewCommentJpaEntity.reviewId.eq(reviewId))
            .orderBy(reviewCommentJpaEntity.createdAt.desc())
            .fetch();
    }

    @Override
    public List<ReviewReplyItemResult> findVisibleReplies(List<Long> commentIds) {
        if (commentIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
            .select(Projections.constructor(ReviewReplyItemResult.class,
                reviewReplyJpaEntity.id,
                reviewReplyJpaEntity.commentId,
                reviewReplyJpaEntity.memberId,
                memberJpaEntity.nickname,
                fileUrlResolver.urlOf(uploadedFileJpaEntity.filePath),
                reviewReplyJpaEntity.replyToMemberId,
                replyToMember.nickname,
                reviewReplyJpaEntity.content,
                reviewReplyJpaEntity.createdAt
            ))
            .from(reviewReplyJpaEntity)
            .leftJoin(memberJpaEntity)
            .on(reviewReplyJpaEntity.memberId.eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(ReviewQueryPredicates.memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .leftJoin(replyToMember)
            .on(reviewReplyJpaEntity.replyToMemberId.eq(replyToMember.id))
            .where(
                reviewReplyJpaEntity.commentId.in(commentIds),
                reviewReplyJpaEntity.hidden.eq(false)
            )
            .orderBy(reviewReplyJpaEntity.createdAt.asc())
            .fetch();
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

        List<IdStringRow> results = queryFactory
            .select(Projections.constructor(IdStringRow.class, reviewImageJpaEntity.reviewId, uploadedFileJpaEntity.filePath))
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
            .filter(row -> row.id() != null && row.value() != null)
            .collect(Collectors.toMap(
                row -> Objects.requireNonNull(row.id()),
                row -> Objects.requireNonNull(row.value()),
                (existing, replacement) -> existing
            ));

        return fileUrlResolver.resolveAll(filePathByReviewId);
    }
}
