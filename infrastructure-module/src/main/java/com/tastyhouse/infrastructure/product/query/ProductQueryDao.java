package com.tastyhouse.infrastructure.product.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductBbqJpaEntity.productBbqJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductCategoryJpaEntity.productCategoryJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductCommonOptionGroupJpaEntity.productCommonOptionGroupJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductCommonOptionJpaEntity.productCommonOptionJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductImageJpaEntity.productImageJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductJpaEntity.productJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductOptionGroupJpaEntity.productOptionGroupJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductOptionJpaEntity.productOptionJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;

/**
 * product 도메인 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로 write
 * 포트({@code ProductRepository} 등 9개)와 역할이 겹치지 않는다. 소비 모듈(web/admin-api·batch-module)의
 * {@code ProductQueryService}가 이 DAO를 주입해 사용하며, 소비 모듈은 QueryDSL을 알지 못한다.
 *
 * <p>소비자별 메서드 분리:
 * <ul>
 *   <li>web — {@link #findTodayDiscountProducts}, {@link #findProductOptions}, {@link #findProductsBatch},
 *       {@link #findProductImageUrls}, {@link #findShopProducts}, {@link #searchByKeyword}</li>
 *   <li>admin — {@link #findProducts}(관리 목록), {@link #findProductDetailById}, {@link #findProductCategories}</li>
 *   <li>batch — {@link #findFirstBbqSyncTarget}</li>
 * </ul>
 * 상품 대표 이미지 경로를 위해 file 도메인, 가게명을 위해 shop 도메인의 Q타입을 조인한다(같은 모듈 내 참조).
 *
 * <p>{@code PRODUCT.shop_id}/{@code product_category_id}·{@code PRODUCT_CATEGORY.shop_id}·
 * {@code PRODUCT_OPTION_GROUP.product_id}·{@code PRODUCT_COMMON_OPTION_GROUP.product_id}·
 * {@code PRODUCT_OPTION.option_group_id}·{@code PRODUCT_COMMON_OPTION.option_group_id}·
 * {@code PRODUCT_IMAGE.product_id}/{@code image_file_id}·{@code PRODUCT_BBQ.product_id}/{@code bbq_menu_id}는
 * 전부 {@code @Convert}로 VO에 매핑된 FK 필드라 QueryDSL이 VO 타입 path를 생성하므로,
 * {@link Expressions#numberPath}로 raw {@code Long} 컬럼 비교·투영을 우회한다
 * (ID VO 경계 규칙 — query DAO 계층은 항상 raw {@code Long}을 쓴다).
 *
 * <p>조인으로 얻은 저장 경로는 {@link FileUrlResolver}로 표시용 URL까지 변환해 Result에 담는다 —
 * {@code @QueryProjection}은 생성자 직접 투영이라 변환을 투영식에 끼울 수 없어, fetch 직후 재조립한다.
 */
@Repository
@RequiredArgsConstructor
public class ProductQueryDao {

    /**
     * 상품의 대표 이미지(노출 중 최소 sort)를 고르기 위한 서브쿼리 별칭.
     */
    private static final com.tastyhouse.infrastructure.product.persistence.QProductImageJpaEntity subProductImage =
        new com.tastyhouse.infrastructure.product.persistence.QProductImageJpaEntity("subProductImage");

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    // ── web ────────────────────────────────────────────────────────────────

