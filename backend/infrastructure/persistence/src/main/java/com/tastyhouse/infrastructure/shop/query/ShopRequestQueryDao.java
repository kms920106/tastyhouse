package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.application.shop.port.out.ShopRequestManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopRequestQueryPort;
import com.tastyhouse.application.shop.port.out.ShopRequestAdjustmentDetailResult;
import com.tastyhouse.application.shop.port.out.ShopRequestCommentResult;
import com.tastyhouse.application.shop.port.out.ShopRequestDetailResult;
import com.tastyhouse.application.shop.port.out.ShopRequestImageChangeDetailResult;
import com.tastyhouse.application.shop.port.out.ShopRequestListItemResult;
import com.tastyhouse.application.shop.port.out.ShopRequestReviewBlindDetailResult;
import com.tastyhouse.application.shop.port.out.ShopRequestSearchCondition;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewBlindRequestJpaEntity.reviewBlindRequestJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewJpaEntity.reviewJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopDeliveryAreaAdjustmentRequestJpaEntity.shopDeliveryAreaAdjustmentRequestJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopImageChangeRequestJpaEntity.shopImageChangeRequestJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopRequestCommentJpaEntity.shopRequestCommentJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopRequestIndexJpaEntity.shopRequestIndexJpaEntity;

