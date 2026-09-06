package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.application.shop.port.out.ShopSearchManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopSearchQueryPort;
import com.tastyhouse.application.shop.port.out.BestShopItemResult;
import com.tastyhouse.application.shop.port.out.LatestShopItemResult;
import com.tastyhouse.application.shop.port.out.ShopBookmarkedItemResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipRangeResult;
import com.tastyhouse.application.shop.port.out.ShopListItemResult;
import com.tastyhouse.application.shop.port.out.ShopMapMarkerResult;
import com.tastyhouse.application.shop.port.out.ShopSearchCondition;
import com.querydsl.core.types.Projections;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.model.Amenity;
import com.tastyhouse.domain.shop.model.FoodType;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;
import com.tastyhouse.infrastructure.shop.persistence.ShopJpaEntity;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewJpaEntity.reviewJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopAmenityCategoryJpaEntity.shopAmenityCategoryJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopAmenityJpaEntity.shopAmenityJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopBookmarkJpaEntity.shopBookmarkJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopDeliveryAreaJpaEntity.shopDeliveryAreaJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopFoodTypeCategoryJpaEntity.shopFoodTypeCategoryJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopFoodTypeJpaEntity.shopFoodTypeJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QStationJpaEntity.stationJpaEntity;

@Repository
public class ShopSearchQueryDao implements ShopSearchQueryPort, ShopSearchManagementQueryPort {
    private static final double MAP_MARKER_RADIUS_METERS = 200.0;
    private static final double METERS_PER_DEGREE = 111000.0;

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;
    private final ShopDeliveryTipQueryDao shopDeliveryTipQueryDao;

