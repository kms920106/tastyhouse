package com.tastyhouse.core.repository.product;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.place.QPlace;
import com.tastyhouse.core.entity.product.Product;
import com.tastyhouse.core.entity.product.ProductBbq;
import com.tastyhouse.core.entity.product.ProductCategory;
import com.tastyhouse.core.entity.product.ProductCommonOption;
import com.tastyhouse.core.entity.product.ProductCommonOptionGroup;
import com.tastyhouse.core.entity.product.ProductImage;
import com.tastyhouse.core.entity.product.ProductOption;
import com.tastyhouse.core.entity.product.ProductOptionGroup;
import com.tastyhouse.core.entity.product.QProduct;
import com.tastyhouse.core.entity.product.QProductBbq;
import com.tastyhouse.core.entity.product.QProductCategory;
import com.tastyhouse.core.entity.product.QProductCommonOption;
import com.tastyhouse.core.entity.product.QProductCommonOptionGroup;
import com.tastyhouse.core.entity.product.QProductImage;
import com.tastyhouse.core.entity.product.QProductOption;
import com.tastyhouse.core.entity.product.QProductOptionGroup;
import com.tastyhouse.core.entity.file.QUploadedFile;
import com.tastyhouse.core.entity.product.dto.ProductSimpleDto;
import com.tastyhouse.core.entity.product.dto.QProductSimpleDto;
import com.tastyhouse.core.entity.product.dto.QTodayDiscountProductDto;
import com.tastyhouse.core.entity.product.dto.TodayDiscountProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<TodayDiscountProductDto> findTodayDiscountProducts(Pageable pageable) {
        QProduct product = QProduct.product;
        QPlace place = QPlace.place;
        QProductImage productImage = QProductImage.productImage;
        QProductImage subProductImage = new QProductImage("subProductImage");
        QUploadedFile uploadedFile = QUploadedFile.uploadedFile;

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
            .leftJoin(uploadedFile).on(productImage.uploadedFileId.eq(uploadedFile.id))
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
        QProduct product = QProduct.product;
        QPlace place = QPlace.place;
        QProductImage productImage = QProductImage.productImage;
        QProductImage subProductImage = new QProductImage("subProductImage");
        QUploadedFile uploadedFile = QUploadedFile.uploadedFile;

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
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(productImage.uploadedFileId))
            .where(product.placeId.eq(placeId)
                .and(product.isActive.eq(true)))
            .fetch();
    }

    @Override
    public List<Product> findByPlaceIdOrderByRepresentativeAndRating(Long placeId) {
        QProduct product = QProduct.product;
        return queryFactory
            .selectFrom(product)
            .where(product.placeId.eq(placeId))
            .orderBy(product.isRepresentative.desc(), product.rating.desc(), product.id.asc())
            .fetch();
    }

    @Override
    public List<Product> findActiveByPlaceIdOrderByRepresentativeAndRating(Long placeId) {
        QProduct product = QProduct.product;
        return queryFactory
            .selectFrom(product)
            .where(product.placeId.eq(placeId), product.isActive.eq(true))
            .orderBy(product.isRepresentative.desc(), product.rating.desc(), product.id.asc())
            .fetch();
    }

    @Override
    public List<Product> findActiveByPlaceIdOrderBySort(Long placeId) {
        QProduct product = QProduct.product;
        return queryFactory
            .selectFrom(product)
            .where(product.placeId.eq(placeId), product.isActive.eq(true))
            .orderBy(product.sort.asc())
            .fetch();
    }

    @Override
    public List<Product> findByPlaceId(Long placeId) {
        QProduct product = QProduct.product;
        return queryFactory
            .selectFrom(product)
            .where(product.placeId.eq(placeId))
            .fetch();
    }

    @Override
    public List<ProductCategory> findActiveCategoriesByPlaceIdOrderBySort(Long placeId) {
        QProductCategory category = QProductCategory.productCategory;
        return queryFactory
            .selectFrom(category)
            .where(category.placeId.eq(placeId), category.isActive.eq(true))
            .orderBy(category.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductCategory> findCategoriesByNameAndPlaceId(String name, Long placeId) {
        QProductCategory category = QProductCategory.productCategory;
        return queryFactory
            .selectFrom(category)
            .where(category.name.eq(name), category.placeId.eq(placeId))
            .fetch();
    }

    @Override
    public List<ProductImage> findActiveImagesByProductIdOrderBySort(Long productId) {
        QProductImage image = QProductImage.productImage;
        return queryFactory
            .selectFrom(image)
            .where(image.productId.eq(productId), image.isActive.eq(true))
            .orderBy(image.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductImage> findImagesByProductIdOrderBySort(Long productId) {
        QProductImage image = QProductImage.productImage;
        return queryFactory
            .selectFrom(image)
            .where(image.productId.eq(productId))
            .orderBy(image.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductOptionGroup> findActiveOptionGroupsByProductIdOrderBySort(Long productId) {
        QProductOptionGroup group = QProductOptionGroup.productOptionGroup;
        return queryFactory
            .selectFrom(group)
            .where(group.productId.eq(productId), group.isActive.eq(true))
            .orderBy(group.sort.asc())
            .fetch();
    }

    @Override
    public boolean existsOptionGroupByProductId(Long productId) {
        QProductOptionGroup group = QProductOptionGroup.productOptionGroup;
        return queryFactory
            .selectOne()
            .from(group)
            .where(group.productId.eq(productId))
            .fetchFirst() != null;
    }

    @Override
    public List<ProductOption> findActiveOptionsByOptionGroupIdOrderBySort(Long optionGroupId) {
        QProductOption option = QProductOption.productOption;
        return queryFactory
            .selectFrom(option)
            .where(option.optionGroupId.eq(optionGroupId), option.isActive.eq(true))
            .orderBy(option.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductOption> findActiveOptionsByOptionGroupIdsOrderBySort(List<Long> optionGroupIds) {
        QProductOption option = QProductOption.productOption;
        return queryFactory
            .selectFrom(option)
            .where(option.optionGroupId.in(optionGroupIds), option.isActive.eq(true))
            .orderBy(option.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductCommonOptionGroup> findActiveCommonOptionGroupsByProductIdOrderBySort(Long productId) {
        QProductCommonOptionGroup group = QProductCommonOptionGroup.productCommonOptionGroup;
        return queryFactory
            .selectFrom(group)
            .where(group.productId.eq(productId), group.isActive.eq(true))
            .orderBy(group.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductCommonOption> findActiveCommonOptionsByOptionGroupIdOrderBySort(Long optionGroupId) {
        QProductCommonOption option = QProductCommonOption.productCommonOption;
        return queryFactory
            .selectFrom(option)
            .where(option.optionGroupId.eq(optionGroupId), option.isActive.eq(true))
            .orderBy(option.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductCommonOption> findActiveCommonOptionsByOptionGroupIdsOrderBySort(List<Long> optionGroupIds) {
        QProductCommonOption option = QProductCommonOption.productCommonOption;
        return queryFactory
            .selectFrom(option)
            .where(option.optionGroupId.in(optionGroupIds), option.isActive.eq(true))
            .orderBy(option.sort.asc())
            .fetch();
    }

    @Override
    public Optional<ProductBbq> findBbqByProductId(Long productId) {
        QProductBbq bbq = QProductBbq.productBbq;
        return Optional.ofNullable(
            queryFactory
                .selectFrom(bbq)
                .where(bbq.productId.eq(productId))
                .fetchOne()
        );
    }

    @Override
    public boolean existsBbqByProductId(Long productId) {
        QProductBbq bbq = QProductBbq.productBbq;
        return queryFactory
            .selectOne()
            .from(bbq)
            .where(bbq.productId.eq(productId))
            .fetchFirst() != null;
    }

    @Override
    public Optional<ProductBbq> findFirstBbqWithOptionsSyncPending() {
        QProductBbq bbq = QProductBbq.productBbq;
        return Optional.ofNullable(
            queryFactory
                .selectFrom(bbq)
                .where(bbq.isOptionsSynced.eq(false))
                .fetchFirst()
        );
    }

    @Override
    public String findFilePathByUploadedFileId(Long uploadedFileId) {
        QUploadedFile uploadedFile = QUploadedFile.uploadedFile;
        return queryFactory
            .select(uploadedFile.filePath)
            .from(uploadedFile)
            .where(uploadedFile.id.eq(uploadedFileId))
            .fetchOne();
    }
}
