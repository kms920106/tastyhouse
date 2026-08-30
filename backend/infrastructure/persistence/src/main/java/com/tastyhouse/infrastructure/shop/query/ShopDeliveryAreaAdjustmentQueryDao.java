package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentQueryPort;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentDetailResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentListItemResult;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopDeliveryAreaAdjustmentRequestJpaEntity.shopDeliveryAreaAdjustmentRequestJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;

/**
 * 프랜차이즈 배달지역 조정 신청 read 어댑터(CQRS query 측).
 *
 * <p>{@code ShopQueryDao}에 합치지 않고 독립 DAO로 둔다 — 그 DAO는 이미 가게 설정 전반과 이미지 변경요청
 * 까지 담아 비대하므로, 배달 기능군의 {@code ShopDeliveryAreaQueryDao} 선례를 따른다.
 *
 * <p>동의서 파일은 여기서 {@code UPLOADED_FILE}을 left join하고 {@link FileUrlResolver}로 표시용 URL까지
 * 완성해 투영한다 — 소비 Service가 fileId로 재조회하지 않으며 응답에 {@code ~FileId}가 노출되지 않는다.
 */
@Repository
public class ShopDeliveryAreaAdjustmentQueryDao implements ShopDeliveryAreaAdjustmentQueryPort {

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ShopDeliveryAreaAdjustmentQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    /**
     * 가게의 조정 신청 이력 — 최근 신청 순. 가게당 건수가 적고 화면이 시트 안 목록이라 페이징하지 않는다.
     */
    @Override
    public List<ShopDeliveryAreaAdjustmentListItemResult> findAdjustmentRequests(Long shopId) {
        return listItemProjection()
            .where(shopDeliveryAreaAdjustmentRequestJpaEntity.shopId.eq(shopId))
            .orderBy(shopDeliveryAreaAdjustmentRequestJpaEntity.id.desc())
            .fetch()
            .stream()
            .map(this::withResolvedConsentFileUrl)
            .toList();
    }

    /**
     * 조정 신청 목록 페이징(검수 화면) — 상태·가게로 필터하며, 최근 신청 순.
     */
    @Override
    public PageResult<ShopDeliveryAreaAdjustmentListItemResult> findAdjustmentRequestPage(
        DeliveryAreaAdjustmentStatus status,
        Long shopId,
        PageQuery pageQuery
    ) {
        Long total = queryFactory
            .select(shopDeliveryAreaAdjustmentRequestJpaEntity.count())
            .from(shopDeliveryAreaAdjustmentRequestJpaEntity)
            .where(statusEq(status), shopIdEq(shopId))
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ShopDeliveryAreaAdjustmentListItemResult> content = listItemProjection()
            .where(statusEq(status), shopIdEq(shopId))
            .orderBy(shopDeliveryAreaAdjustmentRequestJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedConsentFileUrl)
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    /**
     * 조정 신청 상세(검수 화면).
     */
    @Override
    public Optional<ShopDeliveryAreaAdjustmentDetailResult> findAdjustmentRequestById(Long requestId) {
        ShopDeliveryAreaAdjustmentDetailResult detail = queryFactory
            .select(Projections.constructor(ShopDeliveryAreaAdjustmentDetailResult.class,
                shopDeliveryAreaAdjustmentRequestJpaEntity.id,
                shopDeliveryAreaAdjustmentRequestJpaEntity.shopId,
                shopJpaEntity.name,
                shopDeliveryAreaAdjustmentRequestJpaEntity.counterpartShopName,
                shopDeliveryAreaAdjustmentRequestJpaEntity.counterpartBusinessNumber,
                shopDeliveryAreaAdjustmentRequestJpaEntity.franchiseName,
                shopDeliveryAreaAdjustmentRequestJpaEntity.reason,
                uploadedFileJpaEntity.filePath,
                shopDeliveryAreaAdjustmentRequestJpaEntity.status,
                shopDeliveryAreaAdjustmentRequestJpaEntity.rejectReason,
                shopDeliveryAreaAdjustmentRequestJpaEntity.createdAt,
                shopDeliveryAreaAdjustmentRequestJpaEntity.updatedAt
            ))
            .from(shopDeliveryAreaAdjustmentRequestJpaEntity)
            .leftJoin(shopJpaEntity).on(shopJpaEntity.id.eq(shopDeliveryAreaAdjustmentRequestJpaEntity.shopId))
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopDeliveryAreaAdjustmentRequestJpaEntity.consentFileId))
            .where(shopDeliveryAreaAdjustmentRequestJpaEntity.id.eq(requestId))
            .fetchOne();

