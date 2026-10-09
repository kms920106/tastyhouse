package com.tastyhouse.infrastructure.jpa.shop.query;

import java.util.List;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopBannerImageResult;
import com.tastyhouse.application.shop.port.out.ShopContentBoardResult;
import com.tastyhouse.application.shop.port.out.ShopImageChangeRequestResult;
import com.tastyhouse.application.shop.port.out.ShopMediaManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopMediaOwnerQueryPort;
import com.tastyhouse.application.shop.port.out.ShopMediaQueryPort;
import com.tastyhouse.application.shop.port.out.ShopMenuCollectionImageExposureResult;
import com.tastyhouse.application.shop.port.out.ShopMenuCollectionImageRequestResult;
import com.tastyhouse.application.shop.port.out.ShopMenuCollectionImageResult;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryImageManagementResult;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryImageResult;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryResult;
import com.tastyhouse.infrastructure.jpa.file.persistence.QUploadedFileJpaEntity;
import com.tastyhouse.infrastructure.jpa.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.jpa.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopBannerImageJpaEntity.shopBannerImageJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopContentBoardJpaEntity.shopContentBoardJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopImageChangeRequestJpaEntity.shopImageChangeRequestJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopJpaEntity.shopJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopMenuCollectionImageJpaEntity.shopMenuCollectionImageJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopPhotoCategoryImageJpaEntity.shopPhotoCategoryImageJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopPhotoCategoryJpaEntity.shopPhotoCategoryJpaEntity;

@Repository
class ShopMediaQueryAdapter implements ShopMediaQueryPort, ShopMediaManagementQueryPort, ShopMediaOwnerQueryPort {

    private static final QUploadedFileJpaEntity contentBoardImageFile = new QUploadedFileJpaEntity("contentBoardImageFile");
    private static final QUploadedFileJpaEntity imageChangeRequestImageFile = new QUploadedFileJpaEntity("imageChangeRequestImageFile");

