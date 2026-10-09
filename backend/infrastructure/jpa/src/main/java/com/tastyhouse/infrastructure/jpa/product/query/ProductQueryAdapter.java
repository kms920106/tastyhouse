package com.tastyhouse.infrastructure.jpa.product.query;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.ProductBbqSyncQueryPort;
import com.tastyhouse.application.product.port.out.ProductBbqSyncTargetResult;
import com.tastyhouse.application.product.port.out.ProductCategoryManagementResult;
import com.tastyhouse.application.product.port.out.ProductCategoryResult;
import com.tastyhouse.application.product.port.out.ProductDetailResult;
import com.tastyhouse.application.product.port.out.ProductExposureHourResult;
import com.tastyhouse.application.product.port.out.ProductExposurePeriodResult;
import com.tastyhouse.application.product.port.out.ProductImageManagementResult;
import com.tastyhouse.application.product.port.out.ProductListItemResult;
import com.tastyhouse.application.product.port.out.ProductManagementDetailResult;
import com.tastyhouse.application.product.port.out.ProductManagementQueryPort;
import com.tastyhouse.application.product.port.out.ProductNutritionResult;
import com.tastyhouse.application.product.port.out.ProductOwnerPriceView;
import com.tastyhouse.application.product.port.out.ProductOwnerQueryPort;
import com.tastyhouse.application.product.port.out.ProductPriceResult;
import com.tastyhouse.application.product.port.out.ProductQueryPort;
import com.tastyhouse.application.product.port.out.ProductSearchCondition;
import com.tastyhouse.application.product.port.out.ProductVegetarianSettingResult;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.infrastructure.jpa.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.jpa.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductAllergenJpaEntity.productAllergenJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductBbqJpaEntity.productBbqJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductCategoryJpaEntity.productCategoryJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductExposureHourJpaEntity.productExposureHourJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductImageJpaEntity.productImageJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductJpaEntity.productJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductNutritionJpaEntity.productNutritionJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductPriceJpaEntity.productPriceJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductShopLinkJpaEntity.productShopLinkJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopJpaEntity.shopJpaEntity;

@Repository
class ProductQueryAdapter implements ProductQueryPort, ProductBbqSyncQueryPort, ProductManagementQueryPort, ProductOwnerQueryPort {

    private static final com.tastyhouse.infrastructure.jpa.file.persistence.QUploadedFileJpaEntity
        productImageFile =
        new com.tastyhouse.infrastructure.jpa.file.persistence.QUploadedFileJpaEntity("productImageFile");

    private static final com.tastyhouse.infrastructure.jpa.product.persistence.QProductExposureHourJpaEntity
        subExposureHour =
        new com.tastyhouse.infrastructure.jpa.product.persistence.QProductExposureHourJpaEntity("subExposureHour");

    private static final com.tastyhouse.infrastructure.jpa.product.persistence.QProductJpaEntity subCategoryProduct =
        new com.tastyhouse.infrastructure.jpa.product.persistence.QProductJpaEntity("subCategoryProduct");

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ProductQueryAdapter(
        JPAQueryFactory queryFactory,
        FileUrlResolver fileUrlResolver
    ) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
    public List<String> findProductImageUrls(Long productId) {
        List<String> filePaths = queryFactory
            .select(uploadedFileJpaEntity.filePath)
            .from(productImageJpaEntity)
            .innerJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(productImageJpaEntity.productId.eq(productId), productImageJpaEntity.visible.eq(true))
            .orderBy(productImageJpaEntity.sort.asc())
            .fetch();

        return fileUrlResolver.resolveAll(filePaths);
    }

