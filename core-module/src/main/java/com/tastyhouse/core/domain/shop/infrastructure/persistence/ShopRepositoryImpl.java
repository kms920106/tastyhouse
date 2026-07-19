package com.tastyhouse.core.domain.shop.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.shop.domain.model.Amenity;
import com.tastyhouse.core.domain.shop.domain.model.FoodType;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.repository.ShopRepository;
import com.tastyhouse.core.domain.shop.domain.vo.ShopId;
import com.tastyhouse.core.domain.shop.application.dto.ShopSearchCondition;
import com.tastyhouse.core.domain.shop.application.dto.result.BestShopItemResult;
import com.tastyhouse.core.domain.shop.application.dto.result.LatestShopItemResult;
import com.tastyhouse.core.domain.shop.application.dto.result.QShopListItemResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopBookmarkedItemResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopListItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.domain.shop.domain.model.QShop.shop;
import static com.tastyhouse.core.domain.shop.domain.model.QShopAmenity.shopAmenity;
import static com.tastyhouse.core.domain.shop.domain.model.QShopAmenityCategory.shopAmenityCategory;
import static com.tastyhouse.core.domain.shop.domain.model.QShopBookmark.shopBookmark;
import static com.tastyhouse.core.domain.shop.domain.model.QShopFoodType.shopFoodType;
import static com.tastyhouse.core.domain.shop.domain.model.QShopFoodTypeCategory.shopFoodTypeCategory;
import static com.tastyhouse.core.domain.shop.domain.model.QStation.station;

@Repository
@RequiredArgsConstructor
public class ShopRepositoryImpl implements ShopRepository {

    // review는 infrastructure-module로 이동한 ReviewJpaEntity를 가리킨다. core-module은
    // infrastructure-module을 의존할 수 없어(의존 방향: infrastructure → core) 생성된 Q타입을
    // import할 수 없으므로, PathBuilder로 JPA 엔티티명("ReviewJpaEntity")을 문자열 참조해
    // 필요한 컬럼만 타입 세이프하게 노출한다(ReviewRepositoryImpl의 member 참조와 동일한 우회).
    private static final PathBuilder<Object> review = new PathBuilder<>(Object.class, "ReviewJpaEntity");
    private static final NumberPath<Long> reviewShopIdCol = review.getNumber("shopId", Long.class);
    private static final BooleanPath reviewHiddenCol = review.getBoolean("hidden");

    private final JPAQueryFactory queryFactory;
    private final ShopJpaRepository shopJpaRepository;

    @Override
    public List<Shop> findNearbyShops(BigDecimal latitude, BigDecimal longitude) {
        double distanceInMeters = 200.0;
        double degreeDistance = distanceInMeters / 111000.0;
        BigDecimal latDiff = BigDecimal.valueOf(degreeDistance);
        BigDecimal lonDiff = BigDecimal.valueOf(degreeDistance);

        return queryFactory.select(shop).from(shop).where(shop.latitude.between(latitude.subtract(latDiff), latitude.add(latDiff)).and(shop.longitude.between(longitude.subtract(lonDiff), longitude.add(lonDiff))).and(shop.permanentlyClosed.eq(false))).fetch();
    }

    @Override
    public PageResult<BestShopItemResult> findBestShops(PageQuery pageQuery) {
        Long total = queryFactory.select(shop.count()).from(shop).where(shop.rating.isNotNull().and(shop.permanentlyClosed.eq(false))).fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<Shop> pagedShops = queryFactory.selectFrom(shop).where(shop.rating.isNotNull().and(shop.permanentlyClosed.eq(false))).orderBy(shop.rating.desc()).offset((long) pageQuery.page() * pageQuery.size()).limit(pageQuery.size()).fetch();

        if (pagedShops.isEmpty()) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<Long> shopIds = pagedShops.stream().map(Shop::getId).collect(Collectors.toList());

        var stationMap = queryFactory.select(shop.id, station.stationName).from(shop).join(station).on(station.id.eq(shop.stationId)).where(shop.id.in(shopIds)).fetch().stream().collect(Collectors.toMap(tuple -> Objects.requireNonNull(tuple.get(shop.id)), tuple -> Objects.requireNonNull(tuple.get(station.stationName))));

        var thumbnailFilePathMap = queryFactory
            .select(shop.id, uploadedFile.filePath)
            .from(shop)
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(shop.thumbnailImageFileId))
            .where(shop.id.in(shopIds))
            .fetch()
            .stream()
            .filter(tuple -> tuple.get(uploadedFile.filePath) != null)
            .collect(Collectors.toMap(tuple -> Objects.requireNonNull(tuple.get(shop.id)), tuple -> Objects.requireNonNull(tuple.get(uploadedFile.filePath))));

