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
public class ReviewBlindRequestQueryDao implements ReviewBlindRequestQueryPort, ReviewBlindRequestManagementQueryPort {

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ReviewBlindRequestQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    /**
     * 게시중단 요청 목록 — 최신순({@code created_at DESC, id DESC}).
     */
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

    /**
     * 게시중단 요청 심사 상세.
     */
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

    /**
     * 고객용 게시중단 안내 — 현재 게시중단(APPROVED) 중인 요청 1건과 그 대상 리뷰를 함께 투영한다.
     *
     * <p><b>{@code hidden} 필터를 걸지 않는다</b> — 대상이 애초에 게시중단된 리뷰이므로, 일반 리뷰 상세
     * 조회의 {@code hidden.isFalse()}를 그대로 쓰면 아무것도 나오지 않는다. 대신 <b>작성자 본인 검증은
     * 호출부(서비스)가</b> {@code reviewMemberId}로 수행한다 — 이 DAO는 표현용 투영만 담당한다.
     *
     * <p>{@code status = APPROVED}로 좁히므로 이미 만료·삭제된 건은 자동으로 제외된다(안내할 것이 없다).
     */
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

    /**
     * 증빙 서류 URL 목록(정렬값 오름차순). 저장 경로는 {@link FileUrlResolver}로 표시용 URL까지 완성해
     * 돌려준다 — {@code fileId}를 노출하지 않고 URL만 내리는 응답 규칙 때문이다.
     */
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

    private BooleanExpression statusEq(ReviewBlindStatus status) {
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
