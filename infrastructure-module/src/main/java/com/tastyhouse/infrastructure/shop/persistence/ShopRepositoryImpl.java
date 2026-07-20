package com.tastyhouse.infrastructure.shop.persistence;

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

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopAmenityCategoryJpaEntity.shopAmenityCategoryJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopAmenityJpaEntity.shopAmenityJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopBookmarkJpaEntity.shopBookmarkJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopFoodTypeCategoryJpaEntity.shopFoodTypeCategoryJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopFoodTypeJpaEntity.shopFoodTypeJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QStationJpaEntity.stationJpaEntity;

/**
 * {@code review}는 infrastructure-module로 이동한 {@code ReviewJpaEntity}를 가리킨다.
 * 이 클래스 자신도 infrastructure-module 소속이지만, {@code Review}가 이미 POJO로 분리되어
 * core-module에 {@code QReview}가 더 이상 생성되지 않으므로(review 도메인 전환 시점의 선례와 동일),
 * {@link PathBuilder}로 JPA 엔티티명("ReviewJpaEntity")을 문자열 참조해 필요한 컬럼만
 * 타입 세이프하게 노출한다(`ReviewRepositoryImpl`의 member 참조와 동일한 우회).
 */
@Repository
@RequiredArgsConstructor
public class ShopRepositoryImpl implements ShopRepository {

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

