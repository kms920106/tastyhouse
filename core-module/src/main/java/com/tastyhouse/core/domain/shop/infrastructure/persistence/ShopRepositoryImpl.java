package com.tastyhouse.core.domain.shop.infrastructure.persistence;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.shop.application.dto.result.BestShopItemDto;
import com.tastyhouse.core.domain.shop.application.dto.result.LatestShopItemDto;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopBookmarkedItemDto;
import com.tastyhouse.core.domain.shop.domain.model.Amenity;
import com.tastyhouse.core.domain.shop.domain.model.FoodType;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.domain.shop.domain.model.QShop.shop;
import static com.tastyhouse.core.domain.shop.domain.model.QShopAmenity.shopAmenity;
import static com.tastyhouse.core.domain.shop.domain.model.QShopAmenityCategory.shopAmenityCategory;
import static com.tastyhouse.core.domain.shop.domain.model.QShopBookmark.shopBookmark;
import static com.tastyhouse.core.domain.shop.domain.model.QShopFoodType.shopFoodType;
import static com.tastyhouse.core.domain.shop.domain.model.QShopFoodTypeCategory.shopFoodTypeCategory;
import static com.tastyhouse.core.domain.shop.domain.model.QStation.station;
import static com.tastyhouse.core.domain.review.domain.model.QReview.review;

@Repository
@RequiredArgsConstructor
public class ShopRepositoryImpl implements ShopRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Shop> findNearbyShops(BigDecimal latitude, BigDecimal longitude) {
        double distanceInMeters = 200.0;
        double degreeDistance = distanceInMeters / 111000.0;
        BigDecimal latDiff = BigDecimal.valueOf(degreeDistance);
        BigDecimal lonDiff = BigDecimal.valueOf(degreeDistance);

        return queryFactory.select(shop).from(shop).where(shop.latitude.between(latitude.subtract(latDiff), latitude.add(latDiff)).and(shop.longitude.between(longitude.subtract(lonDiff), longitude.add(lonDiff))).and(shop.permanentlyClosed.eq(false))).fetch();
    }

    @Override
    public Page<BestShopItemDto> findBestShops(Pageable pageable) {
        Long total = queryFactory.select(shop.count()).from(shop).where(shop.rating.isNotNull().and(shop.permanentlyClosed.eq(false))).fetchOne();

        if (total == null || total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<Shop> pagedShops = queryFactory.selectFrom(shop).where(shop.rating.isNotNull().and(shop.permanentlyClosed.eq(false))).orderBy(shop.rating.desc()).offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

        if (pagedShops.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, total);
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

        List<BestShopItemDto> content = pagedShops.stream().map(s -> new BestShopItemDto(s.getId(), s.getName(), stationMap.get(s.getId()), s.getRating(), thumbnailFilePathMap.get(s.getId()), foodTypeMap.getOrDefault(s.getId(), List.of()))).collect(Collectors.toList());

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<LatestShopItemDto> findLatestShops(Long stationId, List<FoodType> foodTypes, List<Amenity> amenities, Pageable pageable) {
        BooleanBuilder whereClause = new BooleanBuilder();
        whereClause.and(shop.permanentlyClosed.eq(false));

        if (stationId != null) {
            whereClause.and(shop.stationId.eq(stationId));
        }

        Set<Long> foodTypeShopIds = null;
        if (foodTypes != null && !foodTypes.isEmpty()) {
            foodTypeShopIds = new HashSet<>(queryFactory
                .select(shopFoodType.shopId)
                .from(shopFoodType)
                .join(shopFoodTypeCategory).on(shopFoodType.shopFoodTypeCategoryId.eq(shopFoodTypeCategory.id))
                .where(shopFoodTypeCategory.foodType.in(foodTypes))
                .fetch());

            if (foodTypeShopIds.isEmpty()) {
                return new PageImpl<>(List.of(), pageable, 0);
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
                return new PageImpl<>(List.of(), pageable, 0);
            }
        }

        Set<Long> filteredShopIds = null;
        if (foodTypeShopIds != null && amenityShopIds != null) {
            filteredShopIds = new HashSet<>(foodTypeShopIds);
            filteredShopIds.retainAll(amenityShopIds);
            if (filteredShopIds.isEmpty()) {
                return new PageImpl<>(List.of(), pageable, 0);
            }
        } else if (foodTypeShopIds != null) {
            filteredShopIds = foodTypeShopIds;
        } else if (amenityShopIds != null) {
            filteredShopIds = amenityShopIds;
        }

        if (filteredShopIds != null) {
            whereClause.and(shop.id.in(filteredShopIds));
        }

        Long total = queryFactory.select(shop.count()).from(shop).where(whereClause).fetchOne();

        if (total == null || total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<Shop> pagedShops = queryFactory.selectFrom(shop).where(whereClause).orderBy(shop.createdAt.desc()).offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

        if (pagedShops.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, total);
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

        var reviewCountMap = queryFactory.select(review.shopId, review.count()).from(review).where(review.shopId.in(shopIds).and(review.hidden.eq(false))).groupBy(review.shopId).fetch().stream().collect(Collectors.toMap(tuple -> Objects.requireNonNull(tuple.get(review.shopId)), tuple -> Objects.requireNonNull(tuple.get(review.count()))));

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

        List<LatestShopItemDto> content = pagedShops.stream().map(s -> new LatestShopItemDto(
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

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<ShopBookmarkedItemDto> searchByKeywordWithBookmark(String keyword, Long memberId, Pageable pageable) {
        BooleanBuilder where = new BooleanBuilder()
                .and(shop.permanentlyClosed.eq(false))
                .and(shop.name.containsIgnoreCase(keyword));

        Long total = queryFactory.select(shop.count()).from(shop).where(where).fetchOne();
        if (total == null || total == 0) return new PageImpl<>(List.of(), pageable, 0);

        List<Shop> pagedShops = queryFactory.selectFrom(shop)
                .where(where)
                .orderBy(shop.rating.desc().nullsLast())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (pagedShops.isEmpty()) return new PageImpl<>(List.of(), pageable, total);

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
        List<ShopBookmarkedItemDto> content = pagedShops.stream()
                .map(s -> new ShopBookmarkedItemDto(
                        s.getId(),
                        null,
                        s.getName(),
                        stationMap.get(s.getId()),
                        s.getRating(),
                        thumbnailFilePathMap.get(s.getId()),
                        bookmarked.contains(s.getId())
                )).collect(Collectors.toList());

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<ShopBookmarkedItemDto> findMyBookmarkedShops(Long memberId, Pageable pageable) {
        Long total = queryFactory
            .select(shopBookmark.count())
            .from(shopBookmark)
            .join(shop).on(shopBookmark.shopId.eq(shop.id).and(shop.permanentlyClosed.eq(false)))
            .where(shopBookmark.memberId.eq(memberId))
            .fetchOne();

        if (total == null || total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
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
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        List<ShopBookmarkedItemDto> content = results.stream()
            .map(tuple -> new ShopBookmarkedItemDto(
                tuple.get(shop.id),
                tuple.get(shopBookmark.id),
                tuple.get(shop.name),
                tuple.get(station.stationName),
                tuple.get(shop.rating),
                tuple.get(uploadedFile.filePath),
                true
            ))
            .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, total);
    }
}