    @Override
    public PageResult<ProductListItemResult> findProducts(ProductSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(productJpaEntity.count())
            .from(productJpaEntity)
            .where(
                shopIdEq(condition.shopId()),
                categoryIdEq(condition.productCategoryId()),
                ProductQueryPredicates.nameContains(condition.name()),
                visibleEq(condition.visible()),
                soldOutEq(condition.soldOut()),
                ProductQueryPredicates.notDeleted()
            )
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ProductListItemResult> content = queryFactory
            .select(Projections.constructor(ProductListItemResult.class,
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
                ProductQueryPredicates.nameContains(condition.name()),
                visibleEq(condition.visible()),
                soldOutEq(condition.soldOut()),
                ProductQueryPredicates.notDeleted()
            )
            .orderBy(productJpaEntity.sort.asc(), productJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<ProductDetailResult> findProductDetailById(Long productId) {
        ProductDetailResult result = queryFactory
            .select(Projections.constructor(ProductDetailResult.class,
                productJpaEntity.id,
                productJpaEntity.shopId,
                productJpaEntity.productCategoryId,
                productJpaEntity.name,
                productJpaEntity.description,
                productJpaEntity.originalPrice,
                productJpaEntity.discountInfo.discountPrice,
                productJpaEntity.discountInfo.discountRate,
                productJpaEntity.rating,
                productJpaEntity.reviewCount,
                productJpaEntity.representative,
                productJpaEntity.spiciness,
                productJpaEntity.soldOut,
                productJpaEntity.visible,
                productJpaEntity.sort,
                productJpaEntity.weightText,
                productJpaEntity.createdAt,
                productJpaEntity.updatedAt
            ))
            .from(productJpaEntity)
            .where(productJpaEntity.id.eq(productId), ProductQueryPredicates.notDeleted())
            .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public List<ProductPriceResult> findProductPrices(Long productId) {
        return queryFactory
            .select(Projections.constructor(ProductPriceResult.class,
                productPriceJpaEntity.id,
                productPriceJpaEntity.productId,
                productPriceJpaEntity.priceName,
                productPriceJpaEntity.deliveryPrice,
                productPriceJpaEntity.storePrice,
                productPriceJpaEntity.pickupPrice,
                productPriceJpaEntity.sort,
                productPriceJpaEntity.pickupPriceSetAt
            ))
            .from(productPriceJpaEntity)
            .where(productPriceJpaEntity.productId.eq(productId))
            .orderBy(productPriceJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductPriceResult> findProductPricesByProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
            .select(Projections.constructor(ProductPriceResult.class,
                productPriceJpaEntity.id,
                productPriceJpaEntity.productId,
                productPriceJpaEntity.priceName,
                productPriceJpaEntity.deliveryPrice,
                productPriceJpaEntity.storePrice,
                productPriceJpaEntity.pickupPrice,
                productPriceJpaEntity.sort,
                productPriceJpaEntity.pickupPriceSetAt
            ))
            .from(productPriceJpaEntity)
            .where(productPriceJpaEntity.productId.in(productIds))
            .orderBy(productPriceJpaEntity.productId.asc(), productPriceJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductPriceResult> findShopProductPrices(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ProductPriceResult.class,
                productPriceJpaEntity.id,
                productPriceJpaEntity.productId,
                productPriceJpaEntity.priceName,
                productPriceJpaEntity.deliveryPrice,
                productPriceJpaEntity.storePrice,
                productPriceJpaEntity.pickupPrice,
                productPriceJpaEntity.sort,
                productPriceJpaEntity.pickupPriceSetAt
            ))
            .from(productPriceJpaEntity)
            .join(productJpaEntity).on(productJpaEntity.id.eq(productPriceJpaEntity.productId))
            .join(productShopLinkJpaEntity)
            .on(
                productShopLinkJpaEntity.productId.eq(productJpaEntity.id),
                productShopLinkJpaEntity.shopId.eq(shopId)
            )
            .where(ProductQueryPredicates.notDeleted())
            .orderBy(productShopLinkJpaEntity.sort.asc(), productPriceJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public long countVisibleProducts(Long shopId) {
        Long count = queryFactory
            .select(productJpaEntity.count())
            .from(productJpaEntity)
            .where(productJpaEntity.shopId.eq(shopId), productJpaEntity.visible.isTrue(), ProductQueryPredicates.notDeleted())
            .fetchOne();
        return count != null ? count : 0L;
    }

    @Override
    public Optional<ProductManagementDetailResult> findProductManagementDetailById(Long productId) {
        ProductManagementDetailResult result = queryFactory
            .select(Projections.constructor(ProductManagementDetailResult.class,
                productJpaEntity.id,
                productJpaEntity.shopId,
                productJpaEntity.productCategoryId,
                productCategoryJpaEntity.name,
                productJpaEntity.name,
                productJpaEntity.composition,
                productJpaEntity.description,
                productJpaEntity.originalPrice,
                productJpaEntity.discountInfo.discountPrice,
                productJpaEntity.singleServing,
                productJpaEntity.spiciness,
                productJpaEntity.representative,
                productJpaEntity.ratingExcluded,
                productJpaEntity.soldOut,
                productJpaEntity.visible,
                fileUrlResolver.urlOf(uploadedFileJpaEntity.filePath),
                productJpaEntity.vegetarianType.stringValue(),
                productJpaEntity.weightText,
                productJpaEntity.exposureStartDate.isNotNull()
                    .or(productJpaEntity.exposureEndDate.isNotNull())
                    .or(existsExposureHours(productJpaEntity.id))
            ))
            .from(productJpaEntity)
            .leftJoin(productCategoryJpaEntity).on(productJpaEntity.productCategoryId.eq(productCategoryJpaEntity.id))
            .leftJoin(productImageJpaEntity).on(ProductQueryPredicates.representativeImageOf(productJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(productJpaEntity.id.eq(productId), ProductQueryPredicates.notDeleted())
            .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<ProductNutritionResult> findNutrition(Long productId) {
        ProductNutritionResult result = queryFactory
            .select(Projections.constructor(ProductNutritionResult.class,
                productNutritionJpaEntity.id,
                productNutritionJpaEntity.productId,
                productNutritionJpaEntity.servingSize,
                productNutritionJpaEntity.totalAmount,
                productNutritionJpaEntity.flavor,
                productNutritionJpaEntity.size,
                productNutritionJpaEntity.calorie,
                productNutritionJpaEntity.sugars,
                productNutritionJpaEntity.protein,
                productNutritionJpaEntity.saturatedFat,
                productNutritionJpaEntity.natrium,
                productNutritionJpaEntity.carbohydrate,
                productNutritionJpaEntity.cholesterol,
                productNutritionJpaEntity.fat,
                productNutritionJpaEntity.transFat,
                productNutritionJpaEntity.caffeine,
                productNutritionJpaEntity.setMenu
            ))
            .from(productNutritionJpaEntity)
            .where(productNutritionJpaEntity.productId.eq(productId))
            .fetchFirst();
        return Optional.ofNullable(result);
    }

    @Override
    public List<String> findAllergenTypes(Long productId) {
        return queryFactory
            .select(productAllergenJpaEntity.allergenType.stringValue())
            .from(productAllergenJpaEntity)
            .where(productAllergenJpaEntity.productId.eq(productId))
            .orderBy(productAllergenJpaEntity.id.asc())
            .fetch();
    }

    private BooleanExpression existsExposureHours(NumberPath<Long> productId) {
        return JPAExpressions
            .selectOne()
            .from(subExposureHour)
            .where(subExposureHour.productId.eq(productId))
            .exists();
    }

    @Override
    public List<ProductCategoryResult> findProductCategories(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ProductCategoryResult.class,
                productCategoryJpaEntity.id,
                productCategoryJpaEntity.shopId,
                productCategoryJpaEntity.name,
                productCategoryJpaEntity.description,
                productCategoryJpaEntity.sort,
                productCategoryJpaEntity.visible
            ))
            .from(productCategoryJpaEntity)
            .where(productCategoryJpaEntity.shopId.eq(shopId), productCategoryJpaEntity.visible.eq(true))
            .orderBy(productCategoryJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductCategoryManagementResult> findProductCategoriesForManagement(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ProductCategoryManagementResult.class,
                productCategoryJpaEntity.id,
                productCategoryJpaEntity.shopId,
                productCategoryJpaEntity.name,
                productCategoryJpaEntity.description,
                productCategoryJpaEntity.sort,
                productCategoryJpaEntity.visible,
                JPAExpressions
                    .select(subCategoryProduct.count())
                    .from(subCategoryProduct)
                    .where(
                        subCategoryProduct.productCategoryId.eq(productCategoryJpaEntity.id),
                        subCategoryProduct.deleted.isFalse()
                    )
            ))
            .from(productCategoryJpaEntity)
            .where(productCategoryJpaEntity.shopId.eq(shopId))
            .orderBy(productCategoryJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductImageManagementResult> findProductImagesForManagement(Long productId) {
        return queryFactory
            .select(Projections.constructor(ProductImageManagementResult.class,
                productImageJpaEntity.id,
                fileUrlResolver.urlOf(productImageFile.filePath),
                productImageJpaEntity.sort,
                productImageJpaEntity.visible
            ))
            .from(productImageJpaEntity)
            .leftJoin(productImageFile).on(productImageFile.id.eq(productImageJpaEntity.imageFileId))
            .where(productImageJpaEntity.productId.eq(productId))
            .orderBy(productImageJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public boolean existsProductInShop(Long productId, Long shopId) {
        return queryFactory
            .selectOne()
            .from(productJpaEntity)
            .leftJoin(productShopLinkJpaEntity)
            .on(
                productShopLinkJpaEntity.productId.eq(productJpaEntity.id),
                productShopLinkJpaEntity.shopId.eq(shopId)
            )
            .where(
                productJpaEntity.id.eq(productId),
                ProductQueryPredicates.notDeleted(),
                productShopLinkJpaEntity.id.isNotNull().or(productJpaEntity.shopId.eq(shopId))
            )
            .fetchFirst() != null;
    }

    @Override
    public Optional<ProductVegetarianSettingResult> findVegetarianSetting(Long productId) {
        ProductVegetarianSettingResult result = queryFactory
            .select(Projections.constructor(ProductVegetarianSettingResult.class,
                productJpaEntity.id,
                productJpaEntity.shopId,
                productJpaEntity.vegetarianType
            ))
            .from(productJpaEntity)
            .where(productJpaEntity.id.eq(productId), ProductQueryPredicates.notDeleted())
            .fetchFirst();
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<ProductExposurePeriodResult> findExposurePeriod(Long productId) {
        ProductExposurePeriodResult result = queryFactory
            .select(Projections.constructor(ProductExposurePeriodResult.class,
                productJpaEntity.id,
                productJpaEntity.shopId,
                productJpaEntity.exposureStartDate,
                productJpaEntity.exposureEndDate
            ))
            .from(productJpaEntity)
            .where(productJpaEntity.id.eq(productId), ProductQueryPredicates.notDeleted())
            .fetchFirst();
        return Optional.ofNullable(result);
    }

    @Override
    public List<ProductExposureHourResult> findExposureHours(Long productId) {
        return queryFactory
            .select(Projections.constructor(ProductExposureHourResult.class,
                productExposureHourJpaEntity.dayType,
                productExposureHourJpaEntity.startTime,
                productExposureHourJpaEntity.endTime
            ))
            .from(productExposureHourJpaEntity)
            .where(productExposureHourJpaEntity.productId.eq(productId))
            .fetch();
    }

    @Override
    public List<ProductOwnerPriceView> findPrices(Long productId) {
        return queryFactory
            .select(Projections.constructor(ProductOwnerPriceView.class,
                productPriceJpaEntity.id,
                productPriceJpaEntity.priceName,
                productPriceJpaEntity.deliveryPrice,
                productPriceJpaEntity.storePrice,
                productPriceJpaEntity.pickupPrice,
                productPriceJpaEntity.sort
            ))
            .from(productPriceJpaEntity)
            .where(productPriceJpaEntity.productId.eq(productId))
            .orderBy(productPriceJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public Optional<ProductBbqSyncTargetResult> findFirstBbqSyncTarget() {
        ProductBbqSyncTargetResult result = queryFactory
            .select(Projections.constructor(ProductBbqSyncTargetResult.class,
                productBbqJpaEntity.productId,
                productBbqJpaEntity.bbqMenuId,
                productJpaEntity.name
            ))
            .from(productBbqJpaEntity)
            .innerJoin(productJpaEntity).on(productBbqJpaEntity.productId.eq(productJpaEntity.id))
            .where(productBbqJpaEntity.optionsSynced.eq(false), ProductQueryPredicates.notDeleted())
            .fetchFirst();
        return Optional.ofNullable(result);
    }

    private BooleanExpression shopIdEq(Long shopId) {
        return shopId != null ? productJpaEntity.shopId.eq(shopId) : null;
    }

    private BooleanExpression categoryIdEq(Long productCategoryId) {
        return productCategoryId != null ? productJpaEntity.productCategoryId.eq(productCategoryId) : null;
    }

    private BooleanExpression visibleEq(Boolean visible) {
        return visible != null ? productJpaEntity.visible.eq(visible) : null;
    }

    private BooleanExpression soldOutEq(Boolean soldOut) {
        return soldOut != null ? productJpaEntity.soldOut.eq(soldOut) : null;
    }
}
