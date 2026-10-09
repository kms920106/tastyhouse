package com.tastyhouse.infrastructure.jpa.shop.query;

import java.util.List;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.ShopAmenityAssignmentResult;
import com.tastyhouse.application.shop.port.out.ShopAmenityCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopAmenityWithCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopClassificationManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopClassificationOwnerQueryPort;
import com.tastyhouse.application.shop.port.out.ShopClassificationQueryPort;
import com.tastyhouse.application.shop.port.out.ShopFoodTypeAssignmentResult;
import com.tastyhouse.application.shop.port.out.ShopFoodTypeCategoryResult;
import com.tastyhouse.infrastructure.jpa.file.persistence.QUploadedFileJpaEntity;
import com.tastyhouse.infrastructure.jpa.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopAmenityCategoryJpaEntity.shopAmenityCategoryJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopAmenityJpaEntity.shopAmenityJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopFoodTypeCategoryJpaEntity.shopFoodTypeCategoryJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopFoodTypeJpaEntity.shopFoodTypeJpaEntity;

@Repository
class ShopClassificationQueryAdapter implements ShopClassificationQueryPort, ShopClassificationManagementQueryPort, ShopClassificationOwnerQueryPort {

    private static final QUploadedFileJpaEntity activeFile = new QUploadedFileJpaEntity("activeFile");
    private static final QUploadedFileJpaEntity inactiveFile = new QUploadedFileJpaEntity("inactiveFile");

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ShopClassificationQueryAdapter(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
    public List<ShopFoodTypeCategoryResult> findVisibleFoodTypeCategories() {
        return queryFactory
            .select(Projections.constructor(ShopFoodTypeCategoryResult.class,
                shopFoodTypeCategoryJpaEntity.id,
                shopFoodTypeCategoryJpaEntity.foodType.stringValue(),
                shopFoodTypeCategoryJpaEntity.displayName,
                fileUrlResolver.urlOf(activeFile.filePath),
                fileUrlResolver.urlOf(inactiveFile.filePath),
                shopFoodTypeCategoryJpaEntity.sort,
                shopFoodTypeCategoryJpaEntity.visible
            ))
            .from(shopFoodTypeCategoryJpaEntity)
            .join(activeFile).on(activeFile.id.eq(shopFoodTypeCategoryJpaEntity.activeImageFileId))
            .join(inactiveFile).on(inactiveFile.id.eq(shopFoodTypeCategoryJpaEntity.inactiveImageFileId))
            .where(shopFoodTypeCategoryJpaEntity.visible.eq(true))
            .orderBy(shopFoodTypeCategoryJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public List<ShopAmenityCategoryResult> findVisibleAmenityCategories() {
        return queryFactory
            .select(Projections.constructor(ShopAmenityCategoryResult.class,
                shopAmenityCategoryJpaEntity.id,
                shopAmenityCategoryJpaEntity.amenity.stringValue(),
                shopAmenityCategoryJpaEntity.displayName,
                fileUrlResolver.urlOf(activeFile.filePath),
                fileUrlResolver.urlOf(inactiveFile.filePath),
                shopAmenityCategoryJpaEntity.sort,
                shopAmenityCategoryJpaEntity.visible
            ))
            .from(shopAmenityCategoryJpaEntity)
            .join(activeFile).on(activeFile.id.eq(shopAmenityCategoryJpaEntity.activeImageFileId))
            .join(inactiveFile).on(inactiveFile.id.eq(shopAmenityCategoryJpaEntity.inactiveImageFileId))
            .where(shopAmenityCategoryJpaEntity.visible.eq(true))
            .orderBy(shopAmenityCategoryJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public List<ShopAmenityCategoryResult> findAllAmenityCategories() {
        return queryFactory
            .select(Projections.constructor(ShopAmenityCategoryResult.class,
                shopAmenityCategoryJpaEntity.id,
                shopAmenityCategoryJpaEntity.amenity.stringValue(),
                shopAmenityCategoryJpaEntity.displayName,
                fileUrlResolver.urlOf(activeFile.filePath),
                fileUrlResolver.urlOf(inactiveFile.filePath),
                shopAmenityCategoryJpaEntity.sort,
                shopAmenityCategoryJpaEntity.visible
            ))
            .from(shopAmenityCategoryJpaEntity)
            .leftJoin(activeFile).on(activeFile.id.eq(shopAmenityCategoryJpaEntity.activeImageFileId))
            .leftJoin(inactiveFile).on(inactiveFile.id.eq(shopAmenityCategoryJpaEntity.inactiveImageFileId))
            .orderBy(shopAmenityCategoryJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public List<ShopFoodTypeCategoryResult> findAllFoodTypeCategories() {
        return queryFactory
            .select(Projections.constructor(ShopFoodTypeCategoryResult.class,
                shopFoodTypeCategoryJpaEntity.id,
                shopFoodTypeCategoryJpaEntity.foodType.stringValue(),
                shopFoodTypeCategoryJpaEntity.displayName,
                fileUrlResolver.urlOf(activeFile.filePath),
                fileUrlResolver.urlOf(inactiveFile.filePath),
                shopFoodTypeCategoryJpaEntity.sort,
                shopFoodTypeCategoryJpaEntity.visible
            ))
            .from(shopFoodTypeCategoryJpaEntity)
            .leftJoin(activeFile).on(activeFile.id.eq(shopFoodTypeCategoryJpaEntity.activeImageFileId))
            .leftJoin(inactiveFile).on(inactiveFile.id.eq(shopFoodTypeCategoryJpaEntity.inactiveImageFileId))
            .orderBy(shopFoodTypeCategoryJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public List<ShopAmenityAssignmentResult> findAmenityAssignments(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopAmenityAssignmentResult.class,
                shopAmenityJpaEntity.id,
                shopAmenityJpaEntity.shopAmenityCategoryId,
                shopAmenityCategoryJpaEntity.amenity.stringValue(),
                shopAmenityCategoryJpaEntity.displayName,
                fileUrlResolver.urlOf(activeFile.filePath)
            ))
            .from(shopAmenityJpaEntity)
            .join(shopAmenityCategoryJpaEntity).on(shopAmenityCategoryJpaEntity.id.eq(shopAmenityJpaEntity.shopAmenityCategoryId))
            .join(activeFile).on(activeFile.id.eq(shopAmenityCategoryJpaEntity.activeImageFileId))
            .where(shopAmenityJpaEntity.shopId.eq(shopId))
            .fetch();
    }

    @Override
    public List<ShopAmenityWithCategoryResult> findAmenitiesWithCategory(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopAmenityWithCategoryResult.class,
                shopAmenityCategoryJpaEntity.amenity.stringValue(),
                shopAmenityCategoryJpaEntity.displayName,
                fileUrlResolver.urlOf(activeFile.filePath)
            ))
            .from(shopAmenityJpaEntity)
            .join(shopAmenityCategoryJpaEntity).on(shopAmenityCategoryJpaEntity.id.eq(shopAmenityJpaEntity.shopAmenityCategoryId))
            .join(activeFile).on(activeFile.id.eq(shopAmenityCategoryJpaEntity.activeImageFileId))
            .where(shopAmenityJpaEntity.shopId.eq(shopId))
            .fetch();
    }

    @Override
    public List<ShopFoodTypeAssignmentResult> findFoodTypeAssignments(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopFoodTypeAssignmentResult.class,
                shopFoodTypeJpaEntity.id,
                shopFoodTypeJpaEntity.shopFoodTypeCategoryId,
                shopFoodTypeCategoryJpaEntity.foodType.stringValue(),
                shopFoodTypeCategoryJpaEntity.displayName,
                fileUrlResolver.urlOf(activeFile.filePath)
            ))
            .from(shopFoodTypeJpaEntity)
            .join(shopFoodTypeCategoryJpaEntity).on(shopFoodTypeCategoryJpaEntity.id.eq(shopFoodTypeJpaEntity.shopFoodTypeCategoryId))
            .join(activeFile).on(activeFile.id.eq(shopFoodTypeCategoryJpaEntity.activeImageFileId))
            .where(shopFoodTypeJpaEntity.shopId.eq(shopId))
            .fetch();
    }

    @Override
    public List<String> findFoodTypeCategoryNames(Long shopId) {
        return queryFactory
            .select(shopFoodTypeCategoryJpaEntity.displayName)
            .from(shopFoodTypeJpaEntity)
            .join(shopFoodTypeCategoryJpaEntity).on(shopFoodTypeCategoryJpaEntity.id.eq(shopFoodTypeJpaEntity.shopFoodTypeCategoryId))
            .where(shopFoodTypeJpaEntity.shopId.eq(shopId))
            .fetch();
    }
}