    /**
     * 오늘의 할인 상품 목록 — 할인가가 설정된 노출 상품을 할인율 내림차순으로 페이징한다.
     */
    public PageResult<TodayDiscountProductResult> findTodayDiscountProducts(PageQuery pageQuery) {
        JPAQuery<TodayDiscountProductResult> query = queryFactory
            .select(new QTodayDiscountProductResult(
                productJpaEntity.id,
                shopJpaEntity.name,
                productJpaEntity.name,
                uploadedFileJpaEntity.filePath,
                productJpaEntity.originalPrice,
                productJpaEntity.discountInfo.discountPrice,
                productJpaEntity.discountInfo.discountRate
            ))
            .from(productJpaEntity)
            .innerJoin(shopJpaEntity).on(productShopId().eq(shopJpaEntity.id))
            .leftJoin(productImageJpaEntity).on(representativeImageOf(productJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(imageFileId().eq(uploadedFileJpaEntity.id))
            .where(productJpaEntity.discountInfo.discountPrice.isNotNull()
                .and(productJpaEntity.visible.eq(true)))
            .orderBy(productJpaEntity.discountInfo.discountRate.desc());

        long total = countTodayDiscountProducts();

        List<TodayDiscountProductResult> products = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();

        return PageResult.of(products, total, pageQuery.page(), pageQuery.size());
    }

    /**
     * 오늘의 할인 상품 총 건수 — 목록 쿼리와 같은 {@code innerJoin}(shop)·같은 where를 재현한다.
     *
     * <p>대표 이미지·파일 {@code leftJoin}은 "노출 중 최소 sort 1장"으로 좁혀져 상품당 최대 1행이라
     * 행이 늘지 않으므로 count 쿼리에서 생략한다. 가게 조인은 {@code innerJoin}이라 짝이 없는 상품을
     * 제외하므로 총 건수에 영향을 주어 그대로 재현한다.
     */
    private long countTodayDiscountProducts() {
        Long total = queryFactory
            .select(productJpaEntity.count())
            .from(productJpaEntity)
            .innerJoin(shopJpaEntity).on(productShopId().eq(shopJpaEntity.id))
            .where(productJpaEntity.discountInfo.discountPrice.isNotNull()
                .and(productJpaEntity.visible.eq(true)))
            .fetchOne();

        return total == null ? 0L : total;
    }

    /**
     * 통합검색 상품 결과 — 판매 중(노출·비품절) 상품 중 영업 중인 가게의 것만 검색한다.
     */
    public PageResult<SearchProductItemResult> searchByKeyword(String keyword, PageQuery pageQuery) {
        BooleanExpression searchable = productJpaEntity.name.containsIgnoreCase(keyword)
            .and(productJpaEntity.visible.eq(true))
            .and(productJpaEntity.soldOut.eq(false))
            .and(shopJpaEntity.permanentlyClosed.eq(false))
            .and(shopJpaEntity.hidden.eq(false));

        Long total = queryFactory
            .select(productJpaEntity.count())
            .from(productJpaEntity)
            .innerJoin(shopJpaEntity).on(productShopId().eq(shopJpaEntity.id))
            .where(searchable)
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<SearchProductItemResult> content = queryFactory
            .select(new QSearchProductItemResult(
                productJpaEntity.id,
                shopJpaEntity.name,
                productJpaEntity.name,
                uploadedFileJpaEntity.filePath,
                productJpaEntity.originalPrice,
                productJpaEntity.discountInfo.discountPrice,
                productJpaEntity.discountInfo.discountRate,
                productJpaEntity.rating,
                productJpaEntity.reviewCount,
                productJpaEntity.representative,
                productJpaEntity.spiciness
            ))
            .from(productJpaEntity)
            .innerJoin(shopJpaEntity).on(productShopId().eq(shopJpaEntity.id))
            .leftJoin(productImageJpaEntity).on(representativeImageOf(productJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(imageFileId().eq(uploadedFileJpaEntity.id))
            .where(searchable)
            .orderBy(productJpaEntity.representative.desc().nullsLast(), productJpaEntity.rating.desc().nullsLast())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    /**
     * 상품 옵션 목록 — 개별 옵션 그룹과 공통 옵션 그룹을 단일 목록으로 병합해 반환한다(개별 먼저).
     * 그룹·옵션을 각각 배치(in) 조회해 N+1을 방지한다.
     */
    public ProductOptionsResult findProductOptions(Long productId) {
        List<OptionGroupResult> result = new ArrayList<>();
        result.addAll(findNormalOptionGroups(productId));
        result.addAll(findCommonOptionGroups(productId));
        return new ProductOptionsResult(result);
    }

    private List<OptionGroupResult> findNormalOptionGroups(Long productId) {
        List<Tuple> groups = queryFactory
            .select(
                productOptionGroupJpaEntity.id,
                productOptionGroupJpaEntity.name,
                productOptionGroupJpaEntity.description,
                productOptionGroupJpaEntity.required,
                productOptionGroupJpaEntity.multipleSelect,
                productOptionGroupJpaEntity.minSelect,
                productOptionGroupJpaEntity.maxSelect
            )
            .from(productOptionGroupJpaEntity)
            .where(optionGroupProductId().eq(productId), productOptionGroupJpaEntity.visible.eq(true))
            .orderBy(productOptionGroupJpaEntity.sort.asc())
            .fetch();

        if (groups.isEmpty()) {
            return List.of();
        }

        List<Long> groupIds = groups.stream().map(tuple -> tuple.get(productOptionGroupJpaEntity.id)).toList();
        // select와 Tuple.get이 같은 표현식을 참조하도록 numberPath를 지역 변수로 추출한다.
        NumberPath<Long> optionGroupId = optionGroupId();
        Map<Long, List<OptionResult>> optionsByGroupId = queryFactory
            .select(
                optionGroupId,
                productOptionJpaEntity.id,
                productOptionJpaEntity.name,
                productOptionJpaEntity.additionalPrice,
                productOptionJpaEntity.soldOut
            )
            .from(productOptionJpaEntity)
            .where(optionGroupId.in(groupIds), productOptionJpaEntity.visible.eq(true))
            .orderBy(productOptionJpaEntity.sort.asc())
            .fetch()
            .stream()
            .filter(tuple -> tuple.get(optionGroupId) != null)
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(optionGroupId)),
                LinkedHashMap::new,
                Collectors.mapping(
                    tuple -> new OptionResult(
                        tuple.get(productOptionJpaEntity.id),
                        tuple.get(productOptionJpaEntity.name),
                        tuple.get(productOptionJpaEntity.additionalPrice),
                        Boolean.TRUE.equals(tuple.get(productOptionJpaEntity.soldOut))
                    ),
                    Collectors.toList()
                )
            ));

        return groups.stream()
            .map(tuple -> new OptionGroupResult(
                tuple.get(productOptionGroupJpaEntity.id),
                tuple.get(productOptionGroupJpaEntity.name),
                tuple.get(productOptionGroupJpaEntity.description),
                Boolean.TRUE.equals(tuple.get(productOptionGroupJpaEntity.required)),
                Boolean.TRUE.equals(tuple.get(productOptionGroupJpaEntity.multipleSelect)),
                tuple.get(productOptionGroupJpaEntity.minSelect),
                tuple.get(productOptionGroupJpaEntity.maxSelect),
                false,
                optionsByGroupId.getOrDefault(tuple.get(productOptionGroupJpaEntity.id), Collections.emptyList())
            ))
            .toList();
    }

    private List<OptionGroupResult> findCommonOptionGroups(Long productId) {
        List<Tuple> groups = queryFactory
            .select(
                productCommonOptionGroupJpaEntity.id,
                productCommonOptionGroupJpaEntity.name,
                productCommonOptionGroupJpaEntity.description,
                productCommonOptionGroupJpaEntity.required,
                productCommonOptionGroupJpaEntity.multipleSelect,
                productCommonOptionGroupJpaEntity.minSelect,
                productCommonOptionGroupJpaEntity.maxSelect
            )
            .from(productCommonOptionGroupJpaEntity)
            .where(
                commonOptionGroupProductId().eq(productId),
                productCommonOptionGroupJpaEntity.visible.eq(true)
            )
            .orderBy(productCommonOptionGroupJpaEntity.sort.asc())
            .fetch();

        if (groups.isEmpty()) {
            return List.of();
        }

        List<Long> groupIds = groups.stream().map(tuple -> tuple.get(productCommonOptionGroupJpaEntity.id)).toList();
        // select와 Tuple.get이 같은 표현식을 참조하도록 numberPath를 지역 변수로 추출한다.
        NumberPath<Long> commonOptionGroupId = commonOptionGroupId();
        Map<Long, List<OptionResult>> optionsByGroupId = queryFactory
            .select(
                commonOptionGroupId,
                productCommonOptionJpaEntity.id,
                productCommonOptionJpaEntity.name,
                productCommonOptionJpaEntity.additionalPrice,
                productCommonOptionJpaEntity.soldOut
            )
            .from(productCommonOptionJpaEntity)
            .where(
                commonOptionGroupId.in(groupIds),
                productCommonOptionJpaEntity.visible.eq(true)
            )
            .orderBy(productCommonOptionJpaEntity.sort.asc())
            .fetch()
            .stream()
            .filter(tuple -> tuple.get(commonOptionGroupId) != null)
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(commonOptionGroupId)),
                LinkedHashMap::new,
                Collectors.mapping(
                    tuple -> new OptionResult(
                        tuple.get(productCommonOptionJpaEntity.id),
                        tuple.get(productCommonOptionJpaEntity.name),
                        tuple.get(productCommonOptionJpaEntity.additionalPrice),
                        Boolean.TRUE.equals(tuple.get(productCommonOptionJpaEntity.soldOut))
                    ),
                    Collectors.toList()
                )
            ));

