package com.tastyhouse.core.repository.place;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.place.Amenity;
import com.tastyhouse.core.entity.place.FoodType;
import com.tastyhouse.core.entity.place.Place;
import com.tastyhouse.core.entity.place.dto.BestPlaceItemDto;
import com.tastyhouse.core.entity.place.dto.LatestPlaceItemDto;
import com.tastyhouse.core.entity.place.dto.PlaceBookmarkedItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.entity.place.QPlace.place;
import static com.tastyhouse.core.entity.place.QPlaceAmenity.placeAmenity;
import static com.tastyhouse.core.entity.place.QPlaceAmenityCategory.placeAmenityCategory;
import static com.tastyhouse.core.entity.place.QPlaceBookmark.placeBookmark;
import static com.tastyhouse.core.entity.place.QPlaceFoodType.placeFoodType;
import static com.tastyhouse.core.entity.place.QPlaceFoodTypeCategory.placeFoodTypeCategory;
import static com.tastyhouse.core.entity.place.QPlaceStation.placeStation;
import static com.tastyhouse.core.entity.review.QReview.review;

@Repository
@RequiredArgsConstructor
public class PlaceRepositoryImpl implements PlaceRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Place> findNearbyPlaces(BigDecimal latitude, BigDecimal longitude) {
        // 200m를 위도/경도 차이로 변환 (대략적인 계산)
        // 1도 = 약 111km, 200m = 0.0018도 정도
        double distanceInMeters = 200.0;
        double degreeDistance = distanceInMeters / 111000.0;
        BigDecimal latDiff = BigDecimal.valueOf(degreeDistance);
        BigDecimal lonDiff = BigDecimal.valueOf(degreeDistance);