        List<ShopJpaEntity> entities = queryFactory.select(shopJpaEntity).from(shopJpaEntity).where(shopJpaEntity.latitude.between(latitude.subtract(latDiff), latitude.add(latDiff)).and(shopJpaEntity.longitude.between(longitude.subtract(lonDiff), longitude.add(lonDiff))).and(shopJpaEntity.permanentlyClosed.eq(false))).fetch();
        return entities.stream().map(ShopMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public PageResult<BestShopItemResult> findBestShops(PageQuery pageQuery) {
        Long total = queryFactory.select(shopJpaEntity.id.count()).from(shopJpaEntity).where(shopJpaEntity.rating.isNotNull().and(shopJpaEntity.permanentlyClosed.eq(false))).fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ShopJpaEntity> pagedShops = queryFactory.selectFrom(shopJpaEntity).where(shopJpaEntity.rating.isNotNull().and(shopJpaEntity.permanentlyClosed.eq(false))).orderBy(shopJpaEntity.rating.desc()).offset((long) pageQuery.page() * pageQuery.size()).limit(pageQuery.size()).fetch();

        if (pagedShops.isEmpty()) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<Long> shopIds = pagedShops.stream().map(ShopJpaEntity::getId).collect(Collectors.toList());

        var stationMap = queryFactory.select(shopJpaEntity.id, stationJpaEntity.stationName).from(shopJpaEntity).join(stationJpaEntity).on(stationJpaEntity.id.eq(shopJpaEntity.stationId)).where(shopJpaEntity.id.in(shopIds)).fetch().stream().collect(Collectors.toMap(tuple -> Objects.requireNonNull(tuple.get(shopJpaEntity.id)), tuple -> Objects.requireNonNull(tuple.get(stationJpaEntity.stationName))));

        var thumbnailFilePathMap = queryFactory
            .select(shopJpaEntity.id, uploadedFileJpaEntity.filePath)
            .from(shopJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopJpaEntity.thumbnailImageFileId))
            .where(shopJpaEntity.id.in(shopIds))
            .fetch()
            .stream()
            .filter(tuple -> tuple.get(uploadedFileJpaEntity.filePath) != null)
            .collect(Collectors.toMap(tuple -> Objects.requireNonNull(tuple.get(shopJpaEntity.id)), tuple -> Objects.requireNonNull(tuple.get(uploadedFileJpaEntity.filePath))));

        var foodTypeMap = queryFactory
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

        List<BestShopItemResult> content = pagedShops.stream().map(s -> new BestShopItemResult(s.getId(), s.getName(), stationMap.get(s.getId()), s.getRating(), thumbnailFilePathMap.get(s.getId()), foodTypeMap.getOrDefault(s.getId(), List.of()))).collect(Collectors.toList());

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<LatestShopItemResult> findLatestShops(Long stationId, List<FoodType> foodTypes, List<Amenity> amenities, PageQuery pageQuery) {
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

        Long total = queryFactory.select(shopJpaEntity.count()).from(shopJpaEntity)
            .where(shopJpaEntity.permanentlyClosed.eq(false), stationIdEq(stationId), shopIdIn(filteredShopIds))
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ShopJpaEntity> pagedShops = queryFactory.selectFrom(shopJpaEntity)
            .where(shopJpaEntity.permanentlyClosed.eq(false), stationIdEq(stationId), shopIdIn(filteredShopIds))
            .orderBy(shopJpaEntity.createdAt.desc()).offset((long) pageQuery.page() * pageQuery.size()).limit(pageQuery.size()).fetch();

        if (pagedShops.isEmpty()) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<Long> shopIds = pagedShops.stream().map(ShopJpaEntity::getId).collect(Collectors.toList());

        var stationMap = queryFactory.select(shopJpaEntity.id, stationJpaEntity.stationName).from(shopJpaEntity).join(stationJpaEntity).on(stationJpaEntity.id.eq(shopJpaEntity.stationId)).where(shopJpaEntity.id.in(shopIds)).fetch().stream().collect(Collectors.toMap(tuple -> Objects.requireNonNull(tuple.get(shopJpaEntity.id)), tuple -> Objects.requireNonNull(tuple.get(stationJpaEntity.stationName))));

        var thumbnailFilePathMap = queryFactory
            .select(shopJpaEntity.id, uploadedFileJpaEntity.filePath)
            .from(shopJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopJpaEntity.thumbnailImageFileId))
            .where(shopJpaEntity.id.in(shopIds))
            .fetch()
            .stream()
            .filter(tuple -> tuple.get(uploadedFileJpaEntity.filePath) != null)
            .collect(Collectors.toMap(tuple -> Objects.requireNonNull(tuple.get(shopJpaEntity.id)), tuple -> Objects.requireNonNull(tuple.get(uploadedFileJpaEntity.filePath))));

        var reviewCountMap = queryFactory.select(reviewShopIdCol, reviewShopIdCol.count()).from(review).where(reviewShopIdCol.in(shopIds).and(reviewHiddenCol.eq(false))).groupBy(reviewShopIdCol).fetch().stream().collect(Collectors.toMap(tuple -> Objects.requireNonNull(tuple.get(reviewShopIdCol)), tuple -> Objects.requireNonNull(tuple.get(reviewShopIdCol.count()))));

        var bookmarkCountMap = queryFactory.select(shopBookmarkJpaEntity.shopId, shopBookmarkJpaEntity.count()).from(shopBookmarkJpaEntity).where(shopBookmarkJpaEntity.shopId.in(shopIds)).groupBy(shopBookmarkJpaEntity.shopId).fetch().stream().collect(Collectors.toMap(tuple -> Objects.requireNonNull(tuple.get(shopBookmarkJpaEntity.shopId)), tuple -> Objects.requireNonNull(tuple.get(shopBookmarkJpaEntity.count()))));

        var foodTypeMap = queryFactory
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
        Long total = queryFactory.select(shopJpaEntity.count()).from(shopJpaEntity)
                .where(shopJpaEntity.permanentlyClosed.eq(false), shopJpaEntity.name.containsIgnoreCase(keyword))
                .fetchOne();
        if (total == null || total == 0) return PageResult.empty(pageQuery.page(), pageQuery.size());

        List<ShopJpaEntity> pagedShops = queryFactory.selectFrom(shopJpaEntity)
                .where(shopJpaEntity.permanentlyClosed.eq(false), shopJpaEntity.name.containsIgnoreCase(keyword))
                .orderBy(shopJpaEntity.rating.desc().nullsLast())
                .offset((long) pageQuery.page() * pageQuery.size())
                .limit(pageQuery.size())
                .fetch();

        if (pagedShops.isEmpty()) return PageResult.empty(pageQuery.page(), pageQuery.size());

        List<Long> shopIds = pagedShops.stream().map(ShopJpaEntity::getId).collect(Collectors.toList());

        var stationMap = queryFactory.select(shopJpaEntity.id, stationJpaEntity.stationName)
                .from(shopJpaEntity).join(stationJpaEntity).on(stationJpaEntity.id.eq(shopJpaEntity.stationId))
                .where(shopJpaEntity.id.in(shopIds)).fetch().stream()
                .collect(Collectors.toMap(t -> Objects.requireNonNull(t.get(shopJpaEntity.id)), t -> Objects.requireNonNull(t.get(stationJpaEntity.stationName))));

        var thumbnailFilePathMap = queryFactory.select(shopJpaEntity.id, uploadedFileJpaEntity.filePath)
                .from(shopJpaEntity).leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopJpaEntity.thumbnailImageFileId))
                .where(shopJpaEntity.id.in(shopIds)).fetch().stream()
                .filter(t -> t.get(uploadedFileJpaEntity.filePath) != null)
                .collect(Collectors.toMap(t -> Objects.requireNonNull(t.get(shopJpaEntity.id)), t -> Objects.requireNonNull(t.get(uploadedFileJpaEntity.filePath))));

        Set<Long> bookmarkedShopIds = new HashSet<>();
        if (memberId != null) {
            bookmarkedShopIds = new HashSet<>(
                queryFactory.select(shopBookmarkJpaEntity.shopId)
                    .from(shopBookmarkJpaEntity)
                    .where(shopBookmarkJpaEntity.shopId.in(shopIds)
                        .and(shopBookmarkJpaEntity.memberId.eq(memberId)))
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
            .select(shopBookmarkJpaEntity.count())
            .from(shopBookmarkJpaEntity)
            .join(shopJpaEntity).on(shopBookmarkJpaEntity.shopId.eq(shopJpaEntity.id).and(shopJpaEntity.permanentlyClosed.eq(false)))
            .where(shopBookmarkJpaEntity.memberId.eq(memberId))
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        var results = queryFactory
            .select(
                shopJpaEntity.id,
                shopBookmarkJpaEntity.id,
                shopJpaEntity.name,
                stationJpaEntity.stationName,
                shopJpaEntity.rating,
                uploadedFileJpaEntity.filePath
            )
            .from(shopBookmarkJpaEntity)
            .join(shopJpaEntity).on(shopBookmarkJpaEntity.shopId.eq(shopJpaEntity.id).and(shopJpaEntity.permanentlyClosed.eq(false)))
            .join(stationJpaEntity).on(shopJpaEntity.stationId.eq(stationJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopJpaEntity.thumbnailImageFileId))
            .where(shopBookmarkJpaEntity.memberId.eq(memberId))
            .orderBy(shopBookmarkJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        List<ShopBookmarkedItemResult> content = results.stream()
            .map(tuple -> new ShopBookmarkedItemResult(
                tuple.get(shopJpaEntity.id),
                tuple.get(shopBookmarkJpaEntity.id),
                tuple.get(shopJpaEntity.name),
                tuple.get(stationJpaEntity.stationName),
                tuple.get(shopJpaEntity.rating),
                tuple.get(uploadedFileJpaEntity.filePath),
                true
            ))
            .collect(Collectors.toList());

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
                permanentlyClosedEq(condition.permanentlyClosed())
            )
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ShopListItemResult> content = queryFactory
            .select(new QShopListItemResult(
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
                permanentlyClosedEq(condition.permanentlyClosed())
            )
            .orderBy(shopJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<Shop> findById(ShopId id) {
        return shopJpaRepository.findById(id.value()).map(ShopMapper::toDomain);
    }

    @Override
    public Shop save(Shop shop) {
        if (shop.getId() == null) {
            ShopJpaEntity saved = shopJpaRepository.save(ShopMapper.toEntity(shop));
            return ShopMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ShopJpaEntity entity = shopJpaRepository.findById(shop.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상점입니다: " + shop.getId()));
        ShopMapper.applyChanges(entity, shop);
        return ShopMapper.toDomain(entity);
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

    private BooleanExpression shopIdIn(Set<Long> shopIds) {
        return shopIds != null ? shopJpaEntity.id.in(shopIds) : null;
    }
}