        return groups.stream()
            .map(tuple -> new OptionGroupResult(
                tuple.get(productCommonOptionGroupJpaEntity.id),
                tuple.get(productCommonOptionGroupJpaEntity.name),
                tuple.get(productCommonOptionGroupJpaEntity.description),
                Boolean.TRUE.equals(tuple.get(productCommonOptionGroupJpaEntity.required)),
                Boolean.TRUE.equals(tuple.get(productCommonOptionGroupJpaEntity.multipleSelect)),
                tuple.get(productCommonOptionGroupJpaEntity.minSelect),
                tuple.get(productCommonOptionGroupJpaEntity.maxSelect),
                true,
                optionsByGroupId.getOrDefault(tuple.get(productCommonOptionGroupJpaEntity.id), Collections.emptyList())
            ))
            .toList();
    }

    /**
     * 장바구니 배치 조회. (상품ID, 옵션ID) 조합 목록을 받아 상품 단위로 그룹핑하여 반환합니다.
     * <ul>
     *   <li>존재하지 않거나 비활성인 상품은 제외하지 않고 available=false 로 남깁니다
     *       (프론트가 "판매 종료" 안내를 띄울 수 있도록 — 쿠팡 cartItemEnable 방식)</li>
     *   <li>옵션은 해당 상품에 실제로 속하고 조회에 성공한 경우에만 options 에 포함됩니다.</li>
     *   <li>요청한 productId 의 최초 등장 순서를 유지합니다.</li>
     * </ul>
     * 상품/옵션/그룹을 각각 배치(in) 조회하여 N+1 을 방지합니다.
     */
    public List<ProductBatchResult> findProductsBatch(List<ProductBatchItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        List<Long> productIds = items.stream()
            .map(ProductBatchItem::productId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        List<Long> optionIds = items.stream()
            .map(ProductBatchItem::optionId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        Map<Long, Tuple> productById = findActiveProductSummaries(productIds);
        Map<Long, String> imagePathByProductId = findRepresentativeImagePaths(productIds);
        Map<Long, BatchOptionInfo> optionById = findBatchOptions(optionIds);
        Map<Long, Long> ownerProductIdByGroupId = findOptionGroupOwners(optionById.values());

        // 요청 순서(productId 최초 등장순)를 유지하며 상품별로 옵션을 그룹핑.
        // 미존재 상품도 available=false 로 남기기 위해 모든 요청 productId 를 키로 등록한다.
        Map<Long, List<BatchOptionResult>> optionsByProductId = new LinkedHashMap<>();
        for (ProductBatchItem item : items) {
            Long productId = item.productId();
            if (productId == null) {
                continue;
            }
            optionsByProductId.computeIfAbsent(productId, key -> new ArrayList<>());

            // 상품이 없으면 옵션도 채울 수 없으므로 건너뛴다(키는 위에서 이미 등록됨).
            if (!productById.containsKey(productId)) {
                continue;
            }

            Long optionId = item.optionId();
            if (optionId == null) {
                continue;
            }
            BatchOptionInfo optionInfo = optionById.get(optionId);
            if (optionInfo == null) {
                continue;
            }
            // 그룹이 없거나, 옵션이 요청한 상품에 속하지 않으면 제외
            // (ownerProductId 가 null 이면 productId.equals 가 false 이므로 함께 걸러진다)
            if (!productId.equals(ownerProductIdByGroupId.get(optionInfo.groupKey()))) {
                continue;
            }
            List<BatchOptionResult> bucket = optionsByProductId.get(productId);
            boolean alreadyAdded = bucket.stream().anyMatch(option -> option.id().equals(optionId));
            if (!alreadyAdded) {
                bucket.add(new BatchOptionResult(optionId, optionInfo.name(), optionInfo.additionalPrice()));
            }
        }

        return optionsByProductId.entrySet().stream()
            .map(entry -> {
                Tuple product = productById.get(entry.getKey());
                if (product == null) {
                    // 존재하지 않거나 비활성인 상품: available=false 로 남긴다.
                    return new ProductBatchResult(entry.getKey(), false, null, null, null, null, null, List.of());
                }
                return new ProductBatchResult(
                    product.get(productJpaEntity.id),
                    true,
                    product.get(productJpaEntity.name),
                    fileUrlResolver.resolve(imagePathByProductId.get(entry.getKey())),
                    product.get(productJpaEntity.originalPrice),
                    product.get(productJpaEntity.discountInfo.discountPrice),
                    product.get(productJpaEntity.discountInfo.discountRate),
                    entry.getValue()
                );
            })
            .toList();
    }

    private Map<Long, Tuple> findActiveProductSummaries(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        return queryFactory
            .select(
                productJpaEntity.id,
                productJpaEntity.name,
                productJpaEntity.originalPrice,
                productJpaEntity.discountInfo.discountPrice,
                productJpaEntity.discountInfo.discountRate
            )
            .from(productJpaEntity)
            .where(productJpaEntity.id.in(productIds), productJpaEntity.visible.eq(true))
            .fetch()
            .stream()
            .filter(tuple -> tuple.get(productJpaEntity.id) != null)
            .collect(Collectors.toMap(
                tuple -> Objects.requireNonNull(tuple.get(productJpaEntity.id)),
                tuple -> tuple,
                (existing, ignored) -> existing
            ));
    }

    private Map<Long, BatchOptionInfo> findBatchOptions(List<Long> optionIds) {
        if (optionIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, BatchOptionInfo> optionById = new HashMap<>();

        // select와 Tuple.get이 같은 표현식을 참조하도록 numberPath를 지역 변수로 추출한다.
        NumberPath<Long> optionGroupId = optionGroupId();
        NumberPath<Long> commonOptionGroupId = commonOptionGroupId();

        queryFactory
            .select(
                productOptionJpaEntity.id,
                optionGroupId,
                productOptionJpaEntity.name,
                productOptionJpaEntity.additionalPrice
            )
            .from(productOptionJpaEntity)
            .where(productOptionJpaEntity.id.in(optionIds), productOptionJpaEntity.visible.eq(true))
            .fetch()
            .forEach(tuple -> optionById.put(
                tuple.get(productOptionJpaEntity.id),
                new BatchOptionInfo(
                    tuple.get(optionGroupId),
                    tuple.get(productOptionJpaEntity.name),
                    tuple.get(productOptionJpaEntity.additionalPrice),
                    false
                )
            ));

        queryFactory
            .select(
                productCommonOptionJpaEntity.id,
                commonOptionGroupId,
                productCommonOptionJpaEntity.name,
                productCommonOptionJpaEntity.additionalPrice
            )
            .from(productCommonOptionJpaEntity)
            .where(productCommonOptionJpaEntity.id.in(optionIds), productCommonOptionJpaEntity.visible.eq(true))
            .fetch()
            .forEach(tuple -> optionById.putIfAbsent(
                tuple.get(productCommonOptionJpaEntity.id),
                new BatchOptionInfo(
                    tuple.get(commonOptionGroupId),
                    tuple.get(productCommonOptionJpaEntity.name),
                    tuple.get(productCommonOptionJpaEntity.additionalPrice),
                    true
                )
            ));

        return optionById;
    }

    /**
     * 옵션의 소속 상품 검증용 — 옵션 그룹(개별/공통)의 소유 상품을 조회한다. 개별·공통 그룹의 id 공간이
     * 서로 겹칠 수 있으므로, 결과 키는 {@link BatchOptionInfo#groupKey()}(공통 여부를 함께 담은 키)다.
     */
    private Map<Long, Long> findOptionGroupOwners(java.util.Collection<BatchOptionInfo> options) {
        List<Long> normalGroupIds = options.stream()
            .filter(info -> !info.common())
            .map(BatchOptionInfo::groupId)
            .distinct()
            .toList();
        List<Long> commonGroupIds = options.stream()
            .filter(BatchOptionInfo::common)
            .map(BatchOptionInfo::groupId)
            .distinct()
            .toList();

        Map<Long, Long> ownerByGroupKey = new HashMap<>();

        // select와 Tuple.get이 같은 표현식을 참조하도록 numberPath를 지역 변수로 추출한다.
        NumberPath<Long> optionGroupProductId = optionGroupProductId();
        NumberPath<Long> commonOptionGroupProductId = commonOptionGroupProductId();

        if (!normalGroupIds.isEmpty()) {
            queryFactory
                .select(productOptionGroupJpaEntity.id, optionGroupProductId)
                .from(productOptionGroupJpaEntity)
                .where(productOptionGroupJpaEntity.id.in(normalGroupIds))
                .fetch()
                .forEach(tuple -> ownerByGroupKey.put(
                    BatchOptionInfo.groupKey(tuple.get(productOptionGroupJpaEntity.id), false),
                    tuple.get(optionGroupProductId)
                ));
        }

        if (!commonGroupIds.isEmpty()) {
            queryFactory
                .select(productCommonOptionGroupJpaEntity.id, commonOptionGroupProductId)
                .from(productCommonOptionGroupJpaEntity)
                .where(productCommonOptionGroupJpaEntity.id.in(commonGroupIds))
                .fetch()
                .forEach(tuple -> ownerByGroupKey.put(
                    BatchOptionInfo.groupKey(tuple.get(productCommonOptionGroupJpaEntity.id), true),
                    tuple.get(commonOptionGroupProductId)
                ));
        }

        return ownerByGroupKey;
    }

    /**
     * 상품의 노출 이미지 표시용 URL 목록(sort 오름차순). 화면 이미지 갤러리용.
     */
    public List<String> findProductImageUrls(Long productId) {
        List<String> filePaths = queryFactory
            .select(uploadedFileJpaEntity.filePath)
            .from(productImageJpaEntity)
            .innerJoin(uploadedFileJpaEntity).on(imageFileId().eq(uploadedFileJpaEntity.id))
            .where(imageProductId().eq(productId), productImageJpaEntity.visible.eq(true))
            .orderBy(productImageJpaEntity.sort.asc())
            .fetch();

        return fileUrlResolver.resolveAll(filePaths);
    }

    /**
     * 가게 상세 화면의 상품 목록 — 대표 상품·평점 순으로, 카테고리별 그룹핑은 소비 모듈이 수행한다.
     */
    public List<ShopProductItemResult> findShopProducts(Long shopId) {
        return queryFactory
            .select(new QShopProductItemResult(
                productJpaEntity.id,
                productCategoryId(),
                productJpaEntity.name,
                uploadedFileJpaEntity.filePath,
                productJpaEntity.originalPrice,
                productJpaEntity.discountInfo.discountPrice,
                productJpaEntity.discountInfo.discountRate,
                productJpaEntity.rating,
                productJpaEntity.reviewCount,
                productJpaEntity.representative,
                productJpaEntity.spiciness,
                productJpaEntity.soldOut
            ))
            .from(productJpaEntity)
            .leftJoin(productImageJpaEntity).on(representativeImageOf(productJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(imageFileId().eq(uploadedFileJpaEntity.id))
            .where(productShopId().eq(shopId), productJpaEntity.visible.eq(true))
            .orderBy(
                productJpaEntity.representative.desc(),
                productJpaEntity.rating.desc(),
                productJpaEntity.id.asc()
            )
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
    }

    // ── admin ──────────────────────────────────────────────────────────────

    /**
     * 관리자 상품 목록 — 조건 페이징 조회.
     */
    public PageResult<ProductListItemResult> findProducts(ProductSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(productJpaEntity.count())
            .from(productJpaEntity)
            .where(
                shopIdEq(condition.shopId()),
                categoryIdEq(condition.productCategoryId()),
                nameContains(condition.name()),
                visibleEq(condition.visible()),
                soldOutEq(condition.soldOut())
            )
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ProductListItemResult> content = queryFactory
            .select(new QProductListItemResult(
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
            .innerJoin(shopJpaEntity).on(productShopId().eq(shopJpaEntity.id))
            .where(
                shopIdEq(condition.shopId()),
                categoryIdEq(condition.productCategoryId()),
                nameContains(condition.name()),
                visibleEq(condition.visible()),
                soldOutEq(condition.soldOut())
            )
            .orderBy(productJpaEntity.sort.asc(), productJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    /**
     * 상품 상세 — web(간략)·admin(관리) 양쪽이 공유한다. 노출 여부와 무관하게 단건 조회한다.
     */
    public Optional<ProductDetailResult> findProductDetailById(Long productId) {
        return Optional.ofNullable(
            queryFactory
                .select(new QProductDetailResult(
                    productJpaEntity.id,
                    productShopId(),
                    productCategoryId(),
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
                    productJpaEntity.createdAt,
                    productJpaEntity.updatedAt
                ))
                .from(productJpaEntity)
                .where(productJpaEntity.id.eq(productId))
                .fetchOne()
        );
    }

    /**
     * 가게의 노출 상품 카테고리 목록(sort 오름차순).
     */
    public List<ProductCategoryResult> findProductCategories(Long shopId) {
        return queryFactory
            .select(new QProductCategoryResult(
                productCategoryJpaEntity.id,
                categoryShopId(),
                productCategoryJpaEntity.name,
                productCategoryJpaEntity.sort,
                productCategoryJpaEntity.visible
            ))
            .from(productCategoryJpaEntity)
            .where(categoryShopId().eq(shopId), productCategoryJpaEntity.visible.eq(true))
            .orderBy(productCategoryJpaEntity.sort.asc())
            .fetch();
    }

    // ── batch ──────────────────────────────────────────────────────────────

    /**
     * BBQ 옵션 동기화가 필요한 상품 1건 — 동기화 대상 식별자와 기본 옵션 생성용 상품명을 함께 투영한다.
     */
    public Optional<ProductBbqSyncTargetResult> findFirstBbqSyncTarget() {
        return Optional.ofNullable(
            queryFactory
                .select(new QProductBbqSyncTargetResult(
                    bbqProductId(),
                    bbqMenuId(),
                    productJpaEntity.name
                ))
                .from(productBbqJpaEntity)
                .innerJoin(productJpaEntity).on(bbqProductId().eq(productJpaEntity.id))
                .where(productBbqJpaEntity.optionsSynced.eq(false))
                .fetchFirst()
        );
    }

    /**
     * 투영된 저장 경로를 표시용 URL로 바꿔 재조립한다. {@code @QueryProjection}은 생성자 직접 투영이라
     * 변환을 투영식에 넣을 수 없어 fetch 직후 호출한다.
     */
    private SearchProductItemResult withResolvedImageUrl(SearchProductItemResult row) {
        return new SearchProductItemResult(
            row.id(),
            row.shopName(),
            row.name(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.originalPrice(),
            row.discountPrice(),
            row.discountRate(),
            row.rating(),
            row.reviewCount(),
            row.representative(),
            row.spiciness()
        );
    }

    private TodayDiscountProductResult withResolvedImageUrl(TodayDiscountProductResult row) {
        return new TodayDiscountProductResult(
            row.id(),
            row.shopName(),
            row.name(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.originalPrice(),
            row.discountPrice(),
            row.discountRate()
        );
    }

    private ShopProductItemResult withResolvedImageUrl(ShopProductItemResult row) {
        return new ShopProductItemResult(
            row.id(),
            row.productCategoryId(),
            row.name(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.originalPrice(),
            row.discountPrice(),
            row.discountRate(),
            row.rating(),
            row.reviewCount(),
            row.representative(),
            row.spiciness(),
            row.soldOut()
        );
    }

    // ── 공용 조건 헬퍼 ─────────────────────────────────────────────────────

    /**
     * 상품의 대표 이미지(노출 중 최소 sort) 한 건만 조인하기 위한 on 조건.
     */
    private BooleanExpression representativeImageOf(NumberPath<Long> productIdPath) {
        return imageProductId().eq(productIdPath)
            .and(productImageJpaEntity.visible.eq(true))
            .and(productImageJpaEntity.sort.eq(
                JPAExpressions
                    .select(subProductImage.sort.min())
                    .from(subProductImage)
                    .where(subImageProductId().eq(productIdPath)
                        .and(subProductImage.visible.eq(true)))
            ));
    }

    private Map<Long, String> findRepresentativeImagePaths(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        // select와 Tuple.get이 같은 표현식을 참조하도록 numberPath를 지역 변수로 추출한다.
        NumberPath<Long> imageProductId = imageProductId();
        return queryFactory
            .select(imageProductId, uploadedFileJpaEntity.filePath)
            .from(productImageJpaEntity)
            .innerJoin(uploadedFileJpaEntity).on(imageFileId().eq(uploadedFileJpaEntity.id))
            .where(
                imageProductId.in(productIds),
                productImageJpaEntity.visible.eq(true),
                productImageJpaEntity.sort.eq(
                    JPAExpressions
                        .select(subProductImage.sort.min())
                        .from(subProductImage)
                        .where(subImageProductId().eq(imageProductId)
                            .and(subProductImage.visible.eq(true)))
                )
            )
            .fetch()
            .stream()
            .filter(tuple -> tuple.get(imageProductId) != null
                && tuple.get(uploadedFileJpaEntity.filePath) != null)
            .collect(Collectors.toMap(
                tuple -> Objects.requireNonNull(tuple.get(imageProductId)),
                tuple -> Objects.requireNonNull(tuple.get(uploadedFileJpaEntity.filePath)),
                (existing, ignored) -> existing
            ));
    }

    private BooleanExpression shopIdEq(Long shopId) {
        return shopId != null ? productShopId().eq(shopId) : null;
    }

    private BooleanExpression categoryIdEq(Long productCategoryId) {
        return productCategoryId != null ? productCategoryId().eq(productCategoryId) : null;
    }

    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? productJpaEntity.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression visibleEq(Boolean visible) {
        return visible != null ? productJpaEntity.visible.eq(visible) : null;
    }

    private BooleanExpression soldOutEq(Boolean soldOut) {
        return soldOut != null ? productJpaEntity.soldOut.eq(soldOut) : null;
    }

    // ── @Convert VO 컬럼의 raw Long path ───────────────────────────────────

    /**
     * {@code @Convert} VO 컬럼인 {@code PRODUCT.shop_id}를 raw {@code Long}으로 비교·투영하기 위한 path.
     */
    private NumberPath<Long> productShopId() {
        return Expressions.numberPath(Long.class, productJpaEntity, "shopId");
    }

    /**
     * {@code @Convert} VO 컬럼인 {@code PRODUCT.product_category_id}를 raw {@code Long}으로
     * 비교·투영하기 위한 path.
     */
    private NumberPath<Long> productCategoryId() {
        return Expressions.numberPath(Long.class, productJpaEntity, "productCategoryId");
    }

    /**
     * {@code @Convert} VO 컬럼인 {@code PRODUCT_CATEGORY.shop_id}를 raw {@code Long}으로
     * 비교·투영하기 위한 path.
     */
    private NumberPath<Long> categoryShopId() {
        return Expressions.numberPath(Long.class, productCategoryJpaEntity, "shopId");
    }

    /**
     * {@code @Convert} VO 컬럼인 {@code PRODUCT_OPTION_GROUP.product_id}를 raw {@code Long}으로
     * 비교·투영하기 위한 path.
     */
    private NumberPath<Long> optionGroupProductId() {
        return Expressions.numberPath(Long.class, productOptionGroupJpaEntity, "productId");
    }

    /**
     * {@code @Convert} VO 컬럼인 {@code PRODUCT_COMMON_OPTION_GROUP.product_id}를 raw {@code Long}으로
     * 비교·투영하기 위한 path.
     */
    private NumberPath<Long> commonOptionGroupProductId() {
        return Expressions.numberPath(Long.class, productCommonOptionGroupJpaEntity, "productId");
    }

    /**
     * {@code @Convert} VO 컬럼인 {@code PRODUCT_OPTION.option_group_id}를 raw {@code Long}으로
     * 비교·투영하기 위한 path.
     */
    private NumberPath<Long> optionGroupId() {
        return Expressions.numberPath(Long.class, productOptionJpaEntity, "optionGroupId");
    }

    /**
     * {@code @Convert} VO 컬럼인 {@code PRODUCT_COMMON_OPTION.option_group_id}를 raw {@code Long}으로
     * 비교·투영하기 위한 path.
     */
    private NumberPath<Long> commonOptionGroupId() {
        return Expressions.numberPath(Long.class, productCommonOptionJpaEntity, "optionGroupId");
    }

    /**
     * {@code @Convert} VO 컬럼인 {@code PRODUCT_IMAGE.product_id}를 raw {@code Long}으로
     * 비교·투영하기 위한 path.
     */
    private NumberPath<Long> imageProductId() {
        return Expressions.numberPath(Long.class, productImageJpaEntity, "productId");
    }

    /**
     * 대표 이미지 서브쿼리 별칭({@link #subProductImage})의 {@code @Convert} VO 컬럼
     * {@code product_id}를 raw {@code Long}으로 비교하기 위한 path.
     */
    private NumberPath<Long> subImageProductId() {
        return Expressions.numberPath(Long.class, subProductImage, "productId");
    }

    /**
     * {@code @Convert} VO 컬럼인 {@code PRODUCT_IMAGE.image_file_id}를 raw {@code Long}으로
     * 비교하기 위한 path.
     */
    private NumberPath<Long> imageFileId() {
        return Expressions.numberPath(Long.class, productImageJpaEntity, "imageFileId");
    }

    /**
     * {@code @Convert} VO 컬럼인 {@code PRODUCT_BBQ.product_id}를 raw {@code Long}으로
     * 비교·투영하기 위한 path.
     */
    private NumberPath<Long> bbqProductId() {
        return Expressions.numberPath(Long.class, productBbqJpaEntity, "productId");
    }

    /**
     * {@code @Convert} VO 컬럼인 {@code PRODUCT_BBQ.bbq_menu_id}를 raw {@code Long}으로
     * 투영하기 위한 path.
     */
    private NumberPath<Long> bbqMenuId() {
        return Expressions.numberPath(Long.class, productBbqJpaEntity, "bbqMenuId");
    }

    /**
     * 배치 조회 내부 계산용 옵션 정보(개별/공통 구분 포함). DAO 밖으로 나가지 않는다.
     */
    private record BatchOptionInfo(Long groupId, String name, Integer additionalPrice, boolean common) {

        /**
         * 개별·공통 그룹의 id 공간이 겹칠 수 있어, 소유 상품 맵의 키는 공통 여부를 함께 인코딩한다.
         */
        Long groupKey() {
            return groupKey(groupId, common);
        }

        static Long groupKey(Long groupId, boolean common) {
            return common ? -groupId : groupId;
        }
    }
}
