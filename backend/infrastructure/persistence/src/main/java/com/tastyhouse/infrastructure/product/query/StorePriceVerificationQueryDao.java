package com.tastyhouse.infrastructure.product.query;

import com.tastyhouse.application.product.port.out.StorePriceVerificationQueryPort;
import com.tastyhouse.application.product.port.out.StorePriceVerificationItemResult;
import com.tastyhouse.application.product.port.out.StorePriceVerificationListItemResult;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductJpaEntity.productJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductPriceJpaEntity.productPriceJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QStorePriceVerificationItemJpaEntity.storePriceVerificationItemJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QStorePriceVerificationJpaEntity.storePriceVerificationJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;

@Repository
public class StorePriceVerificationQueryDao implements StorePriceVerificationQueryPort {
    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public StorePriceVerificationQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
    public PageResult<StorePriceVerificationListItemResult> findVerificationPage(
        StorePriceVerificationStatus status,
        PageQuery pageQuery
    ) {
        Long total = queryFactory
            .select(storePriceVerificationJpaEntity.count())
            .from(storePriceVerificationJpaEntity)
            .where(statusEq(status))
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<StorePriceVerificationListItemResult> content = verificationProjection()
            .where(statusEq(status))
            .orderBy(storePriceVerificationJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedFileUrl)
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<StorePriceVerificationListItemResult> findVerificationById(Long verificationId) {
        return Optional.ofNullable(
                verificationProjection()
                    .where(storePriceVerificationJpaEntity.id.eq(verificationId))
                    .fetchOne())
            .map(this::withResolvedFileUrl);
    }

    @Override
    public List<StorePriceVerificationItemResult> findVerificationItems(Long verificationId) {
        return queryFactory
            .select(Projections.constructor(StorePriceVerificationItemResult.class,
                storePriceVerificationItemJpaEntity.productId,
                productJpaEntity.name,
                storePriceVerificationItemJpaEntity.productPriceId,
                productPriceJpaEntity.priceName,
                storePriceVerificationItemJpaEntity.storePrice,
                productPriceJpaEntity.deliveryPrice,
                storePriceVerificationItemJpaEntity.applyPickupSamePrice
            ))
            .from(storePriceVerificationItemJpaEntity)
            .leftJoin(productJpaEntity)
            .on(productJpaEntity.id.eq(storePriceVerificationItemJpaEntity.productId))
            .leftJoin(productPriceJpaEntity)
            .on(productPriceJpaEntity.id.eq(storePriceVerificationItemJpaEntity.productPriceId))
            .where(storePriceVerificationItemJpaEntity.verificationId.eq(verificationId))
            .orderBy(storePriceVerificationItemJpaEntity.id.asc())
            .fetch();
    }

    private com.querydsl.jpa.JPQLQuery<StorePriceVerificationListItemResult> verificationProjection() {
        return queryFactory
            .select(Projections.constructor(StorePriceVerificationListItemResult.class,
                storePriceVerificationJpaEntity.id,
                storePriceVerificationJpaEntity.shopId,
                shopJpaEntity.name,
                storePriceVerificationJpaEntity.status,
                uploadedFileJpaEntity.filePath,
                storePriceVerificationJpaEntity.rejectReason,
                JPAExpressions
                    .select(storePriceVerificationItemJpaEntity.count())
                    .from(storePriceVerificationItemJpaEntity)
                    .where(storePriceVerificationItemJpaEntity.verificationId
                        .eq(storePriceVerificationJpaEntity.id)),
                storePriceVerificationJpaEntity.createdAt,
                storePriceVerificationJpaEntity.processedAt
            ))
            .from(storePriceVerificationJpaEntity)
            .leftJoin(shopJpaEntity).on(shopJpaEntity.id.eq(storePriceVerificationJpaEntity.shopId))
            .leftJoin(uploadedFileJpaEntity)
            .on(uploadedFileJpaEntity.id.eq(storePriceVerificationJpaEntity.priceListFileId));
    }

    private BooleanExpression statusEq(StorePriceVerificationStatus status) {
        return status != null ? storePriceVerificationJpaEntity.status.eq(status) : null;
    }

    private StorePriceVerificationListItemResult withResolvedFileUrl(StorePriceVerificationListItemResult row) {
        return new StorePriceVerificationListItemResult(
            row.id(),
            row.shopId(),
            row.shopName(),
            row.status(),
            fileUrlResolver.resolve(row.priceListFileUrl()),
            row.rejectReason(),
            row.itemCount(),
            row.requestedAt(),
            row.processedAt()
        );
    }
}
