package com.tastyhouse.core.domain.product.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.product.application.dto.result.QTodayDiscountProductResult;
import com.tastyhouse.core.domain.product.application.dto.result.SearchProductItemResult;
import com.tastyhouse.core.domain.product.application.dto.result.TodayDiscountProductResult;
import com.tastyhouse.core.domain.product.domain.model.Product;
import com.tastyhouse.core.domain.product.domain.model.QProductImage;
import com.tastyhouse.core.domain.product.domain.repository.ProductRepository;
import com.tastyhouse.core.domain.product.domain.vo.ProductId;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.domain.product.domain.model.QProduct.product;
import static com.tastyhouse.core.domain.product.domain.model.QProductImage.productImage;
import static com.tastyhouse.core.domain.shop.domain.model.QShop.shop;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private static final QProductImage subProductImage = new QProductImage("subProductImage");

    private final JPAQueryFactory queryFactory;
    private final ProductJpaRepository productJpaRepository;

    @Override
    public PageResult<TodayDiscountProductResult> findTodayDiscountProducts(PageQuery pageQuery) {
        JPAQuery<TodayDiscountProductResult> query = queryFactory
            .select(new QTodayDiscountProductResult(
                product.id,
                shop.name,
                product.name,
                uploadedFile.filePath,
                product.originalPrice,
                product.discountInfo.discountPrice,
                product.discountInfo.discountRate
            ))
            .from(product)
            .innerJoin(shop).on(product.shopId.eq(shop.id))
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
            .innerJoin(shop).on(product.shopId.eq(shop.id))
            .where(
                product.name.containsIgnoreCase(keyword)
                    .and(product.visible.eq(true))
                    .and(product.soldOut.eq(false))
                    .and(shop.permanentlyClosed.eq(false))
            )
            .fetchOne();

        if (total == null || total == 0) return PageResult.empty(pageQuery.page(), pageQuery.size());

        List<SearchProductItemResult> content = queryFactory
            .select(Projections.constructor(SearchProductItemResult.class,
                product.id,
                shop.name,
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
            .innerJoin(shop).on(product.shopId.eq(shop.id))
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
                    .and(shop.permanentlyClosed.eq(false))
            )
            .orderBy(product.representative.desc().nullsLast(), product.rating.desc().nullsLast())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
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
