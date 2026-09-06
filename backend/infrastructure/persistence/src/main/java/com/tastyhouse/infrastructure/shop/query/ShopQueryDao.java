package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.application.shop.port.out.ShopManagementDetailResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopOwnerQueryPort;
import com.tastyhouse.application.shop.port.out.ShopQueryPort;
import com.tastyhouse.application.shop.port.out.ShopVisibleDetailResult;
import com.tastyhouse.application.shop.port.out.ShopAmenityAssignmentResult;
import com.tastyhouse.application.shop.port.out.ShopAmenityCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopAmenityWithCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopBannerImageResult;
import com.tastyhouse.application.shop.port.out.ShopBreakTimeResult;
import com.tastyhouse.application.shop.port.out.ShopBusinessHourResult;
import com.tastyhouse.application.shop.port.out.ShopClosedDayResult;
import com.tastyhouse.application.shop.port.out.ShopContentBoardResult;
import com.tastyhouse.application.shop.port.out.ShopConvenienceInfoResult;
import com.tastyhouse.application.shop.port.out.ShopFoodTypeAssignmentResult;
import com.tastyhouse.application.shop.port.out.ShopFoodTypeCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopHygieneBadgeResult;
import com.tastyhouse.application.shop.port.out.ShopImageChangeRequestResult;
import com.tastyhouse.application.shop.port.out.ShopImageUrlsResult;
import com.tastyhouse.application.shop.port.out.ShopMenuCollectionImageExposureResult;
import com.tastyhouse.application.shop.port.out.ShopMenuCollectionImageRequestResult;
import com.tastyhouse.application.shop.port.out.ShopMenuCollectionImageResult;
import com.tastyhouse.application.shop.port.out.ShopOrderMethodResult;
import com.tastyhouse.application.shop.port.out.ShopOriginInfoResult;
import com.tastyhouse.application.shop.port.out.ShopOwnerMessageResult;
import com.tastyhouse.application.shop.port.out.ShopPhoneNumberResult;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryImageManagementResult;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryImageResult;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopSuspensionResult;
import com.tastyhouse.application.shop.port.out.ShopTemporaryClosureResult;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.model.ShopContentType;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopAmenityCategoryJpaEntity.shopAmenityCategoryJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopAmenityJpaEntity.shopAmenityJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopBannerImageJpaEntity.shopBannerImageJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopBreakTimeJpaEntity.shopBreakTimeJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopBusinessHourJpaEntity.shopBusinessHourJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopClosedDayJpaEntity.shopClosedDayJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopContentBoardJpaEntity.shopContentBoardJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopConvenienceInfoJpaEntity.shopConvenienceInfoJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopFoodTypeCategoryJpaEntity.shopFoodTypeCategoryJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopFoodTypeJpaEntity.shopFoodTypeJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopHygieneBadgeJpaEntity.shopHygieneBadgeJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopImageChangeRequestJpaEntity.shopImageChangeRequestJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopOrderMethodJpaEntity.shopOrderMethodJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopOriginInfoJpaEntity.shopOriginInfoJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopOwnerMessageHistoryJpaEntity.shopOwnerMessageHistoryJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopPhoneNumberJpaEntity.shopPhoneNumberJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopBookmarkJpaEntity.shopBookmarkJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopMenuCollectionImageJpaEntity.shopMenuCollectionImageJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopPhotoCategoryImageJpaEntity.shopPhotoCategoryImageJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopPhotoCategoryJpaEntity.shopPhotoCategoryJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopSuspensionJpaEntity.shopSuspensionJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopTemporaryClosureJpaEntity.shopTemporaryClosureJpaEntity;

@Repository
public class ShopQueryDao implements ShopQueryPort, ShopBasicInfoQueryPort, ShopManagementQueryPort, ShopOwnerQueryPort {
    private static final QUploadedFileJpaEntity activeFile = new QUploadedFileJpaEntity("activeFile");
    private static final QUploadedFileJpaEntity inactiveFile = new QUploadedFileJpaEntity("inactiveFile");

    private static final QUploadedFileJpaEntity contentBoardImageFile = new QUploadedFileJpaEntity("contentBoardImageFile");
    private static final QUploadedFileJpaEntity imageChangeRequestImageFile = new QUploadedFileJpaEntity("imageChangeRequestImageFile");

    private static final QUploadedFileJpaEntity menuCollectionImageFile = new QUploadedFileJpaEntity("menuCollectionImageFile");

