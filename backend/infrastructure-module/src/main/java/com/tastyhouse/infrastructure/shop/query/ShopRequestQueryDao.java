package com.tastyhouse.infrastructure.shop.query;

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
import static com.tastyhouse.infrastructure.shop.persistence.QShopDeliveryAreaAdjustmentRequestJpaEntity.shopDeliveryAreaAdjustmentRequestJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopImageChangeRequestJpaEntity.shopImageChangeRequestJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopRequestCommentJpaEntity.shopRequestCommentJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopRequestIndexJpaEntity.shopRequestIndexJpaEntity;

/**
 * 요청처리 현황 read 어댑터(CQRS query 측).
 *
 * <p>목록은 인덱스 테이블 단독으로 조회한다 — 유형별 원본을 UNION하지 않으므로 정렬·페이징·필터가 단일
 * 테이블 인덱스로 해결되고, 유형이 늘어도 이 코드는 그대로다. 진입 인덱스는
 * {@code (shop_id, created_at)}이며 기본 정렬 {@code created_at DESC, id DESC}가 이를 그대로 탄다.
 *
 * <p><b>상세는 인덱스와 원본을 함께 읽는다.</b> 인덱스에서 {@code requestType}/{@code sourceRequestId}를
 * 얻어 유형별 원본을 별도 투영하며, 상태·반려 사유는 <b>원본 값</b>으로 응답한다(인덱스는 파생 읽기모델).
 *
 * <p>날짜 필터는 <b>반열림 구간</b> {@code [startDate 00:00, endDate+1일 00:00)}으로 만든다 —
 * {@code DATE(created_at)} 같은 함수를 컬럼에 씌우면 인덱스를 타지 못한다. 조회 기간 상한은 두지 않으므로
 * 변경이력의 보관 하한 이중 안전망에 대응하는 조건이 없다.
 *
 * <p>첨부 URL은 {@code UPLOADED_FILE}을 left join한 뒤 {@link FileUrlResolver}로 완성해 투영한다(응답에
 * {@code ~FileId}를 노출하지 않는 규칙). 목록에서는 join 없이 존재 여부만 담는다.
 */
@Repository
public class ShopRequestQueryDao {

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ShopRequestQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    /**
     * 요청처리 현황 목록 페이징 — 최신순({@code created_at DESC, id DESC}).
     */
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

    /**
     * 요청처리 현황 상세(인덱스 부분). 유형별 원본은 아래 두 메서드가 별도로 투영한다.
     */
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

    /**
     * 이미지 변경요청 원본 투영. 상태·반려 사유의 진실원이라 함께 담는다.
     */
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

    /**
     * 배달지역 조정 신청 원본 투영. 상태·반려 사유의 진실원이라 함께 담는다.
     */
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

    /**
     * 문의 스레드 전체 — <b>작성순</b>({@code created_at ASC, id ASC}).
     *
     * <p>이 저장소의 목록 조회는 대체로 최신순(DESC)인데 여기만 ASC인 것은 이 목록이 <b>대화</b>라서다 —
     * 문의와 답변이 오간 순서대로 읽혀야 한다. 페이징하지 않는 것도 같은 이유로, 요청 1건당 대화량이 적고
     * 화면이 스레드를 통째로 보여준다.
     */
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

    /**
     * 투영된 {@code filePath}를 표시용 URL로 바꿔 Result를 재조립한다. record 재조립은 위치 기반이므로
     * 필드 선언 순서와 인자 순서를 하나씩 대조한다.
     */
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

    /**
     * 요청별 문의 건수 상관 서브쿼리. {@code (shop_request_index_id, id)} 인덱스가 커버하며, 목록 size가
     * 최대 100이라 행마다 실행돼도 비용이 낮다.
     */
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

    /**
     * 종료일 필터는 <b>다음날 00:00 미만</b>으로 만든다 — 종료일 당일에 접수된 요청이 빠지지 않게 하려면
     * {@code loe(endDate.atStartOfDay())}가 아니라 반열림 상한이어야 한다.
     */
    private BooleanExpression createdAtLt(LocalDate endDate) {
        if (endDate == null) {
            return null;
        }
        LocalDateTime until = endDate.plusDays(1).atStartOfDay();
        return shopRequestIndexJpaEntity.createdAt.lt(until);
    }
}
