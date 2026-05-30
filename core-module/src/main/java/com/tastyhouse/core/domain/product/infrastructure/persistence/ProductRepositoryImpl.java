package com.tastyhouse.core.domain.product.infrastructure.persistence;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.product.application.dto.result.ProductSimpleResult;
import com.tastyhouse.core.domain.product.application.dto.result.QProductSimpleResult;
import com.tastyhouse.core.domain.product.application.dto.result.QTodayDiscountProductResult;
import com.tastyhouse.core.domain.product.application.dto.result.SearchProductItemResult;
import com.tastyhouse.core.domain.product.application.dto.result.TodayDiscountProductResult;
import com.tastyhouse.core.domain.product.domain.model.Product;
import com.tastyhouse.core.domain.product.domain.model.QProductImage;
import com.tastyhouse.core.domain.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
    public Page<TodayDiscountProductResult> findTodayDiscountProducts(Pageable pageable) {
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
                    .and(productImage.isActive.eq(true))
                    .and(productImage.sort.eq(
                        JPAExpressions
                            .select(subProductImage.sort.min())
                            .from(subProductImage)
                            .where(subProductImage.productId.eq(product.id)
                                .and(subProductImage.isActive.eq(true)))
                    ))
            )
            .leftJoin(uploadedFile).on(productImage.imageFileId.eq(uploadedFile.id))
            .where(product.discountInfo.discountPrice.isNotNull()
                .and(product.isActive.eq(true)))
            .orderBy(product.discountInfo.discountRate.desc());

        long total = query.fetch().size();

        List<TodayDiscountProductResult> products = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return new PageImpl<>(products, pageable, total);
    }

    @Override
    public Page<SearchProductItemResult> searchByKeyword(String keyword, Pageable pageable) {
        Long total = queryFactory
            .select(product.count())
            .from(product)
            .innerJoin(shop).on(product.shopId.eq(shop.id))
            .where(
                product.name.containsIgnoreCase(keyword)
                    .and(product.isActive.eq(true))
                    .and(product.isSoldOut.eq(false))
                    .and(shop.permanentlyClosed.eq(false))
            )
            .fetchOne();

        if (total == null || total == 0) return new PageImpl<>(List.of(), pageable, 0);

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
                product.isRepresentative,
                product.spiciness
            ))
            .from(product)
            .innerJoin(shop).on(product.shopId.eq(shop.id))
            .leftJoin(productImage).on(
                productImage.productId.eq(product.id)
                    .and(productImage.isActive.eq(true))
                    .and(productImage.sort.eq(
                        JPAExpressions.select(subProductImage.sort.min())
                            .from(subProductImage)
                            .where(subProductImage.productId.eq(product.id)
                                .and(subProductImage.isActive.eq(true)))
                    ))
            )
            .leftJoin(uploadedFile).on(productImage.imageFileId.eq(uploadedFile.id))
            .where(
                product.name.containsIgnoreCase(keyword)
                    .and(product.isActive.eq(true))
                    .and(product.isSoldOut.eq(false))
                    .and(shop.permanentlyClosed.eq(false))
            )
            .orderBy(product.isRepresentative.desc().nullsLast(), product.rating.desc().nullsLast())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<ProductSimpleResult> findProductsByShopId(Long shopId) {
        return queryFactory
            .select(new QProductSimpleResult(
                product.id,
                shop.name,
                product.name,
                uploadedFile.filePath,
                product.originalPrice,
                product.discountInfo.discountPrice,
                product.discountInfo.discountRate
            ))
            .from(product)
            .innerJoin(shop).on(shop.id.eq(product.shopId))
            .leftJoin(productImage).on(
                productImage.productId.eq(product.id)
                    .and(productImage.isActive.eq(true))
                    .and(productImage.sort.eq(
                        JPAExpressions
                            .select(subProductImage.sort.min())
                            .from(subProductImage)
                            .where(subProductImage.productId.eq(product.id)
                                .and(subProductImage.isActive.eq(true)))
                    ))
            )
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(productImage.imageFileId))
            .where(product.shopId.eq(shopId)
                .and(product.isActive.eq(true)))
            .fetch();
    }

    @Override
    public List<Product> findByShopIdOrderByRepresentativeAndRating(Long shopId) {
        return queryFactory
            .selectFrom(product)
            .where(product.shopId.eq(shopId))
            .orderBy(product.isRepresentative.desc(), product.rating.desc(), product.id.asc())
            .fetch();
    }

    @Override
    public List<Product> findActiveByShopIdOrderByRepresentativeAndRating(Long shopId) {
        return queryFactory
            .selectFrom(product)
            .where(product.shopId.eq(shopId), product.isActive.eq(true))
            .orderBy(product.isRepresentative.desc(), product.rating.desc(), product.id.asc())
            .fetch();
    }

    @Override
    public List<Product> findByShopId(Long shopId) {
        return queryFactory
            .selectFrom(product)
            .where(product.shopId.eq(shopId))
            .fetch();
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return productJpaRepository.existsById(id);
    }

    @Override
    public Product save(Product entity) {
        return productJpaRepository.save(entity);
    }
}