        return Optional.ofNullable(detail).map(this::withResolvedConsentFileUrl);
    }

    private JPQLQuery<ShopDeliveryAreaAdjustmentListItemResult> listItemProjection() {
        return queryFactory
            .select(Projections.constructor(ShopDeliveryAreaAdjustmentListItemResult.class,
                shopDeliveryAreaAdjustmentRequestJpaEntity.id,
                shopDeliveryAreaAdjustmentRequestJpaEntity.shopId,
                shopJpaEntity.name,
                shopDeliveryAreaAdjustmentRequestJpaEntity.counterpartShopName,
                shopDeliveryAreaAdjustmentRequestJpaEntity.counterpartBusinessNumber,
                shopDeliveryAreaAdjustmentRequestJpaEntity.franchiseName,
                shopDeliveryAreaAdjustmentRequestJpaEntity.reason,
                uploadedFileJpaEntity.filePath,
                shopDeliveryAreaAdjustmentRequestJpaEntity.status,
                shopDeliveryAreaAdjustmentRequestJpaEntity.rejectReason,
                shopDeliveryAreaAdjustmentRequestJpaEntity.createdAt
            ))
            .from(shopDeliveryAreaAdjustmentRequestJpaEntity)
            .leftJoin(shopJpaEntity).on(shopJpaEntity.id.eq(shopDeliveryAreaAdjustmentRequestJpaEntity.shopId))
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopDeliveryAreaAdjustmentRequestJpaEntity.consentFileId));
    }

    /**
     * 투영된 {@code filePath}를 표시용 URL로 바꿔 Result를 재조립한다. record 재조립은 위치 기반이므로
     * 필드 선언 순서와 인자 순서를 하나씩 대조한다.
     */
    private ShopDeliveryAreaAdjustmentListItemResult withResolvedConsentFileUrl(ShopDeliveryAreaAdjustmentListItemResult row) {
        return new ShopDeliveryAreaAdjustmentListItemResult(
            row.id(),
            row.shopId(),
            row.shopName(),
            row.counterpartShopName(),
            row.counterpartBusinessNumber(),
            row.franchiseName(),
            row.reason(),
            fileUrlResolver.resolve(row.consentFileUrl()),
            row.status(),
            row.rejectReason(),
            row.createdAt()
        );
    }

    private ShopDeliveryAreaAdjustmentDetailResult withResolvedConsentFileUrl(ShopDeliveryAreaAdjustmentDetailResult row) {
        return new ShopDeliveryAreaAdjustmentDetailResult(
            row.id(),
            row.shopId(),
            row.shopName(),
            row.counterpartShopName(),
            row.counterpartBusinessNumber(),
            row.franchiseName(),
            row.reason(),
            fileUrlResolver.resolve(row.consentFileUrl()),
            row.status(),
            row.rejectReason(),
            row.createdAt(),
            row.updatedAt()
        );
    }

    private BooleanExpression statusEq(DeliveryAreaAdjustmentStatus status) {
        return status != null ? shopDeliveryAreaAdjustmentRequestJpaEntity.status.eq(status) : null;
    }

    private BooleanExpression shopIdEq(Long shopId) {
        return shopId != null ? shopDeliveryAreaAdjustmentRequestJpaEntity.shopId.eq(shopId) : null;
    }
}
