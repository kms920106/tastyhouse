package com.tastyhouse.infrastructure.review.query;

import com.tastyhouse.application.review.port.out.ReviewBlindRequestManagementQueryPort;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestQueryPort;
import com.tastyhouse.application.review.port.out.ReviewBlindNoticeResult;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestDetailResult;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestListItemResult;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestSearchCondition;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity.memberJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewBlindRequestAttachmentJpaEntity.reviewBlindRequestAttachmentJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewBlindRequestJpaEntity.reviewBlindRequestJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewImageJpaEntity.reviewImageJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewJpaEntity.reviewJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;

@Repository
public class ReviewBlindRequestQueryDao implements ReviewBlindRequestQueryPort, ReviewBlindRequestManagementQueryPort {
    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ReviewBlindRequestQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
    public PageResult<ReviewBlindRequestListItemResult> findBlindRequestPage(
        ReviewBlindRequestSearchCondition condition,
        PageQuery pageQuery
    ) {
        BooleanExpression[] predicates = {
            shopIdEq(condition.shopId()),
            statusEq(condition.status()),
            reasonEq(condition.reason()),
            createdAtGoe(condition.startDate()),
            createdAtLt(condition.endDate()),
        };

        Long total = queryFactory
            .select(reviewBlindRequestJpaEntity.count())
            .from(reviewBlindRequestJpaEntity)
            .where(predicates)
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ReviewBlindRequestListItemResult> content = queryFactory
            .select(Projections.constructor(ReviewBlindRequestListItemResult.class,
                reviewBlindRequestJpaEntity.id,
                reviewBlindRequestJpaEntity.reviewId,
                reviewBlindRequestJpaEntity.shopId,
                shopJpaEntity.name,
                reviewBlindRequestJpaEntity.reason,
                reviewBlindRequestJpaEntity.status,
                reviewBlindRequestJpaEntity.blindUntil,
                reviewJpaEntity.content,
                reviewJpaEntity.totalRating,
                reviewBlindRequestJpaEntity.createdAt
            ))
            .from(reviewBlindRequestJpaEntity)
            .leftJoin(reviewJpaEntity).on(reviewJpaEntity.id.eq(reviewBlindRequestJpaEntity.reviewId))
            .leftJoin(shopJpaEntity).on(shopJpaEntity.id.eq(reviewBlindRequestJpaEntity.shopId))
            .where(predicates)
            .orderBy(reviewBlindRequestJpaEntity.createdAt.desc(), reviewBlindRequestJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<ReviewBlindRequestDetailResult> findBlindRequestDetail(Long id) {
        ReviewBlindRequestDetailResult detail = queryFactory
            .select(Projections.constructor(ReviewBlindRequestDetailResult.class,
                reviewBlindRequestJpaEntity.id,
                reviewBlindRequestJpaEntity.reviewId,
                reviewBlindRequestJpaEntity.shopId,
                shopJpaEntity.name,
                reviewBlindRequestJpaEntity.reason,
                reviewBlindRequestJpaEntity.detailReason,
                reviewBlindRequestJpaEntity.status,
                reviewBlindRequestJpaEntity.rejectReason,
                reviewBlindRequestJpaEntity.blindUntil,
                reviewJpaEntity.content,
                reviewJpaEntity.totalRating,
                Expressions.constant(List.<String>of()),
                Expressions.constant(List.<String>of()),
                memberJpaEntity.nickname,
                reviewJpaEntity.hidden,
                reviewJpaEntity.createdAt,
                reviewBlindRequestJpaEntity.createdAt
            ))
            .from(reviewBlindRequestJpaEntity)
            .leftJoin(reviewJpaEntity).on(reviewJpaEntity.id.eq(reviewBlindRequestJpaEntity.reviewId))
            .leftJoin(shopJpaEntity).on(shopJpaEntity.id.eq(reviewBlindRequestJpaEntity.shopId))
            .leftJoin(memberJpaEntity).on(memberJpaEntity.id.eq(reviewJpaEntity.memberId))
            .where(reviewBlindRequestJpaEntity.id.eq(id))
            .fetchOne();

        if (detail == null) {
            return Optional.empty();
        }
        return Optional.of(detail.withUrls(
            findReviewImageUrls(detail.reviewId()),
            findAttachmentUrls(detail.id())
        ));
    }

    @Override
    public Optional<ReviewBlindNoticeResult> findBlindNotice(Long reviewId) {
        ReviewBlindNoticeResult notice = queryFactory
            .select(Projections.constructor(ReviewBlindNoticeResult.class,
                reviewJpaEntity.id,
                reviewJpaEntity.content,
                Expressions.constant(List.<String>of()),
                reviewJpaEntity.createdAt,
                shopJpaEntity.name,
                reviewBlindRequestJpaEntity.reason,
                reviewBlindRequestJpaEntity.detailReason,
                reviewBlindRequestJpaEntity.blindUntil,
                reviewJpaEntity.memberId
            ))
            .from(reviewBlindRequestJpaEntity)
            .innerJoin(reviewJpaEntity).on(reviewJpaEntity.id.eq(reviewBlindRequestJpaEntity.reviewId))
            .leftJoin(shopJpaEntity).on(shopJpaEntity.id.eq(reviewBlindRequestJpaEntity.shopId))
            .where(
                reviewBlindRequestJpaEntity.reviewId.eq(reviewId),
                reviewBlindRequestJpaEntity.status.eq(ReviewBlindStatus.APPROVED)
            )
            .orderBy(reviewBlindRequestJpaEntity.id.desc())
            .fetchFirst();

        if (notice == null) {
            return Optional.empty();
        }
        return Optional.of(notice.withImageUrls(findReviewImageUrls(notice.reviewId())));
    }

    private List<String> findAttachmentUrls(Long blindRequestId) {
        if (blindRequestId == null) {
            return List.of();
        }
        List<String> filePaths = queryFactory
            .select(uploadedFileJpaEntity.filePath)
            .from(reviewBlindRequestAttachmentJpaEntity)
            .innerJoin(uploadedFileJpaEntity)
            .on(uploadedFileJpaEntity.id.eq(reviewBlindRequestAttachmentJpaEntity.attachmentFileId))
            .where(reviewBlindRequestAttachmentJpaEntity.blindRequestId.eq(blindRequestId))
            .orderBy(reviewBlindRequestAttachmentJpaEntity.sort.asc())
            .fetch();

        return fileUrlResolver.resolveAll(filePaths).stream()
            .filter(Objects::nonNull)
            .toList();
    }

    private List<String> findReviewImageUrls(Long reviewId) {
        if (reviewId == null) {
            return List.of();
        }
        List<String> filePaths = queryFactory
            .select(uploadedFileJpaEntity.filePath)
            .from(reviewImageJpaEntity)
            .innerJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(reviewImageJpaEntity.imageFileId))
            .where(reviewImageJpaEntity.reviewId.eq(reviewId))
            .orderBy(reviewImageJpaEntity.sort.asc())
            .fetch();

        return fileUrlResolver.resolveAll(filePaths).stream()
            .filter(Objects::nonNull)
            .toList();
    }

    private BooleanExpression shopIdEq(Long shopId) {
        return shopId != null ? reviewBlindRequestJpaEntity.shopId.eq(shopId) : null;
    }

    private BooleanExpression statusEq(ReviewBlindStatus status) {
        return status != null ? reviewBlindRequestJpaEntity.status.eq(status) : null;
    }

    private BooleanExpression reasonEq(ReviewBlindReason reason) {
        return reason != null ? reviewBlindRequestJpaEntity.reason.eq(reason) : null;
    }

    private BooleanExpression createdAtGoe(LocalDate startDate) {
        return startDate != null ? reviewBlindRequestJpaEntity.createdAt.goe(startDate.atStartOfDay()) : null;
    }

    private BooleanExpression createdAtLt(LocalDate endDate) {
        if (endDate == null) {
            return null;
        }
        LocalDateTime until = endDate.plusDays(1).atStartOfDay();
        return reviewBlindRequestJpaEntity.createdAt.lt(until);
    }
}
