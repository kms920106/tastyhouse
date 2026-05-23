package com.tastyhouse.core.repository.product;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.product.Product;
import com.tastyhouse.core.entity.product.ProductBbq;
import com.tastyhouse.core.entity.product.ProductCategory;
import com.tastyhouse.core.entity.product.ProductCommonOption;
import com.tastyhouse.core.entity.product.ProductCommonOptionGroup;
import com.tastyhouse.core.entity.product.ProductImage;
import com.tastyhouse.core.entity.product.ProductOption;
import com.tastyhouse.core.entity.product.ProductOptionGroup;
import com.tastyhouse.core.entity.product.QProductImage;
import com.tastyhouse.core.entity.product.dto.ProductSimpleDto;
import com.tastyhouse.core.entity.product.dto.QProductSimpleDto;
import com.tastyhouse.core.entity.product.dto.QTodayDiscountProductDto;
import com.tastyhouse.core.entity.product.dto.SearchProductItemDto;
import com.tastyhouse.core.entity.product.dto.TodayDiscountProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.entity.place.QPlace.place;
import static com.tastyhouse.core.entity.product.QProduct.product;
import static com.tastyhouse.core.entity.product.QProductBbq.productBbq;
import static com.tastyhouse.core.entity.product.QProductCategory.productCategory;
import static com.tastyhouse.core.entity.product.QProductCommonOption.productCommonOption;
import static com.tastyhouse.core.entity.product.QProductCommonOptionGroup.productCommonOptionGroup;
import static com.tastyhouse.core.entity.product.QProductImage.productImage;
import static com.tastyhouse.core.entity.product.QProductOption.productOption;
import static com.tastyhouse.core.entity.product.QProductOptionGroup.productOptionGroup;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private static final QProductImage subProductImage = new QProductImage("subProductImage");

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<TodayDiscountProductDto> findTodayDiscountProducts(Pageable pageable) {
        JPAQuery<TodayDiscountProductDto> query = queryFactory
            .select(new QTodayDiscountProductDto(
                product.id,
                place.name,
                product.name,
                uploadedFile.filePath,
                product.originalPrice,
                product.discountPrice,
                product.discountRate
            ))
            .from(product)
            .innerJoin(place).on(product.placeId.eq(place.id))
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
            .where(product.discountPrice.isNotNull()
            .and(product.isActive.eq(true)))
            .orderBy(product.discountRate.desc());

        long total = query.fetch().size();

        List<TodayDiscountProductDto> products = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return new PageImpl<>(products, pageable, total);
    }

    @Override
    public List<ProductSimpleDto> findProductsByPlaceId(Long placeId) {
        return queryFactory
            .select(new QProductSimpleDto(
                product.id,
                place.name,
                product.name,
                uploadedFile.filePath,
                product.originalPrice,
                product.discountPrice,
                product.discountRate
            ))
            .from(product)
            .innerJoin(place).on(place.id.eq(product.placeId))
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
            .where(product.placeId.eq(placeId)
                .and(product.isActive.eq(true)))
            .fetch();
    }

    @Override
    public List<Product> findByPlaceIdOrderByRepresentativeAndRating(Long placeId) {
        return queryFactory
            .selectFrom(product)
            .where(product.placeId.eq(placeId))
            .orderBy(product.isRepresentative.desc(), product.rating.desc(), product.id.asc())
            .fetch();
    }

    @Override
    public List<Product> findActiveByPlaceIdOrderByRepresentativeAndRating(Long placeId) {
        return queryFactory
            .selectFrom(product)
            .where(product.placeId.eq(placeId), product.isActive.eq(true))
            .orderBy(product.isRepresentative.desc(), product.rating.desc(), product.id.asc())
            .fetch();
    }

    @Override
    public List<Product> findByPlaceId(Long placeId) {
        return queryFactory
            .selectFrom(product)
            .where(product.placeId.eq(placeId))
            .fetch();
    }

    @Override
    public List<ProductCategory> findActiveCategoriesByPlaceIdOrderBySort(Long placeId) {
        return queryFactory
            .selectFrom(productCategory)
            .where(productCategory.placeId.eq(placeId), productCategory.isActive.eq(true))
            .orderBy(productCategory.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductCategory> findCategoriesByNameAndPlaceId(String name, Long placeId) {
        return queryFactory
            .selectFrom(productCategory)
            .where(productCategory.name.eq(name), productCategory.placeId.eq(placeId))
            .fetch();
    }

    @Override
    public List<ProductImage> findActiveImagesByProductIdOrderBySort(Long productId) {
        return queryFactory
            .selectFrom(productImage)
            .where(productImage.productId.eq(productId), productImage.isActive.eq(true))
            .orderBy(productImage.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductImage> findImagesByProductIdOrderBySort(Long productId) {
        return queryFactory
            .selectFrom(productImage)
            .where(productImage.productId.eq(productId))
            .orderBy(productImage.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductOptionGroup> findActiveOptionGroupsByProductIdOrderBySort(Long productId) {
        return queryFactory
            .selectFrom(productOptionGroup)
            .where(productOptionGroup.productId.eq(productId), productOptionGroup.isActive.eq(true))
            .orderBy(productOptionGroup.sort.asc())
            .fetch();
    }

    @Override
    public boolean existsOptionGroupByProductId(Long productId) {
        return queryFactory
            .selectOne()
            .from(productOptionGroup)
            .where(productOptionGroup.productId.eq(productId))
            .fetchFirst() != null;
    }

    @Override
    public List<ProductOption> findActiveOptionsByOptionGroupIdOrderBySort(Long optionGroupId) {
        return queryFactory
            .selectFrom(productOption)
            .where(productOption.optionGroupId.eq(optionGroupId), productOption.isActive.eq(true))
            .orderBy(productOption.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductOption> findActiveOptionsByOptionGroupIdsOrderBySort(List<Long> optionGroupIds) {
        return queryFactory
            .selectFrom(productOption)
            .where(productOption.optionGroupId.in(optionGroupIds), productOption.isActive.eq(true))
            .orderBy(productOption.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductCommonOptionGroup> findActiveCommonOptionGroupsByProductIdOrderBySort(Long productId) {
        return queryFactory
            .selectFrom(productCommonOptionGroup)
            .where(productCommonOptionGroup.productId.eq(productId), productCommonOptionGroup.isActive.eq(true))
            .orderBy(productCommonOptionGroup.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductCommonOption> findActiveCommonOptionsByOptionGroupIdOrderBySort(Long optionGroupId) {
        return queryFactory
            .selectFrom(productCommonOption)
            .where(productCommonOption.optionGroupId.eq(optionGroupId), productCommonOption.isActive.eq(true))
            .orderBy(productCommonOption.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductCommonOption> findActiveCommonOptionsByOptionGroupIdsOrderBySort(List<Long> optionGroupIds) {
        return queryFactory
            .selectFrom(productCommonOption)
            .where(productCommonOption.optionGroupId.in(optionGroupIds), productCommonOption.isActive.eq(true))
            .orderBy(productCommonOption.sort.asc())
            .fetch();
    }

    @Override
    public Optional<ProductBbq> findBbqByProductId(Long productId) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(productBbq)
                .where(productBbq.productId.eq(productId))
                .fetchOne()
        );
    }

    @Override
    public boolean existsBbqByProductId(Long productId) {
        return queryFactory
            .selectOne()
            .from(productBbq)
            .where(productBbq.productId.eq(productId))
            .fetchFirst() != null;
    }

    @Override
    public Optional<ProductBbq> findFirstBbqWithOptionsSyncPending() {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(productBbq)
                .where(productBbq.isOptionsSynced.eq(false))
                .fetchFirst()
        );
    }

    @Override
    public Page<SearchProductItemDto> searchByKeyword(String keyword, Pageable pageable) {
        Long total = queryFactory
            .select(product.count())
            .from(product)
            .innerJoin(place).on(product.placeId.eq(place.id))
            .where(
                product.name.containsIgnoreCase(keyword)
                .and(product.isActive.eq(true))
                .and(product.isSoldOut.eq(false))
                .and(place.permanentlyClosed.eq(false))
            )
            .fetchOne();

        if (total == null || total == 0) return new PageImpl<>(List.of(), pageable, 0);

        List<SearchProductItemDto> content = queryFactory
            .select(Projections.constructor(SearchProductItemDto.class,
                product.id,
                place.name,
                product.name,
                uploadedFile.filePath,
                product.originalPrice,
                product.discountPrice,
                product.discountRate,
                product.rating,
                product.reviewCount,
                product.isRepresentative,
                product.spiciness
            ))
            .from(product)
            .innerJoin(place).on(product.placeId.eq(place.id))
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
                .and(place.permanentlyClosed.eq(false))
            )
            .orderBy(product.isRepresentative.desc().nullsLast(), product.rating.desc().nullsLast())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public String findFilePathByImageFileId(Long imageFileId) {
        return queryFactory
            .select(uploadedFile.filePath)
            .from(uploadedFile)
            .where(uploadedFile.id.eq(imageFileId))
            .fetchOne();
    }
}
