package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentManagementQueryPort;
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

@Repository
public class ShopDeliveryAreaAdjustmentQueryDao implements ShopDeliveryAreaAdjustmentQueryPort, ShopDeliveryAreaAdjustmentManagementQueryPort {
    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ShopDeliveryAreaAdjustmentQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

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
