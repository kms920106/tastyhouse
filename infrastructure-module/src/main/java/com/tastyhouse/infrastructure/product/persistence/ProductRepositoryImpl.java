package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.core.domain.product.domain.model.Product;
import com.tastyhouse.core.domain.product.domain.repository.ProductRepository;
import com.tastyhouse.core.domain.product.domain.vo.ProductId;
import com.tastyhouse.core.domain.product.application.dto.ProductSearchCondition;
import com.tastyhouse.core.domain.product.application.dto.result.ProductListItemResult;
import com.tastyhouse.core.domain.product.application.dto.result.QProductListItemResult;
import com.tastyhouse.core.domain.product.application.dto.result.QTodayDiscountProductResult;
import com.tastyhouse.core.domain.product.application.dto.result.SearchProductItemResult;
import com.tastyhouse.core.domain.product.application.dto.result.TodayDiscountProductResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductImageJpaEntity.productImageJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductJpaEntity.productJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private static final QProductImageJpaEntity subProductImage = new QProductImageJpaEntity("subProductImage");

    private final JPAQueryFactory queryFactory;
    private final ProductJpaRepository productJpaRepository;

    @Override
    public PageResult<TodayDiscountProductResult> findTodayDiscountProducts(PageQuery pageQuery) {
        JPAQuery<TodayDiscountProductResult> query = queryFactory
            .select(new QTodayDiscountProductResult(
                productJpaEntity.id,
                shopJpaEntity.name,
                productJpaEntity.name,
                uploadedFileJpaEntity.filePath,
                productJpaEntity.originalPrice,
                productJpaEntity.discountInfo.discountPrice,
                productJpaEntity.discountInfo.discountRate
            ))
            .from(productJpaEntity)
            .innerJoin(shopJpaEntity).on(productJpaEntity.shopId.eq(shopJpaEntity.id))
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
            .where(productJpaEntity.discountInfo.discountPrice.isNotNull()
                .and(productJpaEntity.visible.eq(true)))
            .orderBy(productJpaEntity.discountInfo.discountRate.desc());

        long total = query.fetch().size();

        List<TodayDiscountProductResult> products = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(products, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<SearchProductItemResult> searchByKeyword(String keyword, PageQuery pageQuery) {
        Long total = queryFactory
            .select(productJpaEntity.count())
            .from(productJpaEntity)
            .innerJoin(shopJpaEntity).on(productJpaEntity.shopId.eq(shopJpaEntity.id))
            .where(
                productJpaEntity.name.containsIgnoreCase(keyword)
                    .and(productJpaEntity.visible.eq(true))
                    .and(productJpaEntity.soldOut.eq(false))
                    .and(shopJpaEntity.permanentlyClosed.eq(false))
                    .and(shopJpaEntity.hidden.eq(false))
            )
            .fetchOne();

        if (total == null || total == 0) return PageResult.empty(pageQuery.page(), pageQuery.size());

        List<SearchProductItemResult> content = queryFactory
            .select(Projections.constructor(SearchProductItemResult.class,
                productJpaEntity.id,
                shopJpaEntity.name,
                productJpaEntity.name,
                uploadedFileJpaEntity.filePath,
                productJpaEntity.originalPrice,
                productJpaEntity.discountInfo.discountPrice,
                productJpaEntity.discountInfo.discountRate,
                productJpaEntity.rating,
                productJpaEntity.reviewCount,
                productJpaEntity.representative,
                productJpaEntity.spiciness
            ))
            .from(productJpaEntity)
            .innerJoin(shopJpaEntity).on(productJpaEntity.shopId.eq(shopJpaEntity.id))
            .leftJoin(productImageJpaEntity).on(
                productImageJpaEntity.productId.eq(productJpaEntity.id)
                    .and(productImageJpaEntity.visible.eq(true))
                    .and(productImageJpaEntity.sort.eq(
                        JPAExpressions.select(subProductImage.sort.min())
                            .from(subProductImage)
                            .where(subProductImage.productId.eq(productJpaEntity.id)
                                .and(subProductImage.visible.eq(true)))
                    ))
            )
            .leftJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(
                productJpaEntity.name.containsIgnoreCase(keyword)
                    .and(productJpaEntity.visible.eq(true))
                    .and(productJpaEntity.soldOut.eq(false))
                    .and(shopJpaEntity.permanentlyClosed.eq(false))
                    .and(shopJpaEntity.hidden.eq(false))
            )
            .orderBy(productJpaEntity.representative.desc().nullsLast(), productJpaEntity.rating.desc().nullsLast())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<ProductListItemResult> findProducts(ProductSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(productJpaEntity.count())
            .from(productJpaEntity)
            .where(
                shopIdEq(condition.shopId()),
                categoryIdEq(condition.productCategoryId()),
                nameContains(condition.name()),
                visibleEq(condition.visible()),
                soldOutEq(condition.soldOut())
            )
            .fetchOne();

        if (total == null || total == 0) return PageResult.empty(pageQuery.page(), pageQuery.size());

        List<ProductListItemResult> content = queryFactory
            .select(new QProductListItemResult(
                productJpaEntity.id,
                shopJpaEntity.name,
                productJpaEntity.name,
                productJpaEntity.originalPrice,
                productJpaEntity.discountInfo.discountPrice,
                productJpaEntity.discountInfo.discountRate,
                productJpaEntity.representative,
                productJpaEntity.soldOut,
                productJpaEntity.visible,
                productJpaEntity.sort
            ))
            .from(productJpaEntity)
            .innerJoin(shopJpaEntity).on(productJpaEntity.shopId.eq(shopJpaEntity.id))
            .where(
                shopIdEq(condition.shopId()),
                categoryIdEq(condition.productCategoryId()),
                nameContains(condition.name()),
                visibleEq(condition.visible()),
                soldOutEq(condition.soldOut())
            )
            .orderBy(productJpaEntity.sort.asc(), productJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    private BooleanExpression shopIdEq(Long shopId) {
        return shopId != null ? productJpaEntity.shopId.eq(shopId) : null;
    }

    private BooleanExpression categoryIdEq(Long productCategoryId) {
        return productCategoryId != null ? productJpaEntity.productCategoryId.eq(productCategoryId) : null;
    }

    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? productJpaEntity.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression visibleEq(Boolean visible) {
        return visible != null ? productJpaEntity.visible.eq(visible) : null;
    }

    private BooleanExpression soldOutEq(Boolean soldOut) {
        return soldOut != null ? productJpaEntity.soldOut.eq(soldOut) : null;
    }

    @Override
    public List<Product> findActiveByShopIdOrderByRepresentativeAndRating(Long shopId) {
        return queryFactory
            .selectFrom(productJpaEntity)
            .where(productJpaEntity.shopId.eq(shopId), productJpaEntity.visible.eq(true))
            .orderBy(productJpaEntity.representative.desc(), productJpaEntity.rating.desc(), productJpaEntity.id.asc())
            .fetch()
            .stream()
            .map(ProductMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return productJpaRepository.findById(id.value()).map(ProductMapper::toDomain);
    }

    @Override
    public List<Product> findAllByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(productJpaEntity)
            .where(productJpaEntity.id.in(ids), productJpaEntity.visible.eq(true))
            .fetch()
            .stream()
            .map(ProductMapper::toDomain)
            .toList();
    }

    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            ProductJpaEntity saved = productJpaRepository.save(ProductMapper.toEntity(product));
            return ProductMapper.toDomain(saved);
        }

        ProductJpaEntity entity = productJpaRepository.findById(product.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품입니다: " + product.getId()));
        ProductMapper.applyChanges(entity, product);
        return ProductMapper.toDomain(entity);
    }
}