    private static final QUploadedFileJpaEntity shopThumbnailFile = new QUploadedFileJpaEntity("shopThumbnailFile");
    private static final QUploadedFileJpaEntity shopTrademarkFile = new QUploadedFileJpaEntity("shopTrademarkFile");

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ShopQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    public Optional<String> findShopName(Long shopId) {
        return Optional.ofNullable(
            queryFactory
                .select(shopJpaEntity.name)
                .from(shopJpaEntity)
                .where(shopJpaEntity.id.eq(shopId))
                .fetchOne()
        );
    }

    @Override
    public List<ShopPhoneNumberResult> findPhoneNumbers(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopPhoneNumberResult.class,
                shopPhoneNumberJpaEntity.id,
                shopPhoneNumberJpaEntity.shopId,
                shopPhoneNumberJpaEntity.phoneNumber,
                shopPhoneNumberJpaEntity.primary,
                shopPhoneNumberJpaEntity.virtual
            ))
            .from(shopPhoneNumberJpaEntity)
            .where(shopPhoneNumberJpaEntity.shopId.eq(shopId))
            .orderBy(shopPhoneNumberJpaEntity.primary.desc(), shopPhoneNumberJpaEntity.id.asc())
            .fetch();
    }

    @Override
    public Optional<ShopImageUrlsResult> findShopImageUrls(Long shopId) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(ShopImageUrlsResult.class,
                    shopJpaEntity.id,
                    shopThumbnailFile.filePath,
                    shopTrademarkFile.filePath
                ))
                .from(shopJpaEntity)
                .leftJoin(shopThumbnailFile).on(shopThumbnailFile.id.eq(shopJpaEntity.thumbnailImageFileId))
                .leftJoin(shopTrademarkFile).on(shopTrademarkFile.id.eq(shopJpaEntity.trademarkImageFileId))
                .where(shopJpaEntity.id.eq(shopId))
                .fetchOne()
        ).map(this::withResolvedImageUrls);
    }

    @Override
    public Optional<ShopConvenienceInfoResult> findConvenienceInfo(Long shopId) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(ShopConvenienceInfoResult.class,
                    shopConvenienceInfoJpaEntity.id,
                    shopConvenienceInfoJpaEntity.shopId,
                    shopConvenienceInfoJpaEntity.parkingAvailable,
                    shopConvenienceInfoJpaEntity.parkingPaid,
                    shopConvenienceInfoJpaEntity.valetAvailable,
                    shopConvenienceInfoJpaEntity.valetPaid,
                    shopConvenienceInfoJpaEntity.directionsGuide,
                    shopConvenienceInfoJpaEntity.displayLatitude,
                    shopConvenienceInfoJpaEntity.displayLongitude
                ))
                .from(shopConvenienceInfoJpaEntity)
                .where(shopConvenienceInfoJpaEntity.shopId.eq(shopId))
                .fetchFirst()
        );
    }

    @Override
    public Optional<ShopOriginInfoResult> findOriginInfo(Long shopId) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(ShopOriginInfoResult.class,
                    shopOriginInfoJpaEntity.id,
                    shopOriginInfoJpaEntity.shopId,
                    shopOriginInfoJpaEntity.sourceType.stringValue(),
                    shopOriginInfoJpaEntity.content,
                    shopOriginInfoJpaEntity.url,
                    shopOriginInfoJpaEntity.updatedAt
                ))
                .from(shopOriginInfoJpaEntity)
                .where(shopOriginInfoJpaEntity.shopId.eq(shopId))
                .fetchFirst()
        );
    }

    @Override
    public List<ShopContentBoardResult> findContentBoards(Long shopId) {
        return contentBoardProjection()
            .where(shopContentBoardJpaEntity.shopId.eq(shopId))
            .orderBy(shopContentBoardJpaEntity.id.asc())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
    }

    @Override
    public PageResult<ShopContentBoardResult> findContentBoardPage(
        Long shopId,
        Boolean hidden,
        ShopContentType contentType,
        PageQuery pageQuery
    ) {
        Long total = queryFactory
            .select(shopContentBoardJpaEntity.count())
            .from(shopContentBoardJpaEntity)
            .where(
                shopContentBoardJpaEntity.shopId.eq(shopId),
                contentBoardHiddenEq(hidden),
                contentBoardContentTypeEq(contentType)
            )
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ShopContentBoardResult> content = contentBoardProjection()
            .where(
                shopContentBoardJpaEntity.shopId.eq(shopId),
                contentBoardHiddenEq(hidden),
                contentBoardContentTypeEq(contentType)
            )
            .orderBy(shopContentBoardJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    private JPQLQuery<ShopContentBoardResult> contentBoardProjection() {
        return queryFactory
            .select(Projections.constructor(ShopContentBoardResult.class,
                shopContentBoardJpaEntity.id,
                shopContentBoardJpaEntity.shopId,
                shopContentBoardJpaEntity.contentType,
                shopContentBoardJpaEntity.topic,
                contentBoardImageFile.filePath,
                shopContentBoardJpaEntity.youtubeUrl,
                shopContentBoardJpaEntity.description,
                shopContentBoardJpaEntity.hidden,
                shopContentBoardJpaEntity.createdAt
            ))
            .from(shopContentBoardJpaEntity)
            .leftJoin(contentBoardImageFile).on(contentBoardImageFile.id.eq(shopContentBoardJpaEntity.imageFileId));
    }

    private BooleanExpression contentBoardHiddenEq(Boolean hidden) {
        return hidden != null ? shopContentBoardJpaEntity.hidden.eq(hidden) : null;
    }

    private BooleanExpression contentBoardContentTypeEq(ShopContentType contentType) {
        return contentType != null ? shopContentBoardJpaEntity.contentType.eq(contentType) : null;
    }

    @Override
    public List<ShopHygieneBadgeResult> findHygieneBadges(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopHygieneBadgeResult.class,
                shopHygieneBadgeJpaEntity.id,
                shopHygieneBadgeJpaEntity.shopId,
                shopHygieneBadgeJpaEntity.badgeType,
                shopHygieneBadgeJpaEntity.certifiedDate,
                shopHygieneBadgeJpaEntity.lastInspectionMonth
            ))
            .from(shopHygieneBadgeJpaEntity)
            .where(shopHygieneBadgeJpaEntity.shopId.eq(shopId))
            .orderBy(shopHygieneBadgeJpaEntity.certifiedDate.desc())
            .fetch();
    }

    @Override
    public List<ShopImageChangeRequestResult> findImageChangeRequests(Long shopId, ShopImageType imageType) {
        return imageChangeRequestProjection()
            .where(
                shopImageChangeRequestJpaEntity.shopId.eq(shopId),
                imageChangeImageTypeEq(imageType)
            )
            .orderBy(shopImageChangeRequestJpaEntity.id.desc())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
    }

    @Override
    public PageResult<ShopImageChangeRequestResult> findImageChangeRequestPage(
        ApprovalStatus status,
        ShopImageType imageType,
        PageQuery pageQuery
    ) {
        Long total = queryFactory
            .select(shopImageChangeRequestJpaEntity.count())
            .from(shopImageChangeRequestJpaEntity)
            .where(imageChangeStatusEq(status), imageChangeImageTypeEq(imageType))
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ShopImageChangeRequestResult> content = imageChangeRequestProjection()
            .where(imageChangeStatusEq(status), imageChangeImageTypeEq(imageType))
            .orderBy(shopImageChangeRequestJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    private JPQLQuery<ShopImageChangeRequestResult> imageChangeRequestProjection() {
        return queryFactory
            .select(Projections.constructor(ShopImageChangeRequestResult.class,
                shopImageChangeRequestJpaEntity.id,
                shopImageChangeRequestJpaEntity.shopId,
                shopImageChangeRequestJpaEntity.imageType,
                imageChangeRequestImageFile.filePath,
                shopImageChangeRequestJpaEntity.status,
                shopImageChangeRequestJpaEntity.rejectReason
            ))
            .from(shopImageChangeRequestJpaEntity)
            .leftJoin(imageChangeRequestImageFile).on(imageChangeRequestImageFile.id.eq(shopImageChangeRequestJpaEntity.imageFileId));
    }

    private BooleanExpression imageChangeStatusEq(ApprovalStatus status) {
        return status != null ? shopImageChangeRequestJpaEntity.status.eq(status) : null;
    }

    private BooleanExpression imageChangeImageTypeEq(ShopImageType imageType) {
        return imageType != null ? shopImageChangeRequestJpaEntity.imageType.eq(imageType) : null;
    }

    @Override
    public List<ShopSuspensionResult> findSuspensions(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopSuspensionResult.class,
                shopSuspensionJpaEntity.id,
                shopSuspensionJpaEntity.shopId,
                shopSuspensionJpaEntity.reason,
                shopSuspensionJpaEntity.orderMethod,
                shopSuspensionJpaEntity.startAt,
                shopSuspensionJpaEntity.endAt,
                shopSuspensionJpaEntity.releasedAt
            ))
            .from(shopSuspensionJpaEntity)
            .where(shopSuspensionJpaEntity.shopId.eq(shopId))
            .orderBy(shopSuspensionJpaEntity.startAt.desc())
            .fetch();
    }

    @Override
    public List<ShopTemporaryClosureResult> findTemporaryClosures(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopTemporaryClosureResult.class,
                shopTemporaryClosureJpaEntity.id,
                shopTemporaryClosureJpaEntity.shopId,
                shopTemporaryClosureJpaEntity.startDate,
                shopTemporaryClosureJpaEntity.endDate
            ))
            .from(shopTemporaryClosureJpaEntity)
            .where(shopTemporaryClosureJpaEntity.shopId.eq(shopId))
            .orderBy(shopTemporaryClosureJpaEntity.startDate.asc())
            .fetch();
    }

    @Override
    public List<ShopFoodTypeCategoryResult> findVisibleFoodTypeCategories() {
        return queryFactory
            .select(Projections.constructor(ShopFoodTypeCategoryResult.class,
                shopFoodTypeCategoryJpaEntity.id,
                shopFoodTypeCategoryJpaEntity.foodType,
                shopFoodTypeCategoryJpaEntity.displayName,
                activeFile.filePath,
                inactiveFile.filePath,
                shopFoodTypeCategoryJpaEntity.sort,
                shopFoodTypeCategoryJpaEntity.visible
            ))
            .from(shopFoodTypeCategoryJpaEntity)
            .join(activeFile).on(activeFile.id.eq(shopFoodTypeCategoryJpaEntity.activeImageFileId))
            .join(inactiveFile).on(inactiveFile.id.eq(shopFoodTypeCategoryJpaEntity.inactiveImageFileId))
            .where(shopFoodTypeCategoryJpaEntity.visible.eq(true))
            .orderBy(shopFoodTypeCategoryJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(this::withResolvedIconUrls)
            .toList();
    }

    @Override
    public List<ShopAmenityCategoryResult> findVisibleAmenityCategories() {
        return queryFactory
            .select(Projections.constructor(ShopAmenityCategoryResult.class,
                shopAmenityCategoryJpaEntity.id,
                shopAmenityCategoryJpaEntity.amenity,
                shopAmenityCategoryJpaEntity.displayName,
                activeFile.filePath,
                inactiveFile.filePath,
                shopAmenityCategoryJpaEntity.sort,
                shopAmenityCategoryJpaEntity.visible
            ))
            .from(shopAmenityCategoryJpaEntity)
            .join(activeFile).on(activeFile.id.eq(shopAmenityCategoryJpaEntity.activeImageFileId))
            .join(inactiveFile).on(inactiveFile.id.eq(shopAmenityCategoryJpaEntity.inactiveImageFileId))
            .where(shopAmenityCategoryJpaEntity.visible.eq(true))
            .orderBy(shopAmenityCategoryJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(this::withResolvedIconUrls)
            .toList();
    }

    @Override
    public List<ShopAmenityCategoryResult> findAllAmenityCategories() {
        return queryFactory
            .select(Projections.constructor(ShopAmenityCategoryResult.class,
                shopAmenityCategoryJpaEntity.id,
                shopAmenityCategoryJpaEntity.amenity,
                shopAmenityCategoryJpaEntity.displayName,
                activeFile.filePath,
                inactiveFile.filePath,
                shopAmenityCategoryJpaEntity.sort,
                shopAmenityCategoryJpaEntity.visible
            ))
            .from(shopAmenityCategoryJpaEntity)
            .leftJoin(activeFile).on(activeFile.id.eq(shopAmenityCategoryJpaEntity.activeImageFileId))
            .leftJoin(inactiveFile).on(inactiveFile.id.eq(shopAmenityCategoryJpaEntity.inactiveImageFileId))
            .orderBy(shopAmenityCategoryJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(this::withResolvedIconUrls)
            .toList();
    }

    @Override
    public List<ShopFoodTypeCategoryResult> findAllFoodTypeCategories() {
        return queryFactory
            .select(Projections.constructor(ShopFoodTypeCategoryResult.class,
                shopFoodTypeCategoryJpaEntity.id,
                shopFoodTypeCategoryJpaEntity.foodType,
                shopFoodTypeCategoryJpaEntity.displayName,
                activeFile.filePath,
                inactiveFile.filePath,
                shopFoodTypeCategoryJpaEntity.sort,
                shopFoodTypeCategoryJpaEntity.visible
            ))
            .from(shopFoodTypeCategoryJpaEntity)
            .leftJoin(activeFile).on(activeFile.id.eq(shopFoodTypeCategoryJpaEntity.activeImageFileId))
            .leftJoin(inactiveFile).on(inactiveFile.id.eq(shopFoodTypeCategoryJpaEntity.inactiveImageFileId))
            .orderBy(shopFoodTypeCategoryJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(this::withResolvedIconUrls)
            .toList();
    }

    @Override
    public List<ShopAmenityAssignmentResult> findAmenityAssignments(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopAmenityAssignmentResult.class,
                shopAmenityJpaEntity.id,
                shopAmenityJpaEntity.shopAmenityCategoryId,
                shopAmenityCategoryJpaEntity.amenity,
                shopAmenityCategoryJpaEntity.displayName,
                activeFile.filePath
            ))
            .from(shopAmenityJpaEntity)
            .join(shopAmenityCategoryJpaEntity).on(shopAmenityCategoryJpaEntity.id.eq(shopAmenityJpaEntity.shopAmenityCategoryId))
            .join(activeFile).on(activeFile.id.eq(shopAmenityCategoryJpaEntity.activeImageFileId))
            .where(shopAmenityJpaEntity.shopId.eq(shopId))
            .fetch()
            .stream()
            .map(this::withResolvedIconUrl)
            .toList();
    }

    @Override
    public List<ShopAmenityWithCategoryResult> findAmenitiesWithCategory(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopAmenityWithCategoryResult.class,
                shopAmenityCategoryJpaEntity.amenity,
                shopAmenityCategoryJpaEntity.displayName,
                activeFile.filePath
            ))
            .from(shopAmenityJpaEntity)
            .join(shopAmenityCategoryJpaEntity).on(shopAmenityCategoryJpaEntity.id.eq(shopAmenityJpaEntity.shopAmenityCategoryId))
            .join(activeFile).on(activeFile.id.eq(shopAmenityCategoryJpaEntity.activeImageFileId))
            .where(shopAmenityJpaEntity.shopId.eq(shopId))
            .fetch()
            .stream()
            .map(this::withResolvedIconUrl)
            .toList();
    }

    @Override
    public List<ShopFoodTypeAssignmentResult> findFoodTypeAssignments(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopFoodTypeAssignmentResult.class,
                shopFoodTypeJpaEntity.id,
                shopFoodTypeJpaEntity.shopFoodTypeCategoryId,
                shopFoodTypeCategoryJpaEntity.foodType,
                shopFoodTypeCategoryJpaEntity.displayName,
                activeFile.filePath
            ))
            .from(shopFoodTypeJpaEntity)
            .join(shopFoodTypeCategoryJpaEntity).on(shopFoodTypeCategoryJpaEntity.id.eq(shopFoodTypeJpaEntity.shopFoodTypeCategoryId))
            .join(activeFile).on(activeFile.id.eq(shopFoodTypeCategoryJpaEntity.activeImageFileId))
            .where(shopFoodTypeJpaEntity.shopId.eq(shopId))
            .fetch()
            .stream()
            .map(this::withResolvedIconUrl)
            .toList();
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

    @Override
    public List<ShopBannerImageResult> findBannerImages(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopBannerImageResult.class,
                shopBannerImageJpaEntity.id,
                uploadedFileJpaEntity.filePath,
                shopBannerImageJpaEntity.sort
            ))
            .from(shopBannerImageJpaEntity)
            .join(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopBannerImageJpaEntity.imageFileId))
            .where(shopBannerImageJpaEntity.shopId.eq(shopId))
            .orderBy(shopBannerImageJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
    }

    @Override
    public List<ShopMenuCollectionImageResult> findMenuCollectionImages(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopMenuCollectionImageResult.class,
                shopMenuCollectionImageJpaEntity.id,
                menuCollectionImageFile.filePath,
                shopMenuCollectionImageJpaEntity.sort,
                shopMenuCollectionImageJpaEntity.status,
                shopMenuCollectionImageJpaEntity.rejectReason
            ))
            .from(shopMenuCollectionImageJpaEntity)
            .leftJoin(menuCollectionImageFile)
                .on(menuCollectionImageFile.id.eq(shopMenuCollectionImageJpaEntity.imageFileId))
            .where(shopMenuCollectionImageJpaEntity.shopId.eq(shopId))
            .orderBy(shopMenuCollectionImageJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
    }

    @Override
    public List<ShopMenuCollectionImageExposureResult> findExposedMenuCollectionImages(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopMenuCollectionImageExposureResult.class,
                shopMenuCollectionImageJpaEntity.id,
                menuCollectionImageFile.filePath,
                shopMenuCollectionImageJpaEntity.sort
            ))
            .from(shopMenuCollectionImageJpaEntity)
            .leftJoin(menuCollectionImageFile)
                .on(menuCollectionImageFile.id.eq(shopMenuCollectionImageJpaEntity.imageFileId))
            .where(
                shopMenuCollectionImageJpaEntity.shopId.eq(shopId),
                shopMenuCollectionImageJpaEntity.status.eq(ApprovalStatus.APPROVED)
            )
            .orderBy(shopMenuCollectionImageJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
    }

    @Override
    public PageResult<ShopMenuCollectionImageRequestResult> findMenuCollectionImageRequestPage(
        ApprovalStatus status,
        PageQuery pageQuery
    ) {
        Long total = queryFactory
            .select(shopMenuCollectionImageJpaEntity.count())
            .from(shopMenuCollectionImageJpaEntity)
            .where(menuCollectionImageStatusEq(status))
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ShopMenuCollectionImageRequestResult> content = queryFactory
            .select(Projections.constructor(ShopMenuCollectionImageRequestResult.class,
                shopMenuCollectionImageJpaEntity.id,
                shopMenuCollectionImageJpaEntity.shopId,
                shopJpaEntity.name,
                menuCollectionImageFile.filePath,
                shopMenuCollectionImageJpaEntity.sort,
                shopMenuCollectionImageJpaEntity.status,
                shopMenuCollectionImageJpaEntity.rejectReason
            ))
            .from(shopMenuCollectionImageJpaEntity)
            .leftJoin(shopJpaEntity).on(shopJpaEntity.id.eq(shopMenuCollectionImageJpaEntity.shopId))
            .leftJoin(menuCollectionImageFile)
                .on(menuCollectionImageFile.id.eq(shopMenuCollectionImageJpaEntity.imageFileId))
            .where(menuCollectionImageStatusEq(status))
            .orderBy(shopMenuCollectionImageJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    private BooleanExpression menuCollectionImageStatusEq(ApprovalStatus status) {
        return status != null ? shopMenuCollectionImageJpaEntity.status.eq(status) : null;
    }

    @Override
    public List<ShopPhotoCategoryImageResult> findAllPhotoCategoryImages() {
        return photoCategoryImageProjection()
            .orderBy(shopPhotoCategoryImageJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
    }

    @Override
    public List<ShopPhotoCategoryImageManagementResult> findPhotoCategoryImages(Long shopPhotoCategoryId) {
        return queryFactory
            .select(Projections.constructor(ShopPhotoCategoryImageManagementResult.class,
                shopPhotoCategoryImageJpaEntity.id,
                shopPhotoCategoryImageJpaEntity.shopPhotoCategoryId,
                uploadedFileJpaEntity.filePath,
                shopPhotoCategoryImageJpaEntity.sort,
                shopPhotoCategoryImageJpaEntity.visible
            ))
            .from(shopPhotoCategoryImageJpaEntity)
            .join(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopPhotoCategoryImageJpaEntity.imageFileId))
            .where(shopPhotoCategoryImageJpaEntity.shopPhotoCategoryId.eq(shopPhotoCategoryId))
            .orderBy(shopPhotoCategoryImageJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
    }

    @Override
    public List<ShopPhotoCategoryResult> findPhotoCategories(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopPhotoCategoryResult.class,
                shopPhotoCategoryJpaEntity.id,
                shopPhotoCategoryJpaEntity.name
            ))
            .from(shopPhotoCategoryJpaEntity)
            .where(shopPhotoCategoryJpaEntity.shopId.eq(shopId))
            .orderBy(shopPhotoCategoryJpaEntity.id.asc())
            .fetch();
    }

    @Override
    public List<ShopOrderMethodResult> findOrderMethods(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopOrderMethodResult.class,
                shopOrderMethodJpaEntity.id,
                shopOrderMethodJpaEntity.orderMethod
            ))
            .from(shopOrderMethodJpaEntity)
            .where(shopOrderMethodJpaEntity.shopId.eq(shopId))
            .orderBy(shopOrderMethodJpaEntity.id.asc())
            .fetch();
    }

    @Override
    public Optional<ShopOwnerMessageResult> findLatestOwnerMessage(Long shopId) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(ShopOwnerMessageResult.class,
                    shopOwnerMessageHistoryJpaEntity.message,
                    shopOwnerMessageHistoryJpaEntity.createdAt
                ))
                .from(shopOwnerMessageHistoryJpaEntity)
                .where(shopOwnerMessageHistoryJpaEntity.shopId.eq(shopId))
                .orderBy(shopOwnerMessageHistoryJpaEntity.createdAt.desc())
                .fetchFirst()
        );
    }

    @Override
    public List<ShopBusinessHourResult> findBusinessHours(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopBusinessHourResult.class,
                shopBusinessHourJpaEntity.id,
                shopBusinessHourJpaEntity.dayType,
                shopBusinessHourJpaEntity.openTime,
                shopBusinessHourJpaEntity.closeTime,
                shopBusinessHourJpaEntity.isClosed,
                shopBusinessHourJpaEntity.is24Hours
            ))
            .from(shopBusinessHourJpaEntity)
            .where(shopBusinessHourJpaEntity.shopId.eq(shopId))
            .orderBy(shopBusinessHourJpaEntity.dayType.asc())
            .fetch();
    }

    @Override
    public List<ShopBreakTimeResult> findBreakTimes(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopBreakTimeResult.class,
                shopBreakTimeJpaEntity.id,
                shopBreakTimeJpaEntity.dayType,
                shopBreakTimeJpaEntity.startTime,
                shopBreakTimeJpaEntity.endTime
            ))
            .from(shopBreakTimeJpaEntity)
            .where(shopBreakTimeJpaEntity.shopId.eq(shopId))
            .orderBy(shopBreakTimeJpaEntity.dayType.asc())
            .fetch();
    }

    @Override
    public List<ShopClosedDayResult> findClosedDays(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopClosedDayResult.class,
                shopClosedDayJpaEntity.id,
                shopClosedDayJpaEntity.closedDayType
            ))
            .from(shopClosedDayJpaEntity)
            .where(shopClosedDayJpaEntity.shopId.eq(shopId))
            .orderBy(shopClosedDayJpaEntity.id.asc())
            .fetch();
    }

    private JPQLQuery<ShopPhotoCategoryImageResult> photoCategoryImageProjection() {
        return queryFactory
            .select(Projections.constructor(ShopPhotoCategoryImageResult.class,
                shopPhotoCategoryImageJpaEntity.id,
                shopPhotoCategoryImageJpaEntity.shopPhotoCategoryId,
                uploadedFileJpaEntity.filePath,
                shopPhotoCategoryImageJpaEntity.sort
            ))
            .from(shopPhotoCategoryImageJpaEntity)
            .join(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopPhotoCategoryImageJpaEntity.imageFileId));
    }

    private ShopFoodTypeCategoryResult withResolvedIconUrls(ShopFoodTypeCategoryResult row) {
        return new ShopFoodTypeCategoryResult(
            row.id(),
            row.foodType(),
            row.displayName(),
            fileUrlResolver.resolve(row.activeIconUrl()),
            fileUrlResolver.resolve(row.inactiveIconUrl()),
            row.sort(),
            row.visible()
        );
    }

    private ShopAmenityCategoryResult withResolvedIconUrls(ShopAmenityCategoryResult row) {
        return new ShopAmenityCategoryResult(
            row.id(),
            row.amenity(),
            row.displayName(),
            fileUrlResolver.resolve(row.activeIconUrl()),
            fileUrlResolver.resolve(row.inactiveIconUrl()),
            row.sort(),
            row.visible()
        );
    }

    private ShopAmenityAssignmentResult withResolvedIconUrl(ShopAmenityAssignmentResult row) {
        return new ShopAmenityAssignmentResult(
            row.id(),
            row.amenityCategoryId(),
            row.amenity(),
            row.displayName(),
            fileUrlResolver.resolve(row.activeIconUrl())
        );
    }

    private ShopAmenityWithCategoryResult withResolvedIconUrl(ShopAmenityWithCategoryResult row) {
        return new ShopAmenityWithCategoryResult(
            row.amenity(),
            row.displayName(),
            fileUrlResolver.resolve(row.activeIconUrl())
        );
    }

    private ShopFoodTypeAssignmentResult withResolvedIconUrl(ShopFoodTypeAssignmentResult row) {
        return new ShopFoodTypeAssignmentResult(
            row.id(),
            row.foodTypeCategoryId(),
            row.foodType(),
            row.displayName(),
            fileUrlResolver.resolve(row.activeIconUrl())
        );
    }

    private ShopContentBoardResult withResolvedImageUrl(ShopContentBoardResult row) {
        return new ShopContentBoardResult(
            row.id(),
            row.shopId(),
            row.contentType(),
            row.topic(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.youtubeUrl(),
            row.description(),
            row.hidden(),
            row.createdAt()
        );
    }

    private ShopImageChangeRequestResult withResolvedImageUrl(ShopImageChangeRequestResult row) {
        return new ShopImageChangeRequestResult(
            row.id(),
            row.shopId(),
            row.imageType(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.status(),
            row.rejectReason()
        );
    }

    private ShopBannerImageResult withResolvedImageUrl(ShopBannerImageResult row) {
        return new ShopBannerImageResult(
            row.id(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.sort()
        );
    }

    private ShopPhotoCategoryImageResult withResolvedImageUrl(ShopPhotoCategoryImageResult row) {
        return new ShopPhotoCategoryImageResult(
            row.id(),
            row.shopPhotoCategoryId(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.sort()
        );
    }

    private ShopPhotoCategoryImageManagementResult withResolvedImageUrl(ShopPhotoCategoryImageManagementResult row) {
        return new ShopPhotoCategoryImageManagementResult(
            row.id(),
            row.shopPhotoCategoryId(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.sort(),
            row.visible()
        );
    }

    private ShopMenuCollectionImageResult withResolvedImageUrl(ShopMenuCollectionImageResult row) {
        return new ShopMenuCollectionImageResult(
            row.id(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.sort(),
            row.status(),
            row.rejectReason()
        );
    }

    private ShopMenuCollectionImageExposureResult withResolvedImageUrl(ShopMenuCollectionImageExposureResult row) {
        return new ShopMenuCollectionImageExposureResult(
            row.id(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.sort()
        );
    }

    private ShopMenuCollectionImageRequestResult withResolvedImageUrl(ShopMenuCollectionImageRequestResult row) {
        return new ShopMenuCollectionImageRequestResult(
            row.id(),
            row.shopId(),
            row.shopName(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.sort(),
            row.status(),
            row.rejectReason()
        );
    }

    private ShopImageUrlsResult withResolvedImageUrls(ShopImageUrlsResult row) {
        return new ShopImageUrlsResult(
            row.shopId(),
            fileUrlResolver.resolve(row.thumbnailImageUrl()),
            fileUrlResolver.resolve(row.trademarkImageUrl())
        );
    }

    @Override
    public Optional<ShopVisibleDetailResult> findVisibleDetailById(Long shopId) {
        ShopVisibleDetailResult result = queryFactory
            .select(Projections.constructor(ShopVisibleDetailResult.class,
                shopJpaEntity.id,
                shopJpaEntity.name,
                shopJpaEntity.latitude,
                shopJpaEntity.longitude,
                shopJpaEntity.rating,
                shopJpaEntity.roadAddress,
                shopJpaEntity.lotAddress,
                shopJpaEntity.phoneNumber,
                shopJpaEntity.minOrderAmount,
                shopJpaEntity.scheduledOrderEnabled
            ))
            .from(shopJpaEntity)
            .where(
                shopJpaEntity.id.eq(shopId),
                shopJpaEntity.permanentlyClosed.isFalse(),
                shopJpaEntity.hidden.isFalse()
            )
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public boolean existsBookmark(Long shopId, Long memberId) {
        Integer found = queryFactory
            .selectOne()
            .from(shopBookmarkJpaEntity)
            .where(
                shopBookmarkJpaEntity.shopId.eq(shopId),
                shopBookmarkJpaEntity.memberId.eq(memberId)
            )
            .fetchFirst();

        return found != null;
    }

    @Override
    public Optional<ShopManagementDetailResult> findManagementDetailById(Long shopId) {
        ShopManagementDetailResult result = queryFactory
            .select(Projections.constructor(ShopManagementDetailResult.class,
                shopJpaEntity.id,
                shopJpaEntity.stationId,
                shopJpaEntity.name,
                shopJpaEntity.latitude,
                shopJpaEntity.longitude,
                shopJpaEntity.rating,
                shopJpaEntity.roadAddress,
                shopJpaEntity.lotAddress,
                shopJpaEntity.phoneNumber,
                shopJpaEntity.permanentlyClosed,
                shopJpaEntity.cupDepositEnabled,
                shopJpaEntity.createdAt,
                shopJpaEntity.updatedAt
            ))
            .from(shopJpaEntity)
            .where(shopJpaEntity.id.eq(shopId))
            .fetchOne();

        return Optional.ofNullable(result);
    }
}
