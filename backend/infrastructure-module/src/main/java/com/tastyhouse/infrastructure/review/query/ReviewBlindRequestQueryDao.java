package com.tastyhouse.infrastructure.review.query;

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
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity.memberJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewBlindRequestJpaEntity.reviewBlindRequestJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewImageJpaEntity.reviewImageJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewJpaEntity.reviewJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;

/**
 * 관리자 게시중단 요청 심사 read 어댑터(CQRS query 측).
 *
 * <p>점주용 {@code ShopReviewManagementQueryDao}와 분리한다 — 이쪽은 가게 스코프가 아니라 <b>전 가게의
 * 심사 대기 큐</b>이고, 진입 조건이 {@code status}(PENDING)라 인덱스도 다르다
 * ({@code idx_review_blind_request_status}).
 *
 * <p>목록은 요청 테이블을 주 테이블로 삼고 리뷰·가게를 join한다 — 요청 1건당 리뷰·가게가 각각 1건이라
 * 행이 불어나지 않는다.
 */
@Repository
public class ReviewBlindRequestQueryDao {

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ReviewBlindRequestQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    /**
     * 게시중단 요청 목록 — 최신순({@code created_at DESC, id DESC}).
     */
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

    /**
     * 게시중단 요청 심사 상세.
     */
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
                reviewJpaEntity.content,
                reviewJpaEntity.totalRating,
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
        return Optional.of(detail.withReviewImageUrls(findReviewImageUrls(detail.reviewId())));
    }

    /**
     * 대상 리뷰의 사진 URL 목록(정렬값 오름차순). 저장 경로는 {@link FileUrlResolver}로 표시용 URL까지
     * 완성해 돌려준다.
     */
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

    private BooleanExpression statusEq(ApprovalStatus status) {
        return status != null ? reviewBlindRequestJpaEntity.status.eq(status) : null;
    }

    private BooleanExpression reasonEq(ReviewBlindReason reason) {
        return reason != null ? reviewBlindRequestJpaEntity.reason.eq(reason) : null;
    }

    private BooleanExpression createdAtGoe(LocalDate startDate) {
        return startDate != null ? reviewBlindRequestJpaEntity.createdAt.goe(startDate.atStartOfDay()) : null;
    }

    /**
     * 종료일 필터는 <b>다음날 00:00 미만</b>으로 만든다 — 종료일 당일 접수분이 빠지지 않게 하려면
     * 반열림 상한이어야 한다.
     */
    private BooleanExpression createdAtLt(LocalDate endDate) {
        if (endDate == null) {
            return null;
        }
        LocalDateTime until = endDate.plusDays(1).atStartOfDay();
        return reviewBlindRequestJpaEntity.createdAt.lt(until);
    }
}
