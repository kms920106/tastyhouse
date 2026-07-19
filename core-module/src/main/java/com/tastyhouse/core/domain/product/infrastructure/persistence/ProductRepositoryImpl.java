package com.tastyhouse.core.domain.product.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.core.domain.product.domain.model.Product;
import com.tastyhouse.core.domain.product.domain.model.QProductImage;
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

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.domain.product.domain.model.QProduct.product;
import static com.tastyhouse.core.domain.product.domain.model.QProductImage.productImage;

/**
 * {@code shop}은 infrastructure-module로 이동한 {@code ShopJpaEntity}를 가리킨다.
 * core-module은 infrastructure-module을 의존할 수 없어(의존 방향: infrastructure → core)
 * 생성된 Q타입을 import할 수 없으므로, {@link PathBuilder}로 JPA 엔티티명("ShopJpaEntity")을
 * 문자열 참조해 필요한 컬럼만 타입 세이프하게 노출한다.
 */
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private static final QProductImage subProductImage = new QProductImage("subProductImage");

    private static final PathBuilder<Object> shop = new PathBuilder<>(Object.class, "ShopJpaEntity");
    private static final NumberPath<Long> shopIdCol = shop.getNumber("id", Long.class);
    private static final StringPath shopNameCol = shop.getString("name");
    private static final BooleanPath shopPermanentlyClosedCol = shop.getBoolean("permanentlyClosed");

    private final JPAQueryFactory queryFactory;
    private final ProductJpaRepository productJpaRepository;

    @Override
    public PageResult<TodayDiscountProductResult> findTodayDiscountProducts(PageQuery pageQuery) {
        JPAQuery<TodayDiscountProductResult> query = queryFactory
            .select(new QTodayDiscountProductResult(
                product.id,
                shopNameCol,
                product.name,
                uploadedFile.filePath,
                product.originalPrice,
                product.discountInfo.discountPrice,
                product.discountInfo.discountRate
            ))
            .from(product)
            .innerJoin(shop).on(product.shopId.eq(shopIdCol))
            .leftJoin(productImage).on(
                productImage.productId.eq(product.id)
                    .and(productImage.visible.eq(true))
                    .and(productImage.sort.eq(
                        JPAExpressions
                            .select(subProductImage.sort.min())
                            .from(subProductImage)
                            .where(subProductImage.productId.eq(product.id)
                                .and(subProductImage.visible.eq(true)))
                    ))
            )
            .leftJoin(uploadedFile).on(productImage.imageFileId.eq(uploadedFile.id))
            .where(product.discountInfo.discountPrice.isNotNull()
                .and(product.visible.eq(true)))
            .orderBy(product.discountInfo.discountRate.desc());

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
            .select(product.count())
            .from(product)
            .innerJoin(shop).on(product.shopId.eq(shopIdCol))
            .where(
                product.name.containsIgnoreCase(keyword)
                    .and(product.visible.eq(true))
                    .and(product.soldOut.eq(false))
                    .and(shopPermanentlyClosedCol.eq(false))
            )
            .fetchOne();

        if (total == null || total == 0) return PageResult.empty(pageQuery.page(), pageQuery.size());

        List<SearchProductItemResult> content = queryFactory
            .select(Projections.constructor(SearchProductItemResult.class,
                product.id,
                shopNameCol,
                product.name,
                uploadedFile.filePath,
                product.originalPrice,
                product.discountInfo.discountPrice,
                product.discountInfo.discountRate,
                product.rating,
                product.reviewCount,
                product.representative,
                product.spiciness
            ))
            .from(product)
            .innerJoin(shop).on(product.shopId.eq(shopIdCol))
            .leftJoin(productImage).on(
                productImage.productId.eq(product.id)
                    .and(productImage.visible.eq(true))
                    .and(productImage.sort.eq(
                        JPAExpressions.select(subProductImage.sort.min())
                            .from(subProductImage)
                            .where(subProductImage.productId.eq(product.id)
                                .and(subProductImage.visible.eq(true)))
                    ))
            )
            .leftJoin(uploadedFile).on(productImage.imageFileId.eq(uploadedFile.id))
            .where(
                product.name.containsIgnoreCase(keyword)
                    .and(product.visible.eq(true))
                    .and(product.soldOut.eq(false))
                    .and(shopPermanentlyClosedCol.eq(false))
            )
            .orderBy(product.representative.desc().nullsLast(), product.rating.desc().nullsLast())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<ProductListItemResult> findProducts(ProductSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(product.count())
            .from(product)
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
                product.id,
                shopNameCol,
                product.name,
                product.originalPrice,
                product.discountInfo.discountPrice,
                product.discountInfo.discountRate,
                product.representative,
                product.soldOut,
                product.visible,
                product.sort
            ))
            .from(product)
            .innerJoin(shop).on(product.shopId.eq(shopIdCol))
            .where(
                shopIdEq(condition.shopId()),
                categoryIdEq(condition.productCategoryId()),
                nameContains(condition.name()),
                visibleEq(condition.visible()),
                soldOutEq(condition.soldOut())
            )
            .orderBy(product.sort.asc(), product.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    private BooleanExpression shopIdEq(Long shopId) {
        return shopId != null ? product.shopId.eq(shopId) : null;
    }

    private BooleanExpression categoryIdEq(Long productCategoryId) {
        return productCategoryId != null ? product.productCategoryId.eq(productCategoryId) : null;
    }

    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? product.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression visibleEq(Boolean visible) {
        return visible != null ? product.visible.eq(visible) : null;
    }

    private BooleanExpression soldOutEq(Boolean soldOut) {
        return soldOut != null ? product.soldOut.eq(soldOut) : null;
    }

    @Override
    public List<Product> findActiveByShopIdOrderByRepresentativeAndRating(Long shopId) {
        return queryFactory
            .selectFrom(product)
            .where(product.shopId.eq(shopId), product.visible.eq(true))
            .orderBy(product.representative.desc(), product.rating.desc(), product.id.asc())
            .fetch();
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return productJpaRepository.findById(id.value());
    }

    @Override
    public List<Product> findAllByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(product)
            .where(product.id.in(ids), product.visible.eq(true))
            .fetch();
    }

    @Override
    public Product save(Product entity) {
        return productJpaRepository.save(entity);
    }
}
