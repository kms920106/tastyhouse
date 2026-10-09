package com.tastyhouse.infrastructure.persistence.shop.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.ProductSimpleResult;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.EditorChoiceResult;
import com.tastyhouse.application.shop.port.out.ShopChoiceDetailResult;
import com.tastyhouse.application.shop.port.out.ShopChoiceManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopChoiceQueryPort;
import com.tastyhouse.application.shop.port.out.StationResult;
import com.tastyhouse.application.shop.port.out.TagResult;
import com.tastyhouse.infrastructure.persistence.file.query.FileUrlResolver;
import com.tastyhouse.infrastructure.persistence.product.persistence.QProductImageJpaEntity;

import static com.tastyhouse.infrastructure.persistence.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.persistence.product.persistence.QProductImageJpaEntity.productImageJpaEntity;
import static com.tastyhouse.infrastructure.persistence.product.persistence.QProductJpaEntity.productJpaEntity;
import static com.tastyhouse.infrastructure.persistence.product.persistence.QProductShopLinkJpaEntity.productShopLinkJpaEntity;
import static com.tastyhouse.infrastructure.persistence.shop.persistence.QShopChoiceJpaEntity.shopChoiceJpaEntity;
import static com.tastyhouse.infrastructure.persistence.shop.persistence.QShopJpaEntity.shopJpaEntity;
import static com.tastyhouse.infrastructure.persistence.shop.persistence.QStationJpaEntity.stationJpaEntity;
import static com.tastyhouse.infrastructure.persistence.shop.persistence.QTagJpaEntity.tagJpaEntity;

@Repository
class ShopChoiceQueryAdapter implements ShopChoiceQueryPort, ShopChoiceManagementQueryPort {

    private static final QProductImageJpaEntity subProductImage = new QProductImageJpaEntity("subProductImage");

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ShopChoiceQueryAdapter(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
    public PageResult<EditorChoiceResult> findEditorChoices(PageQuery pageQuery, int productLimit) {
        Long totalCount = queryFactory
            .select(shopChoiceJpaEntity.count())
            .from(shopChoiceJpaEntity)
            .fetchOne();

        if (totalCount == null || totalCount == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ShopChoiceRow> shopChoices = queryFactory
            .select(Projections.constructor(ShopChoiceRow.class,
                shopChoiceJpaEntity.id,
                shopChoiceJpaEntity.shopId,
                shopJpaEntity.name,
                shopChoiceJpaEntity.title,
                shopChoiceJpaEntity.content,
                fileUrlResolver.urlOf(uploadedFileJpaEntity.filePath)
            ))
            .from(shopChoiceJpaEntity)
            .innerJoin(shopJpaEntity).on(shopJpaEntity.id.eq(shopChoiceJpaEntity.shopId)
                .and(shopJpaEntity.permanentlyClosed.eq(false))
                .and(shopJpaEntity.hidden.eq(false)))
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopJpaEntity.thumbnailImageFileId))
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        List<Long> shopIds = shopChoices.stream()
            .map(ShopChoiceRow::shopId)
            .distinct()
            .toList();

        Map<Long, List<ProductSimpleResult>> productsByShopId = productsByShopId(shopIds, productLimit);

        List<EditorChoiceResult> content = shopChoices.stream()
            .map(row -> {
                Long shopIdValue = row.shopId();
                List<ProductSimpleResult> products = productsByShopId.getOrDefault(shopIdValue, new ArrayList<>());
                return new EditorChoiceResult(
                    row.id(),
                    shopIdValue,
                    row.name(),
                    row.title(),
                    row.content(),
                    row.imageUrl(),
                    products
                );
            })
            .toList();

        return PageResult.of(content, totalCount, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<ShopChoiceDetailResult> findShopChoiceById(Long id) {
        ShopChoiceDetailResult result = queryFactory
            .select(Projections.constructor(ShopChoiceDetailResult.class,
                shopChoiceJpaEntity.id,
                shopChoiceJpaEntity.shopId,
                shopChoiceJpaEntity.title,
                shopChoiceJpaEntity.content
            ))
            .from(shopChoiceJpaEntity)
            .where(shopChoiceJpaEntity.id.eq(id))
            .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public List<TagResult> findAllTags() {
        return queryFactory
            .select(Projections.constructor(TagResult.class,
                tagJpaEntity.id,
                tagJpaEntity.tagName
            ))
            .from(tagJpaEntity)
            .orderBy(tagJpaEntity.id.desc())
            .fetch();
    }

    @Override
    public List<StationResult> findAllStations() {
        return queryFactory
            .select(Projections.constructor(StationResult.class,
                stationJpaEntity.id,
                stationJpaEntity.stationName
            ))
            .from(stationJpaEntity)
            .orderBy(stationJpaEntity.stationName.asc())
            .fetch();
    }

    private Map<Long, List<ProductSimpleResult>> productsByShopId(List<Long> shopIds, int productLimit) {
        if (shopIds.isEmpty()) {
            return Map.of();
        }

        ConstructorExpression<ProductSimpleResult> productProjection = Projections.constructor(
            ProductSimpleResult.class,
            productJpaEntity.id,
            shopJpaEntity.name,
            productJpaEntity.name,
            fileUrlResolver.urlOf(uploadedFileJpaEntity.filePath),
            productJpaEntity.originalPrice,
            productJpaEntity.discountInfo.discountPrice,
            productJpaEntity.discountInfo.discountRate
        );

        List<ShopChoiceProductRow> productRows = queryFactory
            .select(Projections.constructor(ShopChoiceProductRow.class, productShopLinkJpaEntity.shopId, productProjection))
            .from(productShopLinkJpaEntity)
            .innerJoin(productJpaEntity).on(productJpaEntity.id.eq(productShopLinkJpaEntity.productId))
            .innerJoin(shopJpaEntity).on(shopJpaEntity.id.eq(productShopLinkJpaEntity.shopId))
            .leftJoin(productImageJpaEntity).on(
                productImageJpaEntity.productId.eq(productJpaEntity.id)
                    .and(productImageJpaEntity.visible.eq(true))
                    .and(productImageJpaEntity.sort.eq(
                        JPAExpressions
                            .select(subProductImage.sort.min())
                            .from(subProductImage)
                            .where(subProductImage.productId.eq(productJpaEntity.id)
                                .and(subProductImage.visible.eq(true)))
                    ))
            )
            .leftJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(productShopLinkJpaEntity.shopId.in(shopIds), productJpaEntity.deleted.isFalse())
            .fetch();

        return productRows.stream()
            .filter(row -> row.shopId() != null)
            .collect(Collectors.groupingBy(
                row -> Objects.requireNonNull(row.shopId()),
                Collectors.mapping(
                    row -> Objects.requireNonNull(row.product()),
                    Collectors.toList()
                )
            ))
            .entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream().limit(productLimit).toList()
            ));
    }
}