        return queryFactory.select(place).from(place).where(place.latitude.between(latitude.subtract(latDiff), latitude.add(latDiff)).and(place.longitude.between(longitude.subtract(lonDiff), longitude.add(lonDiff))).and(place.permanentlyClosed.eq(false))).fetch();
    }

    @Override
    public Page<BestPlaceItemDto> findBestPlaces(Pageable pageable) {
        // 1. 전체 개수 조회
        Long total = queryFactory.select(place.count()).from(place).where(place.rating.isNotNull().and(place.permanentlyClosed.eq(false))).fetchOne();

        if (total == null || total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 2. 평점 기준 페이징 처리된 Place 조회
        List<Place> pagedPlaces = queryFactory.selectFrom(place).where(place.rating.isNotNull().and(place.permanentlyClosed.eq(false))).orderBy(place.rating.desc()).offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

        if (pagedPlaces.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, total);
        }

        List<Long> placeIds = pagedPlaces.stream().map(Place::getId).collect(Collectors.toList());

        // 3. Place별 Station 정보 조회
        var stationMap = queryFactory.select(place.id, placeStation.stationName).from(place).join(placeStation).on(placeStation.id.eq(place.stationId)).where(place.id.in(placeIds)).fetch().stream().collect(Collectors.toMap(tuple -> tuple.get(place.id), tuple -> tuple.get(placeStation.stationName)));

        // 4. Place별 썸네일 이미지 filePath 조회
        var thumbnailFilePathMap = queryFactory
            .select(place.id, uploadedFile.filePath)
            .from(place)
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(place.thumbnailImageFileId))
            .where(place.id.in(placeIds))
            .fetch()
            .stream()
            .filter(tuple -> tuple.get(uploadedFile.filePath) != null)
            .collect(Collectors.toMap(tuple -> tuple.get(place.id), tuple -> tuple.get(uploadedFile.filePath)));

        // 5. Place별 음식종류 목록 조회
        var foodTypeMap = queryFactory
            .select(placeFoodType.placeId, placeFoodTypeCategory.foodType)
            .from(placeFoodType)
            .join(placeFoodTypeCategory).on(placeFoodType.placeFoodTypeCategoryId.eq(placeFoodTypeCategory.id))
            .where(placeFoodType.placeId.in(placeIds))
            .fetch()
            .stream()
            .collect(Collectors.groupingBy(
                tuple -> tuple.get(placeFoodType.placeId),
                Collectors.mapping(tuple -> tuple.get(placeFoodTypeCategory.foodType), Collectors.toList())
            ));

        // 6. 결과 조합
        List<BestPlaceItemDto> content = pagedPlaces.stream().map(p -> new BestPlaceItemDto(p.getId(), p.getName(), stationMap.get(p.getId()), p.getRating(), thumbnailFilePathMap.get(p.getId()), foodTypeMap.getOrDefault(p.getId(), List.of()))).collect(Collectors.toList());

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<LatestPlaceItemDto> findLatestPlaces(Long stationId, List<FoodType> foodTypes, List<Amenity> amenities, Pageable pageable) {
        // 필터 조건 생성
        BooleanBuilder whereClause = new BooleanBuilder();
        whereClause.and(place.permanentlyClosed.eq(false));

        // 전철역 필터
        if (stationId != null) {
            whereClause.and(place.stationId.eq(stationId));
        }

        // 음식종류 필터
        Set<Long> foodTypePlaceIds = null;
        if (foodTypes != null && !foodTypes.isEmpty()) {
            foodTypePlaceIds = new HashSet<>(queryFactory
                .select(placeFoodType.placeId)
                .from(placeFoodType)
                .join(placeFoodTypeCategory).on(placeFoodType.placeFoodTypeCategoryId.eq(placeFoodTypeCategory.id))
                .where(placeFoodTypeCategory.foodType.in(foodTypes))
                .fetch());

            if (foodTypePlaceIds.isEmpty()) {
                return new PageImpl<>(List.of(), pageable, 0);
            }
        }

        // 편의시설 필터
        Set<Long> amenityPlaceIds = null;
        if (amenities != null && !amenities.isEmpty()) {
            amenityPlaceIds = new HashSet<>(queryFactory
                .select(placeAmenity.placeId)
                .from(placeAmenity)
                .join(placeAmenityCategory).on(placeAmenity.placeAmenityCategoryId.eq(placeAmenityCategory.id))
                .where(placeAmenityCategory.amenity.in(amenities))
                .groupBy(placeAmenity.placeId)
                .having(placeAmenity.placeId.count().goe((long) amenities.size()))
                .fetch());

            if (amenityPlaceIds.isEmpty()) {
                return new PageImpl<>(List.of(), pageable, 0);
            }
        }

        // 필터 결과 교집합 처리
        Set<Long> filteredPlaceIds = null;
        if (foodTypePlaceIds != null && amenityPlaceIds != null) {
            filteredPlaceIds = new HashSet<>(foodTypePlaceIds);
            filteredPlaceIds.retainAll(amenityPlaceIds);
            if (filteredPlaceIds.isEmpty()) {
                return new PageImpl<>(List.of(), pageable, 0);
            }
        } else if (foodTypePlaceIds != null) {
            filteredPlaceIds = foodTypePlaceIds;
        } else if (amenityPlaceIds != null) {
            filteredPlaceIds = amenityPlaceIds;
        }

        if (filteredPlaceIds != null) {
            whereClause.and(place.id.in(filteredPlaceIds));
        }

        // 1. 전체 개수 조회
        Long total = queryFactory
            .select(place.count())
            .from(place)
            .where(whereClause)
            .fetchOne();

        if (total == null || total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 2. 최신순 페이징 처리된 Place 조회
        List<Place> pagedPlaces = queryFactory
            .selectFrom(place)
            .where(whereClause)
            .orderBy(place.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        if (pagedPlaces.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, total);
        }

        List<Long> placeIds = pagedPlaces.stream().map(Place::getId).collect(Collectors.toList());

        // 3. Place별 Station 정보 조회
        var stationMap = queryFactory.select(place.id, placeStation.stationName).from(place).join(placeStation).on(placeStation.id.eq(place.stationId)).where(place.id.in(placeIds)).fetch().stream().collect(Collectors.toMap(tuple -> tuple.get(place.id), tuple -> tuple.get(placeStation.stationName)));

        // 4. Place별 썸네일 이미지 filePath 조회
        var thumbnailFilePathMap = queryFactory
            .select(place.id, uploadedFile.filePath)
            .from(place)
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(place.thumbnailImageFileId))
            .where(place.id.in(placeIds))
            .fetch()
            .stream()
            .filter(tuple -> tuple.get(uploadedFile.filePath) != null)
            .collect(Collectors.toMap(tuple -> tuple.get(place.id), tuple -> tuple.get(uploadedFile.filePath)));

        // 5. Place별 리뷰 개수 조회
        var reviewCountMap = queryFactory.select(review.placeId, review.count()).from(review).where(review.placeId.in(placeIds).and(review.isHidden.eq(false))).groupBy(review.placeId).fetch().stream().collect(Collectors.toMap(tuple -> tuple.get(review.placeId), tuple -> tuple.get(review.count())));

        // 5. Place별 찜 개수 조회
        var bookmarkCountMap = queryFactory.select(placeBookmark.placeId, placeBookmark.count()).from(placeBookmark).where(placeBookmark.placeId.in(placeIds)).groupBy(placeBookmark.placeId).fetch().stream().collect(Collectors.toMap(tuple -> tuple.get(placeBookmark.placeId), tuple -> tuple.get(placeBookmark.count())));

        // 6. Place별 음식종류 목록 조회
        var foodTypeMap = queryFactory
            .select(placeFoodType.placeId, placeFoodTypeCategory.foodType)
            .from(placeFoodType)
            .join(placeFoodTypeCategory).on(placeFoodType.placeFoodTypeCategoryId.eq(placeFoodTypeCategory.id))
            .where(placeFoodType.placeId.in(placeIds))
            .fetch()
            .stream()
            .collect(Collectors.groupingBy(
                tuple -> tuple.get(placeFoodType.placeId),
                Collectors.mapping(tuple -> tuple.get(placeFoodTypeCategory.foodType), Collectors.toList())
            ));

        // 8. 결과 조합
        List<LatestPlaceItemDto> content = pagedPlaces.stream().map(p -> new LatestPlaceItemDto(
            p.getId(),
            p.getName(),
            stationMap.get(p.getId()),
            p.getRating(),
            thumbnailFilePathMap.get(p.getId()),
            p.getCreatedAt(),
            reviewCountMap.getOrDefault(p.getId(), 0L),
            bookmarkCountMap.getOrDefault(p.getId(), 0L),
            foodTypeMap.getOrDefault(p.getId(), List.of())
        )).collect(Collectors.toList());

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<PlaceBookmarkedItemDto> searchByKeywordWithBookmark(String keyword, Long memberId, Pageable pageable) {
        BooleanBuilder where = new BooleanBuilder()
                .and(place.permanentlyClosed.eq(false))
                .and(place.name.containsIgnoreCase(keyword));

        Long total = queryFactory.select(place.count()).from(place).where(where).fetchOne();
        if (total == null || total == 0) return new PageImpl<>(List.of(), pageable, 0);

        List<Place> pagedPlaces = queryFactory.selectFrom(place)
                .where(where)
                .orderBy(place.rating.desc().nullsLast())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (pagedPlaces.isEmpty()) return new PageImpl<>(List.of(), pageable, total);

        List<Long> placeIds = pagedPlaces.stream().map(Place::getId).collect(Collectors.toList());

        var stationMap = queryFactory.select(place.id, placeStation.stationName)
                .from(place).join(placeStation).on(placeStation.id.eq(place.stationId))
                .where(place.id.in(placeIds)).fetch().stream()
                .collect(Collectors.toMap(t -> t.get(place.id), t -> t.get(placeStation.stationName)));

        var thumbnailFilePathMap = queryFactory.select(place.id, uploadedFile.filePath)
                .from(place).leftJoin(uploadedFile).on(uploadedFile.id.eq(place.thumbnailImageFileId))
                .where(place.id.in(placeIds)).fetch().stream()
                .filter(t -> t.get(uploadedFile.filePath) != null)
                .collect(Collectors.toMap(t -> t.get(place.id), t -> t.get(uploadedFile.filePath)));

        Set<Long> bookmarkedPlaceIds = new HashSet<>();
        if (memberId != null) {
            bookmarkedPlaceIds = new HashSet<>(
                queryFactory.select(placeBookmark.placeId)
                    .from(placeBookmark)
                    .where(placeBookmark.placeId.in(placeIds)
                        .and(placeBookmark.memberId.eq(memberId)))
                    .fetch()
            );
        }

        final Set<Long> bookmarked = bookmarkedPlaceIds;
        List<PlaceBookmarkedItemDto> content = pagedPlaces.stream()
                .map(p -> new PlaceBookmarkedItemDto(
                        p.getId(),
                        null,
                        p.getName(),
                        stationMap.get(p.getId()),
                        p.getRating(),
                        thumbnailFilePathMap.get(p.getId()),
                        bookmarked.contains(p.getId())
                )).collect(Collectors.toList());

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<PlaceBookmarkedItemDto> findMyBookmarkedPlaces(Long memberId, Pageable pageable) {
        // 1. 전체 개수 조회
        Long total = queryFactory
            .select(placeBookmark.count())
            .from(placeBookmark)
            .join(place).on(placeBookmark.placeId.eq(place.id).and(place.permanentlyClosed.eq(false)))
            .where(placeBookmark.memberId.eq(memberId))
            .fetchOne();

        if (total == null || total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 2. 북마크된 Place 정보 조회 (최신순)
        var results = queryFactory
            .select(
                place.id,
                placeBookmark.id,
                place.name,
                placeStation.stationName,
                place.rating,
                uploadedFile.filePath
            )
            .from(placeBookmark)
            .join(place).on(placeBookmark.placeId.eq(place.id).and(place.permanentlyClosed.eq(false)))
            .join(placeStation).on(place.stationId.eq(placeStation.id))
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(place.thumbnailImageFileId))
            .where(placeBookmark.memberId.eq(memberId))
            .orderBy(placeBookmark.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 3. DTO 변환
        List<PlaceBookmarkedItemDto> content = results.stream()
            .map(tuple -> new PlaceBookmarkedItemDto(
                tuple.get(place.id),
                tuple.get(placeBookmark.id),
                tuple.get(place.name),
                tuple.get(placeStation.stationName),
                tuple.get(place.rating),
                tuple.get(uploadedFile.filePath),
                true
            ))
            .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, total);
    }
}
