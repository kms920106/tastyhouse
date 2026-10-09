package com.tastyhouse.infrastructure.jpa.shop.query;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopBreakTimeResult;
import com.tastyhouse.application.shop.port.out.ShopBusinessHourResult;
import com.tastyhouse.application.shop.port.out.ShopClosedDayResult;
import com.tastyhouse.application.shop.port.out.ShopConvenienceInfoResult;
import com.tastyhouse.application.shop.port.out.ShopHygieneBadgeResult;
import com.tastyhouse.application.shop.port.out.ShopImageUrlsResult;
import com.tastyhouse.application.shop.port.out.ShopManagementDetailResult;
import com.tastyhouse.application.shop.port.out.ShopManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopOrderMethodResult;
import com.tastyhouse.application.shop.port.out.ShopOriginInfoResult;
import com.tastyhouse.application.shop.port.out.ShopOwnerMessageResult;
import com.tastyhouse.application.shop.port.out.ShopOwnerQueryPort;
import com.tastyhouse.application.shop.port.out.ShopPhoneNumberResult;
import com.tastyhouse.application.shop.port.out.ShopQueryPort;
import com.tastyhouse.application.shop.port.out.ShopSuspensionResult;
import com.tastyhouse.application.shop.port.out.ShopTemporaryClosureResult;
import com.tastyhouse.application.shop.port.out.ShopVisibleDetailResult;
import com.tastyhouse.infrastructure.jpa.file.persistence.QUploadedFileJpaEntity;
import com.tastyhouse.infrastructure.jpa.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopBookmarkJpaEntity.shopBookmarkJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopBreakTimeJpaEntity.shopBreakTimeJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopBusinessHourJpaEntity.shopBusinessHourJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopClosedDayJpaEntity.shopClosedDayJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopConvenienceInfoJpaEntity.shopConvenienceInfoJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopHygieneBadgeJpaEntity.shopHygieneBadgeJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopJpaEntity.shopJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopOrderMethodJpaEntity.shopOrderMethodJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopOriginInfoJpaEntity.shopOriginInfoJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopOwnerMessageHistoryJpaEntity.shopOwnerMessageHistoryJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopPhoneNumberJpaEntity.shopPhoneNumberJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopSuspensionJpaEntity.shopSuspensionJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopTemporaryClosureJpaEntity.shopTemporaryClosureJpaEntity;

@Repository
class ShopQueryAdapter implements ShopQueryPort, ShopBasicInfoQueryPort, ShopManagementQueryPort, ShopOwnerQueryPort {

    private static final QUploadedFileJpaEntity shopThumbnailFile = new QUploadedFileJpaEntity("shopThumbnailFile");
    private static final QUploadedFileJpaEntity shopTrademarkFile = new QUploadedFileJpaEntity("shopTrademarkFile");

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ShopQueryAdapter(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
    public Optional<String> findShopName(Long shopId) {
        String result = queryFactory
            .select(shopJpaEntity.name)
            .from(shopJpaEntity)
            .where(shopJpaEntity.id.eq(shopId))
            .fetchOne();
        return Optional.ofNullable(result);
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
        ShopImageUrlsResult result = queryFactory
            .select(Projections.constructor(ShopImageUrlsResult.class,
                shopJpaEntity.id,
                fileUrlResolver.urlOf(shopThumbnailFile.filePath),
                fileUrlResolver.urlOf(shopTrademarkFile.filePath)
            ))
            .from(shopJpaEntity)
            .leftJoin(shopThumbnailFile).on(shopThumbnailFile.id.eq(shopJpaEntity.thumbnailImageFileId))
            .leftJoin(shopTrademarkFile).on(shopTrademarkFile.id.eq(shopJpaEntity.trademarkImageFileId))
            .where(shopJpaEntity.id.eq(shopId))
            .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<ShopConvenienceInfoResult> findConvenienceInfo(Long shopId) {
        ShopConvenienceInfoResult result = queryFactory
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
            .fetchFirst();
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<ShopOriginInfoResult> findOriginInfo(Long shopId) {
        ShopOriginInfoResult result = queryFactory
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
            .fetchFirst();
        return Optional.ofNullable(result);
    }

    @Override
    public List<ShopHygieneBadgeResult> findHygieneBadges(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopHygieneBadgeResult.class,
                shopHygieneBadgeJpaEntity.id,
                shopHygieneBadgeJpaEntity.shopId,
                shopHygieneBadgeJpaEntity.badgeType.stringValue(),
                shopHygieneBadgeJpaEntity.certifiedDate,
                shopHygieneBadgeJpaEntity.lastInspectionMonth
            ))
            .from(shopHygieneBadgeJpaEntity)
            .where(shopHygieneBadgeJpaEntity.shopId.eq(shopId))
            .orderBy(shopHygieneBadgeJpaEntity.certifiedDate.desc())
            .fetch();
    }

    @Override
    public List<ShopSuspensionResult> findSuspensions(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopSuspensionResult.class,
                shopSuspensionJpaEntity.id,
                shopSuspensionJpaEntity.shopId,
                shopSuspensionJpaEntity.reason.stringValue(),
                shopSuspensionJpaEntity.orderMethod.stringValue(),
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
    public List<ShopOrderMethodResult> findOrderMethods(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopOrderMethodResult.class,
                shopOrderMethodJpaEntity.id,
                shopOrderMethodJpaEntity.orderMethod,
                Expressions.nullExpression(String.class)
            ))
            .from(shopOrderMethodJpaEntity)
            .where(shopOrderMethodJpaEntity.shopId.eq(shopId))
            .orderBy(shopOrderMethodJpaEntity.id.asc())
            .fetch();
    }

    @Override
    public Optional<ShopOwnerMessageResult> findLatestOwnerMessage(Long shopId) {
        ShopOwnerMessageResult result = queryFactory
            .select(Projections.constructor(ShopOwnerMessageResult.class,
                shopOwnerMessageHistoryJpaEntity.message,
                shopOwnerMessageHistoryJpaEntity.createdAt
            ))
            .from(shopOwnerMessageHistoryJpaEntity)
            .where(shopOwnerMessageHistoryJpaEntity.shopId.eq(shopId))
            .orderBy(shopOwnerMessageHistoryJpaEntity.createdAt.desc())
            .fetchFirst();
        return Optional.ofNullable(result);
    }

    @Override
    public List<ShopBusinessHourResult> findBusinessHours(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopBusinessHourResult.class,
                shopBusinessHourJpaEntity.id,
                shopBusinessHourJpaEntity.dayType,
                Expressions.nullExpression(String.class),
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
                Expressions.nullExpression(String.class),
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
                shopClosedDayJpaEntity.closedDayType,
                Expressions.nullExpression(String.class)
            ))
            .from(shopClosedDayJpaEntity)
            .where(shopClosedDayJpaEntity.shopId.eq(shopId))
            .orderBy(shopClosedDayJpaEntity.id.asc())
            .fetch();
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
        return queryFactory
            .selectOne()
            .from(shopBookmarkJpaEntity)
            .where(
                shopBookmarkJpaEntity.shopId.eq(shopId),
                shopBookmarkJpaEntity.memberId.eq(memberId)
            )
            .fetchFirst() != null;
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
