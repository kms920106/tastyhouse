package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.application.shop.port.out.ShopChoiceManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopChoiceQueryPort;
import com.tastyhouse.application.shop.port.out.EditorChoiceResult;
import com.tastyhouse.application.shop.port.out.ShopChoiceDetailResult;
import com.tastyhouse.application.shop.port.out.StationResult;
import com.tastyhouse.application.shop.port.out.TagResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.service.EditorChoicePolicy;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;
import com.tastyhouse.infrastructure.product.persistence.QProductImageJpaEntity;
import com.tastyhouse.application.product.port.out.ProductSimpleResult;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductImageJpaEntity.productImageJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductJpaEntity.productJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductShopLinkJpaEntity.productShopLinkJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopChoiceJpaEntity.shopChoiceJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QStationJpaEntity.stationJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QTagJpaEntity.tagJpaEntity;

@Repository
public class ShopChoiceQueryDao implements ShopChoiceQueryPort, ShopChoiceManagementQueryPort {
    private static final QProductImageJpaEntity subProductImage = new QProductImageJpaEntity("subProductImage");

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ShopChoiceQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
    public PageResult<EditorChoiceResult> findEditorChoices(PageQuery pageQuery) {
        Long totalCount = queryFactory
            .select(shopChoiceJpaEntity.count())
            .from(shopChoiceJpaEntity)
            .fetchOne();

        if (totalCount == null || totalCount == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<Tuple> shopChoices = queryFactory
            .select(
                shopChoiceJpaEntity.id,
                shopChoiceJpaEntity.shopId,
                shopJpaEntity.name,
                shopChoiceJpaEntity.title,
                shopChoiceJpaEntity.content,
                uploadedFileJpaEntity.filePath
            )
            .from(shopChoiceJpaEntity)
            .innerJoin(shopJpaEntity).on(shopJpaEntity.id.eq(shopChoiceJpaEntity.shopId)
                .and(shopJpaEntity.permanentlyClosed.eq(false))
                .and(shopJpaEntity.hidden.eq(false)))
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopJpaEntity.thumbnailImageFileId))
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        List<Long> shopIds = shopChoices.stream()
            .map(tuple -> tuple.get(shopChoiceJpaEntity.shopId))
            .distinct()
            .toList();

        Map<Long, List<ProductSimpleResult>> productsByShopId = productsByShopId(shopIds);

        List<EditorChoiceResult> content = shopChoices.stream()
            .map(tuple -> {
                Long shopIdValue = tuple.get(shopChoiceJpaEntity.shopId);
                List<ProductSimpleResult> products = productsByShopId.getOrDefault(shopIdValue, new ArrayList<>());
                return new EditorChoiceResult(
                    tuple.get(shopChoiceJpaEntity.id),
                    shopIdValue,
                    tuple.get(shopJpaEntity.name),
                    tuple.get(shopChoiceJpaEntity.title),
                    tuple.get(shopChoiceJpaEntity.content),
                    fileUrlResolver.resolve(tuple.get(uploadedFileJpaEntity.filePath)),
                    products
                );
            })
            .toList();

        return PageResult.of(content, totalCount, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<ShopChoiceDetailResult> findShopChoiceById(Long id) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(ShopChoiceDetailResult.class,
                    shopChoiceJpaEntity.id,
                    shopChoiceJpaEntity.shopId,
                    shopChoiceJpaEntity.title,
                    shopChoiceJpaEntity.content
                ))
                .from(shopChoiceJpaEntity)
                .where(shopChoiceJpaEntity.id.eq(id))
                .fetchOne()
        );
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

    private Map<Long, List<ProductSimpleResult>> productsByShopId(List<Long> shopIds) {
        if (shopIds.isEmpty()) {
            return Map.of();
        }

        ConstructorExpression<ProductSimpleResult> productProjection = Projections.constructor(
            ProductSimpleResult.class,
            productJpaEntity.id,
            shopJpaEntity.name,
            productJpaEntity.name,
            uploadedFileJpaEntity.filePath,
            productJpaEntity.originalPrice,
            productJpaEntity.discountInfo.discountPrice,
            productJpaEntity.discountInfo.discountRate
        );

        List<Tuple> productTuples = queryFactory
            .select(productShopLinkJpaEntity.shopId, productProjection)
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

        return productTuples.stream()
            .filter(tuple -> tuple.get(productShopLinkJpaEntity.shopId) != null)
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(productShopLinkJpaEntity.shopId)),
                Collectors.mapping(
                    tuple -> withResolvedImageUrl(Objects.requireNonNull(tuple.get(productProjection))),
                    Collectors.toList()
                )
            ))
            .entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream().limit(EditorChoicePolicy.PRODUCT_LIMIT).toList()
            ));
    }

    private ProductSimpleResult withResolvedImageUrl(ProductSimpleResult row) {
        return new ProductSimpleResult(
            row.id(),
            row.shopName(),
            row.name(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.originalPrice(),
            row.discountPrice(),
            row.discountRate()
        );
    }
}
