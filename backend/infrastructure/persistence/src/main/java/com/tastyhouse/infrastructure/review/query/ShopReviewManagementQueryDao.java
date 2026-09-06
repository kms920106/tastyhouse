package com.tastyhouse.infrastructure.review.query;

import com.tastyhouse.application.review.port.out.ShopReviewManagementQueryPort;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestHistoryResult;
import com.tastyhouse.application.review.port.out.ShopReviewManagementDetailResult;
import com.tastyhouse.application.review.port.out.ShopReviewManagementListItemResult;
import com.tastyhouse.application.review.port.out.ShopReviewManagementSearchCondition;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.review.model.ReviewListTab;
import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;
import com.tastyhouse.infrastructure.review.persistence.QReviewBlindRequestJpaEntity;
import com.tastyhouse.infrastructure.review.persistence.QReviewImageJpaEntity;
import com.tastyhouse.infrastructure.review.persistence.QReviewLikeJpaEntity;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity.memberJpaEntity;
import static com.tastyhouse.infrastructure.order.persistence.QOrderJpaEntity.orderJpaEntity;
import static com.tastyhouse.infrastructure.order.persistence.QOrderProductJpaEntity.orderProductJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewBlindRequestJpaEntity.reviewBlindRequestJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewImageJpaEntity.reviewImageJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewJpaEntity.reviewJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewOwnerReplyJpaEntity.reviewOwnerReplyJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewTagJpaEntity.reviewTagJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QTagJpaEntity.tagJpaEntity;

@Repository
public class ShopReviewManagementQueryDao implements ShopReviewManagementQueryPort {
    private static final QReviewLikeJpaEntity sortReviewLike = new QReviewLikeJpaEntity("sortReviewLike");

    private static final QReviewImageJpaEntity subReviewImage = new QReviewImageJpaEntity("subReviewImage");