    public ShopSearchQueryDao(
        JPAQueryFactory queryFactory,
        FileUrlResolver fileUrlResolver,
        ShopDeliveryTipQueryDao shopDeliveryTipQueryDao
    ) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
        this.shopDeliveryTipQueryDao = shopDeliveryTipQueryDao;
    }

    @Override
    public List<ShopMapMarkerResult> findNearbyShops(BigDecimal latitude, BigDecimal longitude) {
        BigDecimal degreeDiff = BigDecimal.valueOf(MAP_MARKER_RADIUS_METERS / METERS_PER_DEGREE);

        return queryFactory
            .select(Projections.constructor(ShopMapMarkerResult.class,
                shopJpaEntity.id,
                shopJpaEntity.latitude,
                shopJpaEntity.longitude,
                shopJpaEntity.name
            ))
            .from(shopJpaEntity)
            .where(
                shopJpaEntity.latitude.between(latitude.subtract(degreeDiff), latitude.add(degreeDiff)),
                shopJpaEntity.longitude.between(longitude.subtract(degreeDiff), longitude.add(degreeDiff)),
                shopJpaEntity.permanentlyClosed.eq(false),
                shopJpaEntity.hidden.eq(false)
            )
            .fetch();
    }

    @Override
    public PageResult<BestShopItemResult> findBestShops(Long deliveryAdminDongId, PageQuery pageQuery) {
        BooleanExpression[] conditions = {
            shopJpaEntity.rating.isNotNull(),
            shopJpaEntity.permanentlyClosed.eq(false),
            shopJpaEntity.hidden.eq(false),
            deliveryAreaCovers(deliveryAdminDongId)
        };

        Long total = queryFactory.select(shopJpaEntity.count()).from(shopJpaEntity).where(conditions).fetchOne();
        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ShopJpaEntity> pagedShops = queryFactory.selectFrom(shopJpaEntity)
            .where(conditions)
            .orderBy(shopJpaEntity.rating.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        if (pagedShops.isEmpty()) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<Long> shopIds = pagedShops.stream().map(ShopJpaEntity::getId).toList();
        var stationMap = stationNamesByShopId(shopIds);
        var thumbnailMap = thumbnailUrlsByShopId(shopIds);
        var foodTypeMap = foodTypesByShopId(shopIds);
        var tipRangeMap = shopDeliveryTipQueryDao.findTipRanges(shopIds);

        List<BestShopItemResult> content = pagedShops.stream()
            .map(shop -> new BestShopItemResult(
                shop.getId(),
                shop.getName(),
                stationMap.get(shop.getId()),
                shop.getRating(),
                thumbnailMap.get(shop.getId()),
                foodTypeMap.getOrDefault(shop.getId(), List.of()),
                shop.getMinOrderAmount(),
                minDeliveryTip(tipRangeMap, shop.getId()),
                maxDeliveryTip(tipRangeMap, shop.getId())
            ))
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<LatestShopItemResult> findLatestShops(
        Long stationId,
        List<FoodType> foodTypes,
        List<Amenity> amenities,
        Long deliveryAdminDongId,
        PageQuery pageQuery
    ) {
        Set<Long> foodTypeShopIds = null;
        if (foodTypes != null && !foodTypes.isEmpty()) {
            foodTypeShopIds = new HashSet<>(queryFactory
                .select(shopFoodTypeJpaEntity.shopId)
                .from(shopFoodTypeJpaEntity)
                .join(shopFoodTypeCategoryJpaEntity).on(shopFoodTypeJpaEntity.shopFoodTypeCategoryId.eq(shopFoodTypeCategoryJpaEntity.id))
                .where(shopFoodTypeCategoryJpaEntity.foodType.in(foodTypes))
                .fetch());

            if (foodTypeShopIds.isEmpty()) {
                return PageResult.empty(pageQuery.page(), pageQuery.size());
            }
        }

        Set<Long> amenityShopIds = null;
        if (amenities != null && !amenities.isEmpty()) {
            amenityShopIds = new HashSet<>(queryFactory
                .select(shopAmenityJpaEntity.shopId)
                .from(shopAmenityJpaEntity)
                .join(shopAmenityCategoryJpaEntity).on(shopAmenityJpaEntity.shopAmenityCategoryId.eq(shopAmenityCategoryJpaEntity.id))
                .where(shopAmenityCategoryJpaEntity.amenity.in(amenities))
                .groupBy(shopAmenityJpaEntity.shopId)
                .having(shopAmenityJpaEntity.shopId.count().goe((long) amenities.size()))
                .fetch());

            if (amenityShopIds.isEmpty()) {
                return PageResult.empty(pageQuery.page(), pageQuery.size());
            }
        }

        Set<Long> filteredShopIds = intersect(foodTypeShopIds, amenityShopIds);
        if (foodTypeShopIds != null && amenityShopIds != null && filteredShopIds.isEmpty()) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        Long total = queryFactory.select(shopJpaEntity.count()).from(shopJpaEntity)
            .where(
                shopJpaEntity.permanentlyClosed.eq(false),
                shopJpaEntity.hidden.eq(false),
                stationIdEq(stationId),
                shopIdIn(filteredShopIds),
                deliveryAreaCovers(deliveryAdminDongId)
            )
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ShopJpaEntity> pagedShops = queryFactory.selectFrom(shopJpaEntity)
            .where(
                shopJpaEntity.permanentlyClosed.eq(false),
                shopJpaEntity.hidden.eq(false),
                stationIdEq(stationId),
                shopIdIn(filteredShopIds),
                deliveryAreaCovers(deliveryAdminDongId)
            )
            .orderBy(shopJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        if (pagedShops.isEmpty()) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<Long> shopIds = pagedShops.stream().map(ShopJpaEntity::getId).toList();
        var stationMap = stationNamesByShopId(shopIds);
        var thumbnailMap = thumbnailUrlsByShopId(shopIds);
        var foodTypeMap = foodTypesByShopId(shopIds);
        var reviewCountMap = reviewCountsByShopId(shopIds);
        var bookmarkCountMap = bookmarkCountsByShopId(shopIds);
        var tipRangeMap = shopDeliveryTipQueryDao.findTipRanges(shopIds);

        List<LatestShopItemResult> content = pagedShops.stream()
            .map(shop -> new LatestShopItemResult(
                shop.getId(),
                shop.getName(),
                stationMap.get(shop.getId()),
                shop.getRating(),
                thumbnailMap.get(shop.getId()),
                shop.getCreatedAt(),
                reviewCountMap.getOrDefault(shop.getId(), 0L),
                bookmarkCountMap.getOrDefault(shop.getId(), 0L),
                foodTypeMap.getOrDefault(shop.getId(), List.of()),
                shop.getMinOrderAmount(),
                minDeliveryTip(tipRangeMap, shop.getId()),
                maxDeliveryTip(tipRangeMap, shop.getId())
            ))
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<ShopBookmarkedItemResult> searchByKeywordWithBookmark(
        String keyword,
        Long memberId,
        Long deliveryAdminDongId,
        PageQuery pageQuery
    ) {
        BooleanExpression[] conditions = {
            shopJpaEntity.permanentlyClosed.eq(false),
            shopJpaEntity.hidden.eq(false),
            shopJpaEntity.name.containsIgnoreCase(keyword),
            deliveryAreaCovers(deliveryAdminDongId)
        };

        Long total = queryFactory.select(shopJpaEntity.count()).from(shopJpaEntity).where(conditions).fetchOne();
        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ShopJpaEntity> pagedShops = queryFactory.selectFrom(shopJpaEntity)
            .where(conditions)
            .orderBy(shopJpaEntity.rating.desc().nullsLast())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        if (pagedShops.isEmpty()) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<Long> shopIds = pagedShops.stream().map(ShopJpaEntity::getId).toList();
        var stationMap = stationNamesByShopId(shopIds);
        var thumbnailMap = thumbnailUrlsByShopId(shopIds);

        Set<Long> bookmarkedShopIds = memberId == null
            ? Set.of()
            : new HashSet<>(queryFactory
                .select(shopBookmarkJpaEntity.shopId)
                .from(shopBookmarkJpaEntity)
                .where(shopBookmarkJpaEntity.shopId.in(shopIds), shopBookmarkJpaEntity.memberId.eq(memberId))
                .fetch());

        var tipRangeMap = shopDeliveryTipQueryDao.findTipRanges(shopIds);

        List<ShopBookmarkedItemResult> content = pagedShops.stream()
            .map(shop -> new ShopBookmarkedItemResult(
                shop.getId(),
                null,
                shop.getName(),
                stationMap.get(shop.getId()),
                shop.getRating(),
                thumbnailMap.get(shop.getId()),
                bookmarkedShopIds.contains(shop.getId()),
                shop.getMinOrderAmount(),
                minDeliveryTip(tipRangeMap, shop.getId()),
                maxDeliveryTip(tipRangeMap, shop.getId())
            ))
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<ShopBookmarkedItemResult> findMyBookmarkedShops(Long memberId, PageQuery pageQuery) {
        Long total = queryFactory
            .select(shopBookmarkJpaEntity.count())
            .from(shopBookmarkJpaEntity)
            .join(shopJpaEntity).on(shopBookmarkJpaEntity.shopId.eq(shopJpaEntity.id)
                .and(shopJpaEntity.permanentlyClosed.eq(false))
                .and(shopJpaEntity.hidden.eq(false)))
            .where(shopBookmarkJpaEntity.memberId.eq(memberId))
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ShopBookmarkedItemResult> rows = queryFactory
            .select(Projections.constructor(ShopBookmarkedItemResult.class,
                shopJpaEntity.id,
                shopBookmarkJpaEntity.id,
                shopJpaEntity.name,
                stationJpaEntity.stationName,
                shopJpaEntity.rating,
                uploadedFileJpaEntity.filePath,
                Expressions.asBoolean(true),
                shopJpaEntity.minOrderAmount,
                Expressions.asNumber(0),
                Expressions.asNumber(0)
            ))
            .from(shopBookmarkJpaEntity)
            .join(shopJpaEntity).on(shopBookmarkJpaEntity.shopId.eq(shopJpaEntity.id)
                .and(shopJpaEntity.permanentlyClosed.eq(false))
                .and(shopJpaEntity.hidden.eq(false)))
            .join(stationJpaEntity).on(shopJpaEntity.stationId.eq(stationJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopJpaEntity.thumbnailImageFileId))
            .where(shopBookmarkJpaEntity.memberId.eq(memberId))
            .orderBy(shopBookmarkJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        var tipRangeMap = shopDeliveryTipQueryDao.findTipRanges(
            rows.stream().map(ShopBookmarkedItemResult::shopId).toList()
        );

        List<ShopBookmarkedItemResult> content = rows.stream()
            .map(row -> withResolvedImageUrlAndTipRange(row, tipRangeMap))
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<ShopListItemResult> findShops(ShopSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(shopJpaEntity.count())
            .from(shopJpaEntity)
            .where(
                nameContains(condition.name()),
                stationIdEq(condition.stationId()),
                permanentlyClosedEq(condition.permanentlyClosed()),
                ceoIdEq(condition.ceoId())
            )
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ShopListItemResult> content = queryFactory
            .select(Projections.constructor(ShopListItemResult.class,
                shopJpaEntity.id,
                shopJpaEntity.name,
                stationJpaEntity.stationName,
                shopJpaEntity.roadAddress,
                shopJpaEntity.rating,
                shopJpaEntity.permanentlyClosed
            ))
            .from(shopJpaEntity)
            .leftJoin(stationJpaEntity).on(stationJpaEntity.id.eq(shopJpaEntity.stationId))
            .where(
                nameContains(condition.name()),
                stationIdEq(condition.stationId()),
                permanentlyClosedEq(condition.permanentlyClosed()),
                ceoIdEq(condition.ceoId())
            )
            .orderBy(shopJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    private Map<Long, String> stationNamesByShopId(List<Long> shopIds) {
        return queryFactory
            .select(shopJpaEntity.id, stationJpaEntity.stationName)
            .from(shopJpaEntity)
            .join(stationJpaEntity).on(stationJpaEntity.id.eq(shopJpaEntity.stationId))
            .where(shopJpaEntity.id.in(shopIds))
            .fetch()
            .stream()
            .collect(Collectors.toMap(
                tuple -> Objects.requireNonNull(tuple.get(shopJpaEntity.id)),
                tuple -> Objects.requireNonNull(tuple.get(stationJpaEntity.stationName))
            ));
    }

    private Map<Long, String> thumbnailUrlsByShopId(List<Long> shopIds) {
        Map<Long, String> filePathsByShopId = queryFactory
            .select(shopJpaEntity.id, uploadedFileJpaEntity.filePath)
            .from(shopJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopJpaEntity.thumbnailImageFileId))
            .where(shopJpaEntity.id.in(shopIds))
            .fetch()
            .stream()
            .filter(tuple -> tuple.get(uploadedFileJpaEntity.filePath) != null)
            .collect(Collectors.toMap(
                tuple -> Objects.requireNonNull(tuple.get(shopJpaEntity.id)),
                tuple -> Objects.requireNonNull(tuple.get(uploadedFileJpaEntity.filePath))
            ));

        return fileUrlResolver.resolveAll(filePathsByShopId);
    }

    private Map<Long, List<FoodType>> foodTypesByShopId(List<Long> shopIds) {
        return queryFactory
            .select(shopFoodTypeJpaEntity.shopId, shopFoodTypeCategoryJpaEntity.foodType)
            .from(shopFoodTypeJpaEntity)
            .join(shopFoodTypeCategoryJpaEntity).on(shopFoodTypeJpaEntity.shopFoodTypeCategoryId.eq(shopFoodTypeCategoryJpaEntity.id))
            .where(shopFoodTypeJpaEntity.shopId.in(shopIds))
            .fetch()
            .stream()
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(shopFoodTypeJpaEntity.shopId)),
                Collectors.mapping(tuple -> tuple.get(shopFoodTypeCategoryJpaEntity.foodType), Collectors.toList())
            ));
    }

    private Map<Long, Long> reviewCountsByShopId(List<Long> shopIds) {
        return queryFactory
            .select(reviewJpaEntity.shopId, reviewJpaEntity.shopId.count())
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.shopId.in(shopIds),
                reviewJpaEntity.hidden.isFalse(),
                reviewJpaEntity.ownerOnly.isFalse()
            )
            .groupBy(reviewJpaEntity.shopId)
            .fetch()
            .stream()
            .collect(Collectors.toMap(
                tuple -> Objects.requireNonNull(tuple.get(reviewJpaEntity.shopId)),
                tuple -> Objects.requireNonNull(tuple.get(reviewJpaEntity.shopId.count()))
            ));
    }

    private Map<Long, Long> bookmarkCountsByShopId(List<Long> shopIds) {
        return queryFactory
            .select(shopBookmarkJpaEntity.shopId, shopBookmarkJpaEntity.count())
            .from(shopBookmarkJpaEntity)
            .where(shopBookmarkJpaEntity.shopId.in(shopIds))
            .groupBy(shopBookmarkJpaEntity.shopId)
            .fetch()
            .stream()
            .collect(Collectors.toMap(
                tuple -> Objects.requireNonNull(tuple.get(shopBookmarkJpaEntity.shopId)),
                tuple -> Objects.requireNonNull(tuple.get(shopBookmarkJpaEntity.count()))
            ));
    }

    private Set<Long> intersect(Set<Long> first, Set<Long> second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        Set<Long> intersection = new HashSet<>(first);
        intersection.retainAll(second);
        return intersection;
    }

    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? shopJpaEntity.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression permanentlyClosedEq(Boolean permanentlyClosed) {
        return permanentlyClosed != null ? shopJpaEntity.permanentlyClosed.eq(permanentlyClosed) : null;
    }

    private BooleanExpression stationIdEq(Long stationId) {
        return stationId != null ? shopJpaEntity.stationId.eq(stationId) : null;
    }

    private BooleanExpression ceoIdEq(Long ceoId) {
        return ceoId != null ? shopJpaEntity.ceoId.eq(ceoId) : null;
    }

    private BooleanExpression shopIdIn(Set<Long> shopIds) {
        return shopIds != null ? shopJpaEntity.id.in(shopIds) : null;
    }

    private BooleanExpression deliveryAreaCovers(Long adminDongId) {
        if (adminDongId == null) {
            return null;
        }

        BooleanExpression notConfigured = JPAExpressions
            .selectOne()
            .from(shopDeliveryAreaJpaEntity)
            .where(shopDeliveryAreaJpaEntity.shopId.eq(shopJpaEntity.id))
            .notExists();

        BooleanExpression covers = JPAExpressions
            .selectOne()
            .from(shopDeliveryAreaJpaEntity)
            .where(
                shopDeliveryAreaJpaEntity.shopId.eq(shopJpaEntity.id),
                shopDeliveryAreaJpaEntity.adminDongId.eq(adminDongId)
            )
            .exists();

        return notConfigured.or(covers);
    }

    private ShopBookmarkedItemResult withResolvedImageUrlAndTipRange(
        ShopBookmarkedItemResult row,
        Map<Long, ShopDeliveryTipRangeResult> tipRangeMap
    ) {
        return new ShopBookmarkedItemResult(
            row.shopId(),
            row.bookmarkId(),
            row.shopName(),
            row.stationName(),
            row.rating(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.bookmarked(),
            row.minOrderAmount(),
            minDeliveryTip(tipRangeMap, row.shopId()),
            maxDeliveryTip(tipRangeMap, row.shopId())
        );
    }

    private int minDeliveryTip(Map<Long, ShopDeliveryTipRangeResult> tipRangeMap, Long shopId) {
        ShopDeliveryTipRangeResult range = tipRangeMap.get(shopId);
        return range == null ? 0 : range.minDeliveryTip();
    }

    private int maxDeliveryTip(Map<Long, ShopDeliveryTipRangeResult> tipRangeMap, Long shopId) {
        ShopDeliveryTipRangeResult range = tipRangeMap.get(shopId);
        return range == null ? 0 : range.maxDeliveryTip();
    }
}