        var foodTypeMap = queryFactory
            .select(shopFoodType.shopId, shopFoodTypeCategory.foodType)
            .from(shopFoodType)
            .join(shopFoodTypeCategory).on(shopFoodType.shopFoodTypeCategoryId.eq(shopFoodTypeCategory.id))
            .where(shopFoodType.shopId.in(shopIds))
            .fetch()
            .stream()
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(shopFoodType.shopId)),
                Collectors.mapping(tuple -> tuple.get(shopFoodTypeCategory.foodType), Collectors.toList())
            ));

        List<BestShopItemResult> content = pagedShops.stream().map(s -> new BestShopItemResult(s.getId(), s.getName(), stationMap.get(s.getId()), s.getRating(), thumbnailFilePathMap.get(s.getId()), foodTypeMap.getOrDefault(s.getId(), List.of()))).collect(Collectors.toList());

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<LatestShopItemResult> findLatestShops(Long stationId, List<FoodType> foodTypes, List<Amenity> amenities, PageQuery pageQuery) {
        Set<Long> foodTypeShopIds = null;
        if (foodTypes != null && !foodTypes.isEmpty()) {
            foodTypeShopIds = new HashSet<>(queryFactory
                .select(shopFoodType.shopId)
                .from(shopFoodType)
                .join(shopFoodTypeCategory).on(shopFoodType.shopFoodTypeCategoryId.eq(shopFoodTypeCategory.id))
                .where(shopFoodTypeCategory.foodType.in(foodTypes))
                .fetch());

            if (foodTypeShopIds.isEmpty()) {
                return PageResult.empty(pageQuery.page(), pageQuery.size());
            }
        }

        Set<Long> amenityShopIds = null;
        if (amenities != null && !amenities.isEmpty()) {
            amenityShopIds = new HashSet<>(queryFactory
                .select(shopAmenity.shopId)
                .from(shopAmenity)
                .join(shopAmenityCategory).on(shopAmenity.shopAmenityCategoryId.eq(shopAmenityCategory.id))
                .where(shopAmenityCategory.amenity.in(amenities))
                .groupBy(shopAmenity.shopId)
                .having(shopAmenity.shopId.count().goe((long) amenities.size()))
                .fetch());

            if (amenityShopIds.isEmpty()) {
                return PageResult.empty(pageQuery.page(), pageQuery.size());
            }
        }

        Set<Long> filteredShopIds = null;
        if (foodTypeShopIds != null && amenityShopIds != null) {
            filteredShopIds = new HashSet<>(foodTypeShopIds);
            filteredShopIds.retainAll(amenityShopIds);
            if (filteredShopIds.isEmpty()) {
                return PageResult.empty(pageQuery.page(), pageQuery.size());
            }
        } else if (foodTypeShopIds != null) {
            filteredShopIds = foodTypeShopIds;
        } else if (amenityShopIds != null) {
            filteredShopIds = amenityShopIds;
        }

        Long total = queryFactory.select(shop.count()).from(shop)
            .where(shop.permanentlyClosed.eq(false), stationIdEq(stationId), shopIdIn(filteredShopIds))
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<Shop> pagedShops = queryFactory.selectFrom(shop)
            .where(shop.permanentlyClosed.eq(false), stationIdEq(stationId), shopIdIn(filteredShopIds))
            .orderBy(shop.createdAt.desc()).offset((long) pageQuery.page() * pageQuery.size()).limit(pageQuery.size()).fetch();

        if (pagedShops.isEmpty()) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<Long> shopIds = pagedShops.stream().map(Shop::getId).collect(Collectors.toList());

        var stationMap = queryFactory.select(shop.id, station.stationName).from(shop).join(station).on(station.id.eq(shop.stationId)).where(shop.id.in(shopIds)).fetch().stream().collect(Collectors.toMap(tuple -> Objects.requireNonNull(tuple.get(shop.id)), tuple -> Objects.requireNonNull(tuple.get(station.stationName))));

        var thumbnailFilePathMap = queryFactory
            .select(shop.id, uploadedFile.filePath)
            .from(shop)
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(shop.thumbnailImageFileId))
            .where(shop.id.in(shopIds))
            .fetch()
            .stream()
            .filter(tuple -> tuple.get(uploadedFile.filePath) != null)
            .collect(Collectors.toMap(tuple -> Objects.requireNonNull(tuple.get(shop.id)), tuple -> Objects.requireNonNull(tuple.get(uploadedFile.filePath))));

        var reviewCountMap = queryFactory.select(reviewShopIdCol, review.count()).from(review).where(reviewShopIdCol.in(shopIds).and(reviewHiddenCol.eq(false))).groupBy(reviewShopIdCol).fetch().stream().collect(Collectors.toMap(tuple -> Objects.requireNonNull(tuple.get(reviewShopIdCol)), tuple -> Objects.requireNonNull(tuple.get(review.count()))));

        var bookmarkCountMap = queryFactory.select(shopBookmark.shopId, shopBookmark.count()).from(shopBookmark).where(shopBookmark.shopId.in(shopIds)).groupBy(shopBookmark.shopId).fetch().stream().collect(Collectors.toMap(tuple -> Objects.requireNonNull(tuple.get(shopBookmark.shopId)), tuple -> Objects.requireNonNull(tuple.get(shopBookmark.count()))));

        var foodTypeMap = queryFactory
            .select(shopFoodType.shopId, shopFoodTypeCategory.foodType)
            .from(shopFoodType)
            .join(shopFoodTypeCategory).on(shopFoodType.shopFoodTypeCategoryId.eq(shopFoodTypeCategory.id))
            .where(shopFoodType.shopId.in(shopIds))
            .fetch()
            .stream()
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(shopFoodType.shopId)),
                Collectors.mapping(tuple -> tuple.get(shopFoodTypeCategory.foodType), Collectors.toList())
            ));

        List<LatestShopItemResult> content = pagedShops.stream().map(s -> new LatestShopItemResult(
            s.getId(),
            s.getName(),
            stationMap.get(s.getId()),
            s.getRating(),
            thumbnailFilePathMap.get(s.getId()),
            s.getCreatedAt(),
            reviewCountMap.getOrDefault(s.getId(), 0L),
            bookmarkCountMap.getOrDefault(s.getId(), 0L),
            foodTypeMap.getOrDefault(s.getId(), List.of())
        )).collect(Collectors.toList());

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<ShopBookmarkedItemResult> searchByKeywordWithBookmark(String keyword, MemberId memberId, PageQuery pageQuery) {
        Long total = queryFactory.select(shop.count()).from(shop)
                .where(shop.permanentlyClosed.eq(false), shop.name.containsIgnoreCase(keyword))
                .fetchOne();
        if (total == null || total == 0) return PageResult.empty(pageQuery.page(), pageQuery.size());

        List<Shop> pagedShops = queryFactory.selectFrom(shop)
                .where(shop.permanentlyClosed.eq(false), shop.name.containsIgnoreCase(keyword))
                .orderBy(shop.rating.desc().nullsLast())
                .offset((long) pageQuery.page() * pageQuery.size())
                .limit(pageQuery.size())
                .fetch();

        if (pagedShops.isEmpty()) return PageResult.empty(pageQuery.page(), pageQuery.size());

        List<Long> shopIds = pagedShops.stream().map(Shop::getId).collect(Collectors.toList());

        var stationMap = queryFactory.select(shop.id, station.stationName)
                .from(shop).join(station).on(station.id.eq(shop.stationId))
                .where(shop.id.in(shopIds)).fetch().stream()
                .collect(Collectors.toMap(t -> Objects.requireNonNull(t.get(shop.id)), t -> Objects.requireNonNull(t.get(station.stationName))));

        var thumbnailFilePathMap = queryFactory.select(shop.id, uploadedFile.filePath)
                .from(shop).leftJoin(uploadedFile).on(uploadedFile.id.eq(shop.thumbnailImageFileId))
                .where(shop.id.in(shopIds)).fetch().stream()
                .filter(t -> t.get(uploadedFile.filePath) != null)
                .collect(Collectors.toMap(t -> Objects.requireNonNull(t.get(shop.id)), t -> Objects.requireNonNull(t.get(uploadedFile.filePath))));

        Set<Long> bookmarkedShopIds = new HashSet<>();
        if (memberId != null) {
            bookmarkedShopIds = new HashSet<>(
                queryFactory.select(shopBookmark.shopId)
                    .from(shopBookmark)
                    .where(shopBookmark.shopId.in(shopIds)
                        .and(shopBookmark.memberId.eq(memberId)))
                    .fetch()
            );
        }

        final Set<Long> bookmarked = bookmarkedShopIds;
        List<ShopBookmarkedItemResult> content = pagedShops.stream()
                .map(s -> new ShopBookmarkedItemResult(
                        s.getId(),
                        null,
                        s.getName(),
                        stationMap.get(s.getId()),
                        s.getRating(),
                        thumbnailFilePathMap.get(s.getId()),
                        bookmarked.contains(s.getId())
                )).collect(Collectors.toList());

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<ShopBookmarkedItemResult> findMyBookmarkedShops(MemberId memberId, PageQuery pageQuery) {
        Long total = queryFactory
            .select(shopBookmark.count())
            .from(shopBookmark)
            .join(shop).on(shopBookmark.shopId.eq(shop.id).and(shop.permanentlyClosed.eq(false)))
            .where(shopBookmark.memberId.eq(memberId))
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        var results = queryFactory
            .select(
                shop.id,
                shopBookmark.id,
                shop.name,
                station.stationName,
                shop.rating,
                uploadedFile.filePath
            )
            .from(shopBookmark)
            .join(shop).on(shopBookmark.shopId.eq(shop.id).and(shop.permanentlyClosed.eq(false)))
            .join(station).on(shop.stationId.eq(station.id))
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(shop.thumbnailImageFileId))
            .where(shopBookmark.memberId.eq(memberId))
            .orderBy(shopBookmark.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        List<ShopBookmarkedItemResult> content = results.stream()
            .map(tuple -> new ShopBookmarkedItemResult(
                tuple.get(shop.id),
                tuple.get(shopBookmark.id),
                tuple.get(shop.name),
                tuple.get(station.stationName),
                tuple.get(shop.rating),
                tuple.get(uploadedFile.filePath),
                true
            ))
            .collect(Collectors.toList());

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<ShopListItemResult> findShops(ShopSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(shop.count())
            .from(shop)
            .where(
                nameContains(condition.name()),
                stationIdEq(condition.stationId()),
                permanentlyClosedEq(condition.permanentlyClosed())
            )
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ShopListItemResult> content = queryFactory
            .select(new QShopListItemResult(
                shop.id,
                shop.name,
                station.stationName,
                shop.roadAddress,
                shop.rating,
                shop.permanentlyClosed
            ))
            .from(shop)
            .leftJoin(station).on(station.id.eq(shop.stationId))
            .where(
                nameContains(condition.name()),
                stationIdEq(condition.stationId()),
                permanentlyClosedEq(condition.permanentlyClosed())
            )
            .orderBy(shop.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<Shop> findById(ShopId id) {
        return shopJpaRepository.findById(id.value());
    }

    @Override
    public Shop save(Shop shop) {
        return shopJpaRepository.save(shop);
    }

    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? shop.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression permanentlyClosedEq(Boolean permanentlyClosed) {
        return permanentlyClosed != null ? shop.permanentlyClosed.eq(permanentlyClosed) : null;
    }

    private BooleanExpression stationIdEq(Long stationId) {
        return stationId != null ? shop.stationId.eq(stationId) : null;
    }

    private BooleanExpression shopIdIn(Set<Long> shopIds) {
        return shopIds != null ? shop.id.in(shopIds) : null;
    }
}