    private static final QUploadedFileJpaEntity menuCollectionImageFile = new QUploadedFileJpaEntity("menuCollectionImageFile");

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ShopMediaQueryAdapter(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
    public List<ShopContentBoardResult> findContentBoards(Long shopId) {
        return contentBoardProjection()
            .where(shopContentBoardJpaEntity.shopId.eq(shopId))
            .orderBy(shopContentBoardJpaEntity.id.asc())
            .fetch();
    }

    @Override
    public PageResult<ShopContentBoardResult> findContentBoardPage(
        Long shopId,
        Boolean hidden,
        String contentType,
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
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    private JPQLQuery<ShopContentBoardResult> contentBoardProjection() {
        return queryFactory
            .select(Projections.constructor(ShopContentBoardResult.class,
                shopContentBoardJpaEntity.id,
                shopContentBoardJpaEntity.shopId,
                shopContentBoardJpaEntity.contentType.stringValue(),
                shopContentBoardJpaEntity.topic.stringValue(),
                fileUrlResolver.urlOf(contentBoardImageFile.filePath),
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

    private BooleanExpression contentBoardContentTypeEq(String contentType) {
        return contentType != null ? shopContentBoardJpaEntity.contentType.eq(contentType) : null;
    }

    @Override
    public List<ShopImageChangeRequestResult> findImageChangeRequests(Long shopId, String imageType) {
        return imageChangeRequestProjection()
            .where(
                shopImageChangeRequestJpaEntity.shopId.eq(shopId),
                imageChangeImageTypeEq(imageType)
            )
            .orderBy(shopImageChangeRequestJpaEntity.id.desc())
            .fetch();
    }

    @Override
    public PageResult<ShopImageChangeRequestResult> findImageChangeRequestPage(
        String status,
        String imageType,
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
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    private JPQLQuery<ShopImageChangeRequestResult> imageChangeRequestProjection() {
        return queryFactory
            .select(Projections.constructor(ShopImageChangeRequestResult.class,
                shopImageChangeRequestJpaEntity.id,
                shopImageChangeRequestJpaEntity.shopId,
                shopImageChangeRequestJpaEntity.imageType.stringValue(),
                fileUrlResolver.urlOf(imageChangeRequestImageFile.filePath),
                shopImageChangeRequestJpaEntity.status.stringValue(),
                shopImageChangeRequestJpaEntity.rejectReason
            ))
            .from(shopImageChangeRequestJpaEntity)
            .leftJoin(imageChangeRequestImageFile).on(imageChangeRequestImageFile.id.eq(shopImageChangeRequestJpaEntity.imageFileId));
    }

    private BooleanExpression imageChangeStatusEq(String status) {
        return status != null ? shopImageChangeRequestJpaEntity.status.eq(status) : null;
    }

    private BooleanExpression imageChangeImageTypeEq(String imageType) {
        return imageType != null ? shopImageChangeRequestJpaEntity.imageType.eq(imageType) : null;
    }

    @Override
    public List<ShopBannerImageResult> findBannerImages(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopBannerImageResult.class,
                shopBannerImageJpaEntity.id,
                fileUrlResolver.urlOf(uploadedFileJpaEntity.filePath),
                shopBannerImageJpaEntity.sort
            ))
            .from(shopBannerImageJpaEntity)
            .join(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopBannerImageJpaEntity.imageFileId))
            .where(shopBannerImageJpaEntity.shopId.eq(shopId))
            .orderBy(shopBannerImageJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public List<ShopMenuCollectionImageResult> findMenuCollectionImages(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopMenuCollectionImageResult.class,
                shopMenuCollectionImageJpaEntity.id,
                fileUrlResolver.urlOf(menuCollectionImageFile.filePath),
                shopMenuCollectionImageJpaEntity.sort,
                shopMenuCollectionImageJpaEntity.status.stringValue(),
                shopMenuCollectionImageJpaEntity.rejectReason
            ))
            .from(shopMenuCollectionImageJpaEntity)
            .leftJoin(menuCollectionImageFile)
                .on(menuCollectionImageFile.id.eq(shopMenuCollectionImageJpaEntity.imageFileId))
            .where(shopMenuCollectionImageJpaEntity.shopId.eq(shopId))
            .orderBy(shopMenuCollectionImageJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public List<ShopMenuCollectionImageExposureResult> findMenuCollectionImagesByStatus(Long shopId, String status) {
        return queryFactory
            .select(Projections.constructor(ShopMenuCollectionImageExposureResult.class,
                shopMenuCollectionImageJpaEntity.id,
                fileUrlResolver.urlOf(menuCollectionImageFile.filePath),
                shopMenuCollectionImageJpaEntity.sort
            ))
            .from(shopMenuCollectionImageJpaEntity)
            .leftJoin(menuCollectionImageFile)
                .on(menuCollectionImageFile.id.eq(shopMenuCollectionImageJpaEntity.imageFileId))
            .where(
                shopMenuCollectionImageJpaEntity.shopId.eq(shopId),
                shopMenuCollectionImageJpaEntity.status.eq(status)
            )
            .orderBy(shopMenuCollectionImageJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public PageResult<ShopMenuCollectionImageRequestResult> findMenuCollectionImageRequestPage(
        String status,
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
                fileUrlResolver.urlOf(menuCollectionImageFile.filePath),
                shopMenuCollectionImageJpaEntity.sort,
                shopMenuCollectionImageJpaEntity.status.stringValue(),
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
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    private BooleanExpression menuCollectionImageStatusEq(String status) {
        return status != null ? shopMenuCollectionImageJpaEntity.status.eq(status) : null;
    }

    @Override
    public List<ShopPhotoCategoryImageResult> findAllPhotoCategoryImages() {
        return photoCategoryImageProjection()
            .orderBy(shopPhotoCategoryImageJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public List<ShopPhotoCategoryImageManagementResult> findPhotoCategoryImages(Long shopPhotoCategoryId) {
        return queryFactory
            .select(Projections.constructor(ShopPhotoCategoryImageManagementResult.class,
                shopPhotoCategoryImageJpaEntity.id,
                shopPhotoCategoryImageJpaEntity.shopPhotoCategoryId,
                fileUrlResolver.urlOf(uploadedFileJpaEntity.filePath),
                shopPhotoCategoryImageJpaEntity.sort,
                shopPhotoCategoryImageJpaEntity.visible
            ))
            .from(shopPhotoCategoryImageJpaEntity)
            .join(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopPhotoCategoryImageJpaEntity.imageFileId))
            .where(shopPhotoCategoryImageJpaEntity.shopPhotoCategoryId.eq(shopPhotoCategoryId))
            .orderBy(shopPhotoCategoryImageJpaEntity.sort.asc())
            .fetch();
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

    private JPQLQuery<ShopPhotoCategoryImageResult> photoCategoryImageProjection() {
        return queryFactory
            .select(Projections.constructor(ShopPhotoCategoryImageResult.class,
                shopPhotoCategoryImageJpaEntity.id,
                shopPhotoCategoryImageJpaEntity.shopPhotoCategoryId,
                fileUrlResolver.urlOf(uploadedFileJpaEntity.filePath),
                shopPhotoCategoryImageJpaEntity.sort
            ))
            .from(shopPhotoCategoryImageJpaEntity)
            .join(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopPhotoCategoryImageJpaEntity.imageFileId));
    }
}