@Repository
public class ShopRequestQueryDao implements ShopRequestQueryPort, ShopRequestManagementQueryPort {
    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ShopRequestQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
    public PageResult<ShopRequestListItemResult> findRequestPage(
        ShopRequestSearchCondition condition,
        PageQuery pageQuery
    ) {
        BooleanExpression[] predicates = {
            shopRequestIndexJpaEntity.shopId.eq(condition.shopId()),
            requestTypeEq(condition.requestType()),
            statusEq(condition.status()),
            createdAtGoe(condition.startDate()),
            createdAtLt(condition.endDate()),
        };

        Long total = queryFactory
            .select(shopRequestIndexJpaEntity.count())
            .from(shopRequestIndexJpaEntity)
            .where(predicates)
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ShopRequestListItemResult> content = queryFactory
            .select(Projections.constructor(ShopRequestListItemResult.class,
                shopRequestIndexJpaEntity.id,
                shopRequestIndexJpaEntity.requestType,
                shopRequestIndexJpaEntity.summary,
                shopRequestIndexJpaEntity.status,
                shopRequestIndexJpaEntity.rejectReason,
                shopRequestIndexJpaEntity.attachmentFileId.isNotNull(),
                commentCount(),
                shopRequestIndexJpaEntity.createdAt,
                shopRequestIndexJpaEntity.processedAt
            ))
            .from(shopRequestIndexJpaEntity)
            .where(predicates)
            .orderBy(shopRequestIndexJpaEntity.createdAt.desc(), shopRequestIndexJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<ShopRequestDetailResult> findRequestDetail(Long requestId) {
        ShopRequestDetailResult detail = queryFactory
            .select(Projections.constructor(ShopRequestDetailResult.class,
                shopRequestIndexJpaEntity.id,
                shopRequestIndexJpaEntity.shopId,
                shopRequestIndexJpaEntity.requestType,
                shopRequestIndexJpaEntity.sourceRequestId,
                shopRequestIndexJpaEntity.summary,
                shopRequestIndexJpaEntity.status,
                shopRequestIndexJpaEntity.rejectReason,
                uploadedFileJpaEntity.filePath,
                commentCount(),
                shopRequestIndexJpaEntity.createdAt,
                shopRequestIndexJpaEntity.processedAt
            ))
            .from(shopRequestIndexJpaEntity)
            .leftJoin(uploadedFileJpaEntity)
            .on(uploadedFileJpaEntity.id.eq(shopRequestIndexJpaEntity.attachmentFileId))
            .where(shopRequestIndexJpaEntity.id.eq(requestId))
            .fetchOne();

        return Optional.ofNullable(detail).map(this::withResolvedAttachmentUrl);
    }

    @Override
    public Optional<ShopRequestImageChangeDetailResult> findImageChangeDetail(Long sourceRequestId) {
        ShopRequestImageChangeDetailResult detail = queryFactory
            .select(Projections.constructor(ShopRequestImageChangeDetailResult.class,
                shopImageChangeRequestJpaEntity.imageType,
                uploadedFileJpaEntity.filePath,
                shopImageChangeRequestJpaEntity.status,
                shopImageChangeRequestJpaEntity.rejectReason
            ))
            .from(shopImageChangeRequestJpaEntity)
            .leftJoin(uploadedFileJpaEntity)
            .on(uploadedFileJpaEntity.id.eq(shopImageChangeRequestJpaEntity.imageFileId))
            .where(shopImageChangeRequestJpaEntity.id.eq(sourceRequestId))
            .fetchOne();

        return Optional.ofNullable(detail).map(row -> new ShopRequestImageChangeDetailResult(
            row.imageType(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.status(),
            row.rejectReason()
        ));
    }

    @Override
    public Optional<ShopRequestAdjustmentDetailResult> findAdjustmentDetail(Long sourceRequestId) {
        ShopRequestAdjustmentDetailResult detail = queryFactory
            .select(Projections.constructor(ShopRequestAdjustmentDetailResult.class,
                shopDeliveryAreaAdjustmentRequestJpaEntity.counterpartShopName,
                shopDeliveryAreaAdjustmentRequestJpaEntity.counterpartBusinessNumber,
                shopDeliveryAreaAdjustmentRequestJpaEntity.franchiseName,
                shopDeliveryAreaAdjustmentRequestJpaEntity.reason,
                uploadedFileJpaEntity.filePath,
                shopDeliveryAreaAdjustmentRequestJpaEntity.status,
                shopDeliveryAreaAdjustmentRequestJpaEntity.rejectReason
            ))
            .from(shopDeliveryAreaAdjustmentRequestJpaEntity)
            .leftJoin(uploadedFileJpaEntity)
            .on(uploadedFileJpaEntity.id.eq(shopDeliveryAreaAdjustmentRequestJpaEntity.consentFileId))
            .where(shopDeliveryAreaAdjustmentRequestJpaEntity.id.eq(sourceRequestId))
            .fetchOne();

        return Optional.ofNullable(detail).map(row -> new ShopRequestAdjustmentDetailResult(
            row.counterpartShopName(),
            row.counterpartBusinessNumber(),
            row.franchiseName(),
            row.reason(),
            fileUrlResolver.resolve(row.consentFileUrl()),
            row.status(),
            row.rejectReason()
        ));
    }

    @Override
    public Optional<ShopRequestReviewBlindDetailResult> findReviewBlindDetail(Long sourceRequestId) {
        ShopRequestReviewBlindDetailResult detail = queryFactory
            .select(Projections.constructor(ShopRequestReviewBlindDetailResult.class,
                reviewBlindRequestJpaEntity.reviewId,
                reviewBlindRequestJpaEntity.reason,
                reviewBlindRequestJpaEntity.detailReason,
                reviewJpaEntity.content,
                reviewJpaEntity.totalRating,
                reviewBlindRequestJpaEntity.status,
                reviewBlindRequestJpaEntity.rejectReason
            ))
            .from(reviewBlindRequestJpaEntity)
            .leftJoin(reviewJpaEntity).on(reviewJpaEntity.id.eq(reviewBlindRequestJpaEntity.reviewId))
            .where(reviewBlindRequestJpaEntity.id.eq(sourceRequestId))
            .fetchOne();

        return Optional.ofNullable(detail);
    }

    @Override
    public List<ShopRequestCommentResult> findComments(Long requestId) {
        return queryFactory
            .select(Projections.constructor(ShopRequestCommentResult.class,
                shopRequestCommentJpaEntity.id,
                shopRequestCommentJpaEntity.authorType,
                shopRequestCommentJpaEntity.content,
                shopRequestCommentJpaEntity.createdAt
            ))
            .from(shopRequestCommentJpaEntity)
            .where(shopRequestCommentJpaEntity.shopRequestIndexId.eq(requestId))
            .orderBy(shopRequestCommentJpaEntity.createdAt.asc(), shopRequestCommentJpaEntity.id.asc())
            .fetch();
    }

    private ShopRequestDetailResult withResolvedAttachmentUrl(ShopRequestDetailResult row) {
        return new ShopRequestDetailResult(
            row.requestId(),
            row.shopId(),
            row.requestType(),
            row.sourceRequestId(),
            row.summary(),
            row.status(),
            row.rejectReason(),
            fileUrlResolver.resolve(row.attachmentUrl()),
            row.commentCount(),
            row.requestedAt(),
            row.processedAt()
        );
    }

    private Expression<Long> commentCount() {
        return JPAExpressions
            .select(shopRequestCommentJpaEntity.count())
            .from(shopRequestCommentJpaEntity)
            .where(shopRequestCommentJpaEntity.shopRequestIndexId.eq(shopRequestIndexJpaEntity.id));
    }

    private BooleanExpression requestTypeEq(ShopRequestType requestType) {
        return requestType != null ? shopRequestIndexJpaEntity.requestType.eq(requestType) : null;
    }

    private BooleanExpression statusEq(ShopRequestStatus status) {
        return status != null ? shopRequestIndexJpaEntity.status.eq(status) : null;
    }

    private BooleanExpression createdAtGoe(LocalDate startDate) {
        return startDate != null ? shopRequestIndexJpaEntity.createdAt.goe(startDate.atStartOfDay()) : null;
    }

    private BooleanExpression createdAtLt(LocalDate endDate) {
        if (endDate == null) {
            return null;
        }
        LocalDateTime until = endDate.plusDays(1).atStartOfDay();
        return shopRequestIndexJpaEntity.createdAt.lt(until);
    }
}
