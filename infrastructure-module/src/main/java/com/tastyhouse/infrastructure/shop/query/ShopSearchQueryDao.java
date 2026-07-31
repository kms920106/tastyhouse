package com.tastyhouse.infrastructure.shop.query;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.shop.domain.model.Amenity;
import com.tastyhouse.domain.shop.domain.model.FoodType;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.shop.persistence.ShopJpaEntity;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewJpaEntity.reviewJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopAmenityCategoryJpaEntity.shopAmenityCategoryJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopAmenityJpaEntity.shopAmenityJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopBookmarkJpaEntity.shopBookmarkJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopFoodTypeCategoryJpaEntity.shopFoodTypeCategoryJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopFoodTypeJpaEntity.shopFoodTypeJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QStationJpaEntity.stationJpaEntity;

/**
 * 가게 목록·검색 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로 write
 * 포트({@code ShopRepository})와 역할이 겹치지 않는다. 소비 모듈(web/admin/ceo-api)의
 * {@code Shop*QueryService}가 이 DAO를 주입해 사용하며, 그 덕분에 api 모듈은 QueryDSL을 알지 않는다.
 *
 * <p>shop은 대형 도메인이므로 공통 지침의 용도별 분리 허용에 따라 DAO를 둘로 나눈다 — 이 클래스는
 * <b>목록·검색·베스트·즐겨찾기 등 대형 조인</b>을 담당하고, 가게별 설정·관리 화면 조회는
 * {@link ShopQueryDao}가 담당한다.
 *
 * <p>목록 조회는 페이지 대상 가게를 먼저 뽑고 역·썸네일·음식유형·리뷰수·즐겨찾기수를 shopId 일괄
 * 조회(in절)로 채우는 방식을 유지한다 — 컬렉션 필드(음식유형 다건)가 있어 단일 조인 투영으로는
 * 카티전 곱이 생기기 때문이다.
 */
@Repository
@RequiredArgsConstructor
public class ShopSearchQueryDao {

    /**
     * 지도 마커 조회 반경(m). 위·경도 1도 ≈ 111km 근사로 사각 범위를 계산한다.
     */
    private static final double MAP_MARKER_RADIUS_METERS = 200.0;
    private static final double METERS_PER_DEGREE = 111000.0;

    private final JPAQueryFactory queryFactory;

    /**
     * 현재 위치 주변 가게 마커 목록. 폐업·노출정지 가게는 제외한다.
     */
    public List<ShopMapMarkerResult> findNearbyShops(BigDecimal latitude, BigDecimal longitude) {
        BigDecimal degreeDiff = BigDecimal.valueOf(MAP_MARKER_RADIUS_METERS / METERS_PER_DEGREE);

        return queryFactory
            .select(new QShopMapMarkerResult(
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

    /**
     * 베스트 가게 목록 — 평점 높은 순. 평점 없는 가게와 폐업·노출정지 가게는 제외한다.
     */
    public PageResult<BestShopItemResult> findBestShops(PageQuery pageQuery) {
        BooleanExpression[] conditions = {
            shopJpaEntity.rating.isNotNull(),
            shopJpaEntity.permanentlyClosed.eq(false),
            shopJpaEntity.hidden.eq(false)
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
        var thumbnailMap = thumbnailFilePathsByShopId(shopIds);
        var foodTypeMap = foodTypesByShopId(shopIds);

        List<BestShopItemResult> content = pagedShops.stream()
            .map(shop -> new BestShopItemResult(
                shop.getId(),
                shop.getName(),
                stationMap.get(shop.getId()),
                shop.getRating(),
                thumbnailMap.get(shop.getId()),
                foodTypeMap.getOrDefault(shop.getId(), List.of())
            ))
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    /**
     * 최신 가게 목록 — 등록 최신 순. 역·음식유형·편의시설로 필터한다(편의시설은 지정한 항목을 모두 갖춘
     * 가게만). 폐업·노출정지 가게는 제외한다.
     */
    public PageResult<LatestShopItemResult> findLatestShops(
        Long stationId,
        List<FoodType> foodTypes,
        List<Amenity> amenities,
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
                shopIdIn(filteredShopIds)
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
                shopIdIn(filteredShopIds)
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
        var thumbnailMap = thumbnailFilePathsByShopId(shopIds);
        var foodTypeMap = foodTypesByShopId(shopIds);
        var reviewCountMap = reviewCountsByShopId(shopIds);
        var bookmarkCountMap = bookmarkCountsByShopId(shopIds);

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
                foodTypeMap.getOrDefault(shop.getId(), List.of())
            ))
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    /**
     * 상호명 키워드 검색 결과 — 평점 높은 순. 로그인 회원이면 즐겨찾기 여부를 함께 채운다.
     */
    public PageResult<ShopBookmarkedItemResult> searchByKeywordWithBookmark(String keyword, MemberId memberId, PageQuery pageQuery) {
        BooleanExpression[] conditions = {
            shopJpaEntity.permanentlyClosed.eq(false),
            shopJpaEntity.hidden.eq(false),
            shopJpaEntity.name.containsIgnoreCase(keyword)
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
        var thumbnailMap = thumbnailFilePathsByShopId(shopIds);

        Set<Long> bookmarkedShopIds = memberId == null
            ? Set.of()
            : new HashSet<>(queryFactory
                .select(shopBookmarkJpaEntity.shopId)
                .from(shopBookmarkJpaEntity)
                .where(shopBookmarkJpaEntity.shopId.in(shopIds), shopBookmarkJpaEntity.memberId.eq(memberId))
                .fetch());

        List<ShopBookmarkedItemResult> content = pagedShops.stream()
            .map(shop -> new ShopBookmarkedItemResult(
                shop.getId(),
                null,
                shop.getName(),
                stationMap.get(shop.getId()),
                shop.getRating(),
                thumbnailMap.get(shop.getId()),
                bookmarkedShopIds.contains(shop.getId())
            ))
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    /**
     * 내 즐겨찾기 가게 목록 — 즐겨찾기 등록 최신 순. 폐업·노출정지 가게는 제외한다.
     */
    public PageResult<ShopBookmarkedItemResult> findMyBookmarkedShops(MemberId memberId, PageQuery pageQuery) {
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

        List<ShopBookmarkedItemResult> content = queryFactory
            .select(new QShopBookmarkedItemResult(
                shopJpaEntity.id,
                shopBookmarkJpaEntity.id,
                shopJpaEntity.name,
                stationJpaEntity.stationName,
                shopJpaEntity.rating,
                uploadedFileJpaEntity.filePath,
                Expressions.asBoolean(true)
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

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    /**
     * 가게 목록 페이징(관리·점주 화면) — 상호명·역·폐업여부·소유 점주로 필터하며, 최근 등록 순.
     */
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
                permanentlyClosedEq(condition.permanentlyClosed()),
                ceoIdEq(condition.ceoId())
            )
            .orderBy(shopJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    // ------------------------------------------------------ shopId 일괄 보강 조회

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

    private Map<Long, String> thumbnailFilePathsByShopId(List<Long> shopIds) {
        return queryFactory
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
            .where(reviewJpaEntity.shopId.in(shopIds), reviewJpaEntity.hidden.eq(false))
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

    /**
     * 두 필터 집합의 교집합. 한쪽이 없으면 다른 쪽을, 둘 다 없으면 null(필터 없음)을 돌려준다.
     */
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
}