    private static final QReviewBlindRequestJpaEntity subBlindRequest =
        new QReviewBlindRequestJpaEntity("subBlindRequest");

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ShopReviewManagementQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
    public PageResult<ShopReviewManagementListItemResult> findShopReviews(
        ShopReviewManagementSearchCondition condition,
        PageQuery pageQuery
    ) {
        BooleanExpression[] predicates = {
            reviewJpaEntity.shopId.eq(condition.shopId()),
            tabPredicate(condition.tab()),
            createdAtGoe(condition.startDate()),
            createdAtLt(condition.endDate()),
            ratingEq(condition.rating()),
            orderMethodEq(condition.orderMethod()),
            hasImageEq(condition.hasImage()),
        };

        Long total = queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .leftJoin(reviewOwnerReplyJpaEntity)
            .on(reviewOwnerReplyJpaEntity.reviewId.eq(reviewJpaEntity.id))
            .leftJoin(orderJpaEntity)
            .on(orderJpaEntity.id.eq(reviewJpaEntity.orderId))
            .where(predicates)
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        JPAQuery<ShopReviewManagementListItemResult> query = queryFactory
            .select(Projections.constructor(ShopReviewManagementListItemResult.class,
                reviewJpaEntity.id,
                memberJpaEntity.nickname,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content,
                Expressions.constant(List.<String>of()),
                Expressions.constant(List.<String>of()),
                orderJpaEntity.orderMethod,
                reviewJpaEntity.hidden,
                reviewJpaEntity.ownerOnly,
                reviewOwnerReplyJpaEntity.content,
                reviewOwnerReplyJpaEntity.createdAt,
                latestBlindRequestStatus(),
                reviewJpaEntity.createdAt
            ))
            .from(reviewJpaEntity)
            .leftJoin(memberJpaEntity)
            .on(memberJpaEntity.id.eq(reviewJpaEntity.memberId))
            .leftJoin(reviewOwnerReplyJpaEntity)
            .on(reviewOwnerReplyJpaEntity.reviewId.eq(reviewJpaEntity.id))
            .leftJoin(orderJpaEntity)
            .on(orderJpaEntity.id.eq(reviewJpaEntity.orderId))
            .where(predicates);

        applySort(query, condition.sortType());

        List<ShopReviewManagementListItemResult> content = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(withCollections(content), total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<ShopReviewManagementDetailResult> findShopReviewDetail(ReviewId reviewId) {
        Long id = reviewId.value();

        ShopReviewManagementDetailResult detail = queryFactory
            .select(Projections.constructor(ShopReviewManagementDetailResult.class,
                reviewJpaEntity.id,
                reviewJpaEntity.shopId,
                memberJpaEntity.nickname,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content,
                Expressions.constant(List.<String>of()),
                Expressions.constant(List.<String>of()),
                orderJpaEntity.orderMethod,
                reviewJpaEntity.hidden,
                reviewJpaEntity.ownerOnly,
                reviewJpaEntity.tasteRating,
                reviewJpaEntity.amountRating,
                reviewJpaEntity.priceRating,
                reviewJpaEntity.atmosphereRating,
                reviewJpaEntity.kindnessRating,
                reviewJpaEntity.hygieneRating,
                reviewJpaEntity.willRevisit,
                Expressions.constant(List.<String>of()),
                reviewOwnerReplyJpaEntity.id,
                reviewOwnerReplyJpaEntity.content,
                reviewOwnerReplyJpaEntity.createdAt,
                reviewOwnerReplyJpaEntity.updatedAt,
                Expressions.constant(List.<ReviewBlindRequestHistoryResult>of()),
                reviewJpaEntity.createdAt,
                reviewJpaEntity.deliveryRating,
                reviewJpaEntity.deliveryComment
            ))
            .from(reviewJpaEntity)
            .leftJoin(memberJpaEntity)
            .on(memberJpaEntity.id.eq(reviewJpaEntity.memberId))
            .leftJoin(reviewOwnerReplyJpaEntity)
            .on(reviewOwnerReplyJpaEntity.reviewId.eq(reviewJpaEntity.id))
            .leftJoin(orderJpaEntity)
            .on(orderJpaEntity.id.eq(reviewJpaEntity.orderId))
            .where(reviewJpaEntity.id.eq(id))
            .fetchOne();

        if (detail == null) {
            return Optional.empty();
        }

        return Optional.of(detail.withCollections(
            findImageUrls(id),
            findProductNames(List.of(id)).getOrDefault(id, List.of()),
            findTagNames(id),
            findBlindRequestHistory(reviewId)
        ));
    }

    @Override
    public List<ReviewBlindRequestHistoryResult> findBlindRequestHistory(ReviewId reviewId) {
        return queryFactory
            .select(Projections.constructor(ReviewBlindRequestHistoryResult.class,
                reviewBlindRequestJpaEntity.id,
                reviewBlindRequestJpaEntity.reason,
                reviewBlindRequestJpaEntity.detailReason,
                reviewBlindRequestJpaEntity.status,
                reviewBlindRequestJpaEntity.rejectReason,
                reviewBlindRequestJpaEntity.blindUntil,
                reviewBlindRequestJpaEntity.createdAt
            ))
            .from(reviewBlindRequestJpaEntity)
            .where(reviewBlindRequestJpaEntity.reviewId.eq(reviewId.value()))
            .orderBy(reviewBlindRequestJpaEntity.createdAt.desc(), reviewBlindRequestJpaEntity.id.desc())
            .fetch();
    }

    private List<ShopReviewManagementListItemResult> withCollections(
        List<ShopReviewManagementListItemResult> content
    ) {
        if (content.isEmpty()) {
            return content;
        }

        List<Long> reviewIds = content.stream().map(ShopReviewManagementListItemResult::id).toList();
        Map<Long, List<String>> imageUrls = findImageUrlsByReviewIds(reviewIds);
        Map<Long, List<String>> productNames = findProductNames(reviewIds);

        return content.stream()
            .map(row -> row
                .withImageUrls(imageUrls.getOrDefault(row.id(), List.of()))
                .withProductNames(productNames.getOrDefault(row.id(), List.of())))
            .toList();
    }

    private Map<Long, List<String>> findImageUrlsByReviewIds(List<Long> reviewIds) {
        List<Tuple> results = queryFactory
            .select(reviewImageJpaEntity.reviewId, uploadedFileJpaEntity.filePath)
            .from(reviewImageJpaEntity)
            .innerJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(reviewImageJpaEntity.imageFileId))
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

    private List<String> findImageUrls(Long reviewId) {
        return findImageUrlsByReviewIds(List.of(reviewId)).getOrDefault(reviewId, List.of());
    }

    private Map<Long, List<String>> findProductNames(List<Long> reviewIds) {
        List<Tuple> results = queryFactory
            .select(reviewJpaEntity.id, orderProductJpaEntity.name)
            .from(reviewJpaEntity)
            .innerJoin(orderProductJpaEntity)
            .on(orderProductJpaEntity.orderId.eq(reviewJpaEntity.orderId))
            .where(reviewJpaEntity.id.in(reviewIds))
            .orderBy(orderProductJpaEntity.id.asc())
            .fetch();

        return results.stream()
            .filter(tuple -> tuple.get(reviewJpaEntity.id) != null)
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(reviewJpaEntity.id)),
                Collectors.mapping(
                    tuple -> Objects.toString(tuple.get(orderProductJpaEntity.name), ""),
                    Collectors.toList()
                )
            ));
    }

    private List<String> findTagNames(Long reviewId) {
        return queryFactory
            .select(tagJpaEntity.tagName)
            .from(reviewTagJpaEntity)
            .innerJoin(tagJpaEntity).on(tagJpaEntity.id.eq(reviewTagJpaEntity.tagId))
            .where(reviewTagJpaEntity.reviewId.eq(reviewId))
            .fetch();
    }

    private void applySort(JPAQuery<ShopReviewManagementListItemResult> query, ReviewSortType sortType) {
        switch (sortType) {
            case RECOMMENDED -> query
                .leftJoin(sortReviewLike).on(sortReviewLike.reviewId.eq(reviewJpaEntity.id))
                .groupBy(
                    reviewJpaEntity.id,
                    memberJpaEntity.nickname,
                    reviewJpaEntity.totalRating,
                    reviewJpaEntity.content,
                    orderJpaEntity.orderMethod,
                    reviewJpaEntity.hidden,
                    reviewJpaEntity.ownerOnly,
                    reviewOwnerReplyJpaEntity.content,
                    reviewOwnerReplyJpaEntity.createdAt,
                    reviewJpaEntity.createdAt
                )
                .orderBy(sortReviewLike.count().desc(), reviewJpaEntity.createdAt.desc());
            case OLDEST -> query.orderBy(reviewJpaEntity.createdAt.asc(), reviewJpaEntity.id.asc());
            case LATEST -> query.orderBy(reviewJpaEntity.createdAt.desc(), reviewJpaEntity.id.desc());
        }
    }

    private Expression<ReviewBlindStatus> latestBlindRequestStatus() {
        return JPAExpressions
            .select(subBlindRequest.status)
            .from(subBlindRequest)
            .where(subBlindRequest.id.eq(
                JPAExpressions
                    .select(reviewBlindRequestJpaEntity.id.max())
                    .from(reviewBlindRequestJpaEntity)
                    .where(reviewBlindRequestJpaEntity.reviewId.eq(reviewJpaEntity.id))
            ));
    }

    private BooleanExpression tabPredicate(ReviewListTab tab) {
        if (tab == null) {
            return null;
        }
        return switch (tab) {
            case ALL -> null;
            case UNANSWERED -> reviewOwnerReplyJpaEntity.id.isNull();
            case BLINDED -> reviewJpaEntity.hidden.isTrue();
            case OWNER_ONLY -> reviewJpaEntity.ownerOnly.isTrue();
        };
    }

    private BooleanExpression createdAtGoe(LocalDate startDate) {
        return startDate != null ? reviewJpaEntity.createdAt.goe(startDate.atStartOfDay()) : null;
    }

    private BooleanExpression createdAtLt(LocalDate endDate) {
        if (endDate == null) {
            return null;
        }
        LocalDateTime until = endDate.plusDays(1).atStartOfDay();
        return reviewJpaEntity.createdAt.lt(until);
    }

    private BooleanExpression ratingEq(Integer rating) {
        return rating != null ? reviewJpaEntity.totalRating.floor().intValue().eq(rating) : null;
    }

    private BooleanExpression orderMethodEq(OrderMethod orderMethod) {
        return orderMethod != null ? orderJpaEntity.orderMethod.eq(orderMethod) : null;
    }

    private BooleanExpression hasImageEq(Boolean hasImage) {
        if (hasImage == null) {
            return null;
        }
        BooleanExpression exists = JPAExpressions
            .selectOne()
            .from(subReviewImage)
            .where(subReviewImage.reviewId.eq(reviewJpaEntity.id))
            .exists();
        return hasImage ? exists : exists.not();
    }
}
