package com.tastyhouse.infrastructure.product.query;

import com.tastyhouse.application.product.port.out.ProductBatchItem;
import com.tastyhouse.application.product.port.out.ProductManagementQueryPort;
import com.tastyhouse.application.product.port.out.ProductOwnerQueryPort;
import com.tastyhouse.application.product.port.out.ProductQueryPort;
import com.tastyhouse.application.product.port.out.BatchOptionResult;
import com.tastyhouse.application.product.port.out.OptionGroupResult;
import com.tastyhouse.application.product.port.out.OptionResult;
import com.tastyhouse.application.product.port.out.PopularProductItemResult;
import com.tastyhouse.application.product.port.out.ProductAvailabilityItemResult;
import com.tastyhouse.application.product.port.out.ProductAvailabilitySearchCondition;
import com.tastyhouse.application.product.port.out.ProductBatchResult;
import com.tastyhouse.application.product.port.out.ProductBbqSyncQueryPort;
import com.tastyhouse.application.product.port.out.ProductBbqSyncTargetResult;
import com.tastyhouse.application.product.port.out.ProductCategoryManagementResult;
import com.tastyhouse.application.product.port.out.ProductCategoryResult;
import com.tastyhouse.application.product.port.out.ProductDetailResult;
import com.tastyhouse.application.product.port.out.ProductExposurePeriodResult;
import com.tastyhouse.application.product.port.out.ProductImageChangeRequestResult;
import com.tastyhouse.application.product.port.out.ProductImageManagementResult;
import com.tastyhouse.application.product.port.out.ProductListItemResult;
import com.tastyhouse.application.product.port.out.ProductManagementDetailResult;
import com.tastyhouse.application.product.port.out.ProductNutritionResult;
import com.tastyhouse.application.product.port.out.ProductOptionAvailabilityGroupResult;
import com.tastyhouse.application.product.port.out.ProductOptionAvailabilityItemResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupLinkedProductResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupManagementResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupMergeCandidateResult;
import com.tastyhouse.application.product.port.out.ProductOptionManagementResult;
import com.tastyhouse.application.product.port.out.ProductOptionsResult;
import com.tastyhouse.application.product.port.out.ProductPriceResult;
import com.tastyhouse.application.product.port.out.ProductRepresentativeRequestResult;
import com.tastyhouse.application.product.port.out.ProductSearchCondition;
import com.tastyhouse.application.product.port.out.ProductVegetarianRequestResult;
import com.tastyhouse.application.product.port.out.ProductVegetarianSettingResult;
import com.tastyhouse.application.product.port.out.SearchProductItemResult;
import com.tastyhouse.application.product.port.out.ShopProductItemResult;
import com.tastyhouse.application.product.port.out.TodayDiscountProductResult;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.domain.product.service.CupDepositPolicy;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.order.persistence.QOrderJpaEntity.orderJpaEntity;
import static com.tastyhouse.infrastructure.order.persistence.QOrderProductJpaEntity.orderProductJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductBbqJpaEntity.productBbqJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductCategoryJpaEntity.productCategoryJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductCommonOptionGroupJpaEntity.productCommonOptionGroupJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductCommonOptionGroupLinkJpaEntity.productCommonOptionGroupLinkJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductCommonOptionJpaEntity.productCommonOptionJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductImageChangeRequestJpaEntity.productImageChangeRequestJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductAllergenJpaEntity.productAllergenJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductImageJpaEntity.productImageJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductNutritionJpaEntity.productNutritionJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductJpaEntity.productJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductOptionGroupJpaEntity.productOptionGroupJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductOptionGroupLinkJpaEntity.productOptionGroupLinkJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductOptionGroupMergeExclusionJpaEntity.productOptionGroupMergeExclusionJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductOptionJpaEntity.productOptionJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductPriceJpaEntity.productPriceJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductShopLinkJpaEntity.productShopLinkJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductRepresentativeRequestJpaEntity.productRepresentativeRequestJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductVegetarianRequestJpaEntity.productVegetarianRequestJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;

@Repository
public class ProductQueryDao implements ProductQueryPort, ProductBbqSyncQueryPort, ProductManagementQueryPort, ProductOwnerQueryPort {
    private static final com.tastyhouse.infrastructure.product.persistence.QProductImageJpaEntity subProductImage =
        new com.tastyhouse.infrastructure.product.persistence.QProductImageJpaEntity("subProductImage");

    private static final com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity
        imageChangeRequestFile =
        new com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity("imageChangeRequestFile");

    private static final com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity
        productImageFile =
        new com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity("productImageFile");

    private static final com.tastyhouse.infrastructure.product.persistence.QProductExposureHourJpaEntity
        subExposureHour =
        new com.tastyhouse.infrastructure.product.persistence.QProductExposureHourJpaEntity("subExposureHour");

    private static final com.tastyhouse.infrastructure.product.persistence.QProductJpaEntity subCategoryProduct =
        new com.tastyhouse.infrastructure.product.persistence.QProductJpaEntity("subCategoryProduct");

    private static final com.tastyhouse.infrastructure.product.persistence.QProductOptionGroupLinkJpaEntity
        subOptionGroupLink =
        new com.tastyhouse.infrastructure.product.persistence.QProductOptionGroupLinkJpaEntity("subOptionGroupLink");

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private static final int POPULAR_PRODUCT_LIMIT = 5;

    private static final long POPULAR_PRODUCT_WINDOW_DAYS = 30L;

    private static final String MERGE_CANDIDATE_SQL = """
        WITH group_sig AS (
            SELECT g.id AS option_group_id, g.name AS group_name, g.min_select, g.max_select,
                   CONCAT_WS('|', g.name, IFNULL(g.min_select,''), IFNULL(g.max_select,''),
                             COUNT(o.id),
                             IFNULL(GROUP_CONCAT(CONCAT(o.name, ':', o.additional_price)
                                                 ORDER BY o.name, o.additional_price SEPARATOR ','), '')
                   ) AS sig_payload
              FROM PRODUCT_OPTION_GROUP g
              JOIN PRODUCT_OPTION_GROUP_LINK l ON l.option_group_id = g.id
              JOIN PRODUCT p ON p.id = l.product_id
              LEFT JOIN PRODUCT_OPTION o ON o.option_group_id = g.id AND o.is_visible = 1
             WHERE p.shop_id = :shopId AND p.is_deleted = 0 AND g.is_visible = 1
             GROUP BY g.id, g.name, g.min_select, g.max_select
        )
        SELECT s.option_group_id, s.group_name, s.min_select, s.max_select, s.sig_payload
          FROM group_sig s
         WHERE s.sig_payload IN (
                   SELECT sig_payload FROM group_sig GROUP BY sig_payload HAVING COUNT(*) > 1
               )
         ORDER BY s.sig_payload, s.option_group_id
        """;

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;
    private final EntityManager entityManager;
    private final CupDepositPolicy cupDepositPolicy;

    public ProductQueryDao(
        JPAQueryFactory queryFactory,
        FileUrlResolver fileUrlResolver,
        EntityManager entityManager,
        CupDepositPolicy cupDepositPolicy
    ) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
        this.entityManager = entityManager;
        this.cupDepositPolicy = cupDepositPolicy;
    }

    @Override
    public PageResult<TodayDiscountProductResult> findTodayDiscountProducts(PageQuery pageQuery) {
        LocalDateTime now = nowInServiceZone();
        JPAQuery<TodayDiscountProductResult> query = queryFactory
            .select(Projections.constructor(TodayDiscountProductResult.class,
                productJpaEntity.id,
                shopJpaEntity.name,
                productJpaEntity.name,
                uploadedFileJpaEntity.filePath,
                productJpaEntity.originalPrice,
                productJpaEntity.discountInfo.discountPrice,
                productJpaEntity.discountInfo.discountRate
            ))
            .from(productJpaEntity)
            .innerJoin(shopJpaEntity).on(productJpaEntity.shopId.eq(shopJpaEntity.id))
            .leftJoin(productImageJpaEntity).on(representativeImageOf(productJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(todayDiscountSearchable(now))
            .orderBy(productJpaEntity.discountInfo.discountRate.desc());

        long total = countTodayDiscountProducts(now);

        List<TodayDiscountProductResult> products = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();

        return PageResult.of(products, total, pageQuery.page(), pageQuery.size());
    }

    private long countTodayDiscountProducts(LocalDateTime now) {
        Long total = queryFactory
            .select(productJpaEntity.count())
            .from(productJpaEntity)
            .innerJoin(shopJpaEntity).on(productJpaEntity.shopId.eq(shopJpaEntity.id))
            .where(todayDiscountSearchable(now))
            .fetchOne();

        return total == null ? 0L : total;
    }

    @Override
    public PageResult<SearchProductItemResult> searchByKeyword(String keyword, PageQuery pageQuery) {
        BooleanExpression searchable = productJpaEntity.name.containsIgnoreCase(keyword)
            .and(productJpaEntity.visible.eq(true))
            .and(notDeleted())
            .and(productJpaEntity.soldOut.eq(false))
            .and(shopJpaEntity.permanentlyClosed.eq(false))
            .and(shopJpaEntity.hidden.eq(false))
            .and(exposedNow(nowInServiceZone()));

        Long total = queryFactory
            .select(productJpaEntity.count())
            .from(productJpaEntity)
            .innerJoin(shopJpaEntity).on(productJpaEntity.shopId.eq(shopJpaEntity.id))
            .where(searchable)
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<SearchProductItemResult> content = queryFactory
            .select(Projections.constructor(SearchProductItemResult.class,
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
            .innerJoin(shopJpaEntity).on(productJpaEntity.shopId.eq(shopJpaEntity.id))
            .leftJoin(productImageJpaEntity).on(representativeImageOf(productJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
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

    @Override
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
                productOptionGroupJpaEntity.maxSelect,
                productOptionGroupJpaEntity.groupType
            )
            .from(productOptionGroupJpaEntity)
            .innerJoin(productOptionGroupLinkJpaEntity)
            .on(productOptionGroupLinkJpaEntity.optionGroupId.eq(productOptionGroupJpaEntity.id))
            .where(
                productOptionGroupLinkJpaEntity.productId.eq(productId),
                productOptionGroupJpaEntity.visible.eq(true)
            )
            .orderBy(productOptionGroupLinkJpaEntity.sort.asc())
            .fetch();

        if (groups.isEmpty()) {
            return List.of();
        }

        List<Long> groupIds = groups.stream().map(tuple -> tuple.get(productOptionGroupJpaEntity.id)).toList();
        NumberExpression<Long> optionGroupId = productOptionJpaEntity.optionGroupId;
        Map<Long, List<OptionResult>> optionsByGroupId = queryFactory
            .select(
                optionGroupId,
                productOptionJpaEntity.id,
                productOptionJpaEntity.name,
                productOptionJpaEntity.additionalPrice,
                productOptionJpaEntity.soldOut,
                productOptionJpaEntity.cupCount,
                productOptionJpaEntity.personalCupDiscountAmount
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
                        Boolean.TRUE.equals(tuple.get(productOptionJpaEntity.soldOut)),
                        tuple.get(productOptionJpaEntity.cupCount),
                        cupDepositPolicy.depositAmountOf(tuple.get(productOptionJpaEntity.cupCount)),
                        tuple.get(productOptionJpaEntity.personalCupDiscountAmount)
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
                groupTypeNameOf(tuple.get(productOptionGroupJpaEntity.groupType)),
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
            .innerJoin(productCommonOptionGroupLinkJpaEntity)
            .on(productCommonOptionGroupLinkJpaEntity.optionGroupId.eq(productCommonOptionGroupJpaEntity.id))
            .where(
                productCommonOptionGroupLinkJpaEntity.productId.eq(productId),
                productCommonOptionGroupJpaEntity.visible.eq(true)
            )
            .orderBy(productCommonOptionGroupLinkJpaEntity.sort.asc())
            .fetch();

        if (groups.isEmpty()) {
            return List.of();
        }

        List<Long> groupIds = groups.stream().map(tuple -> tuple.get(productCommonOptionGroupJpaEntity.id)).toList();
        NumberExpression<Long> commonOptionGroupId = productCommonOptionJpaEntity.optionGroupId;
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
                        Boolean.TRUE.equals(tuple.get(productCommonOptionJpaEntity.soldOut)),
                        null,
                        0,
                        null
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
                ProductOptionGroupType.NORMAL.name(),
                optionsByGroupId.getOrDefault(tuple.get(productCommonOptionGroupJpaEntity.id), Collections.emptyList())
            ))
            .toList();
    }

    @Override
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
        Map<Long, Set<Long>> linkedProductIdsByGroupKey =
            findLinkedProductIdsByOptionGroup(optionById.values());

        Map<Long, List<BatchOptionResult>> optionsByProductId = new LinkedHashMap<>();
        for (ProductBatchItem item : items) {
            Long productId = item.productId();
            if (productId == null) {
                continue;
            }
            optionsByProductId.computeIfAbsent(productId, key -> new ArrayList<>());

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
            Set<Long> linkedProductIds = linkedProductIdsByGroupKey.get(optionInfo.groupKey());
            if (linkedProductIds == null || !linkedProductIds.contains(productId)) {
                continue;
            }
            List<BatchOptionResult> bucket = optionsByProductId.get(productId);
            boolean alreadyAdded = bucket.stream().anyMatch(option -> option.id().equals(optionId));
            if (!alreadyAdded) {
                bucket.add(new BatchOptionResult(
                    optionId,
                    optionInfo.name(),
                    optionInfo.additionalPrice(),
                    optionInfo.cupCount(),
                    cupDepositPolicy.depositAmountOf(optionInfo.cupCount()),
                    optionInfo.personalCupDiscountAmount()
                ));
            }
        }

        return optionsByProductId.entrySet().stream()
            .map(entry -> {
                Tuple product = productById.get(entry.getKey());
                if (product == null) {
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
            .where(productJpaEntity.id.in(productIds), productJpaEntity.visible.eq(true), notDeleted())
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

        NumberExpression<Long> optionGroupId = productOptionJpaEntity.optionGroupId;
        NumberExpression<Long> commonOptionGroupId = productCommonOptionJpaEntity.optionGroupId;

        queryFactory
            .select(
                productOptionJpaEntity.id,
                optionGroupId,
                productOptionJpaEntity.name,
                productOptionJpaEntity.additionalPrice,
                productOptionJpaEntity.cupCount,
                productOptionJpaEntity.personalCupDiscountAmount
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
                    false,
                    tuple.get(productOptionJpaEntity.cupCount),
                    tuple.get(productOptionJpaEntity.personalCupDiscountAmount)
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
                    true,
                    null,
                    null
                )
            ));

        return optionById;
    }

    private Map<Long, Set<Long>> findLinkedProductIdsByOptionGroup(
        java.util.Collection<BatchOptionInfo> options
    ) {
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

        Map<Long, Set<Long>> linkedProductIdsByGroupKey = new HashMap<>();

        if (!normalGroupIds.isEmpty()) {
            queryFactory
                .select(productOptionGroupLinkJpaEntity.optionGroupId, productOptionGroupLinkJpaEntity.productId)
                .from(productOptionGroupLinkJpaEntity)
                .where(productOptionGroupLinkJpaEntity.optionGroupId.in(normalGroupIds))
                .fetch()
                .forEach(tuple -> linkedProductIdsByGroupKey
                    .computeIfAbsent(
                        BatchOptionInfo.groupKey(
                            tuple.get(productOptionGroupLinkJpaEntity.optionGroupId), false),
                        key -> new HashSet<>())
                    .add(tuple.get(productOptionGroupLinkJpaEntity.productId)));
        }

        if (!commonGroupIds.isEmpty()) {
            queryFactory
                .select(
                    productCommonOptionGroupLinkJpaEntity.optionGroupId,
                    productCommonOptionGroupLinkJpaEntity.productId)
                .from(productCommonOptionGroupLinkJpaEntity)
                .where(productCommonOptionGroupLinkJpaEntity.optionGroupId.in(commonGroupIds))
                .fetch()
                .forEach(tuple -> linkedProductIdsByGroupKey
                    .computeIfAbsent(
                        BatchOptionInfo.groupKey(
                            tuple.get(productCommonOptionGroupLinkJpaEntity.optionGroupId), true),
                        key -> new HashSet<>())
                    .add(tuple.get(productCommonOptionGroupLinkJpaEntity.productId)));
        }

        return linkedProductIdsByGroupKey;
    }

    @Override
    public List<String> findProductImageUrls(Long productId) {
        List<String> filePaths = queryFactory
            .select(uploadedFileJpaEntity.filePath)
            .from(productImageJpaEntity)
            .innerJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(productImageJpaEntity.productId.eq(productId), productImageJpaEntity.visible.eq(true))
            .orderBy(productImageJpaEntity.sort.asc())
            .fetch();

        return fileUrlResolver.resolveAll(filePaths);
    }

    @Override
    public List<ShopProductItemResult> findShopProducts(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopProductItemResult.class,
                productJpaEntity.id,
                productShopLinkJpaEntity.productCategoryId,
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
            .from(productShopLinkJpaEntity)
            .innerJoin(productJpaEntity).on(productJpaEntity.id.eq(productShopLinkJpaEntity.productId))
            .leftJoin(productImageJpaEntity).on(representativeImageOf(productJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(productShopLinkJpaEntity.shopId.eq(shopId), productJpaEntity.visible.eq(true), notDeleted(),
                exposedNow(nowInServiceZone()))
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

    @Override
    public PageResult<ProductListItemResult> findProducts(ProductSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(productJpaEntity.count())
            .from(productJpaEntity)
            .where(
                shopIdEq(condition.shopId()),
                categoryIdEq(condition.productCategoryId()),
                nameContains(condition.name()),
                visibleEq(condition.visible()),
                soldOutEq(condition.soldOut()),
                notDeleted()
            )
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ProductListItemResult> content = queryFactory
            .select(Projections.constructor(ProductListItemResult.class,
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
            .innerJoin(shopJpaEntity).on(productJpaEntity.shopId.eq(shopJpaEntity.id))
            .where(
                shopIdEq(condition.shopId()),
                categoryIdEq(condition.productCategoryId()),
                nameContains(condition.name()),
                visibleEq(condition.visible()),
                soldOutEq(condition.soldOut()),
                notDeleted()
            )
            .orderBy(productJpaEntity.sort.asc(), productJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<ProductDetailResult> findProductDetailById(Long productId) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(ProductDetailResult.class,
                productJpaEntity.id,
                    productJpaEntity.shopId,
                    productJpaEntity.productCategoryId,
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
                    productJpaEntity.weightText,
                    productJpaEntity.createdAt,
                    productJpaEntity.updatedAt
                ))
                .from(productJpaEntity)
                .where(productJpaEntity.id.eq(productId), notDeleted())
                .fetchOne()
        );
    }

    @Override
    public List<ProductPriceResult> findProductPrices(Long productId) {
        return queryFactory
            .select(Projections.constructor(ProductPriceResult.class,
                productPriceJpaEntity.id,
                productPriceJpaEntity.productId,
                productPriceJpaEntity.priceName,
                productPriceJpaEntity.deliveryPrice,
                productPriceJpaEntity.storePrice,
                productPriceJpaEntity.pickupPrice,
                productPriceJpaEntity.sort,
                productPriceJpaEntity.pickupPriceSetAt
            ))
            .from(productPriceJpaEntity)
            .where(productPriceJpaEntity.productId.eq(productId))
            .orderBy(productPriceJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductPriceResult> findProductPricesByProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
            .select(Projections.constructor(ProductPriceResult.class,
                productPriceJpaEntity.id,
                productPriceJpaEntity.productId,
                productPriceJpaEntity.priceName,
                productPriceJpaEntity.deliveryPrice,
                productPriceJpaEntity.storePrice,
                productPriceJpaEntity.pickupPrice,
                productPriceJpaEntity.sort,
                productPriceJpaEntity.pickupPriceSetAt
            ))
            .from(productPriceJpaEntity)
            .where(productPriceJpaEntity.productId.in(productIds))
            .orderBy(productPriceJpaEntity.productId.asc(), productPriceJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductPriceResult> findShopProductPrices(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ProductPriceResult.class,
                productPriceJpaEntity.id,
                productPriceJpaEntity.productId,
                productPriceJpaEntity.priceName,
                productPriceJpaEntity.deliveryPrice,
                productPriceJpaEntity.storePrice,
                productPriceJpaEntity.pickupPrice,
                productPriceJpaEntity.sort,
                productPriceJpaEntity.pickupPriceSetAt
            ))
            .from(productPriceJpaEntity)
            .join(productJpaEntity).on(productJpaEntity.id.eq(productPriceJpaEntity.productId))
            .join(productShopLinkJpaEntity)
            .on(
                productShopLinkJpaEntity.productId.eq(productJpaEntity.id),
                productShopLinkJpaEntity.shopId.eq(shopId)
            )
            .where(notDeleted())
            .orderBy(productShopLinkJpaEntity.sort.asc(), productPriceJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public long countVisibleProducts(Long shopId) {
        Long count = queryFactory
            .select(productJpaEntity.count())
            .from(productJpaEntity)
            .where(productJpaEntity.shopId.eq(shopId), productJpaEntity.visible.isTrue(), notDeleted())
            .fetchOne();
        return count != null ? count : 0L;
    }

    @Override
    public Optional<ProductManagementDetailResult> findProductManagementDetailById(Long productId) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(ProductManagementDetailResult.class,
                productJpaEntity.id,
                    productJpaEntity.shopId,
                    productJpaEntity.productCategoryId,
                    productCategoryJpaEntity.name,
                    productJpaEntity.name,
                    productJpaEntity.composition,
                    productJpaEntity.description,
                    productJpaEntity.originalPrice,
                    productJpaEntity.discountInfo.discountPrice,
                    productJpaEntity.singleServing,
                    productJpaEntity.spiciness,
                    productJpaEntity.representative,
                    productJpaEntity.ratingExcluded,
                    productJpaEntity.soldOut,
                    productJpaEntity.visible,
                    uploadedFileJpaEntity.filePath,
                    productJpaEntity.vegetarianType,
                    productJpaEntity.weightText,
                    productJpaEntity.exposureStartDate.isNotNull()
                        .or(productJpaEntity.exposureEndDate.isNotNull())
                        .or(existsExposureHours(productJpaEntity.id))
                ))
                .from(productJpaEntity)
                .leftJoin(productCategoryJpaEntity).on(productJpaEntity.productCategoryId.eq(productCategoryJpaEntity.id))
                .leftJoin(productImageJpaEntity).on(representativeImageOf(productJpaEntity.id))
                .leftJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
                .where(productJpaEntity.id.eq(productId), notDeleted())
                .fetchOne()
        ).map(this::withResolvedImageUrl);
    }

    @Override
    public Optional<ProductNutritionResult> findNutrition(Long productId) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(ProductNutritionResult.class,
                    productNutritionJpaEntity.id,
                    productNutritionJpaEntity.productId,
                    productNutritionJpaEntity.servingSize,
                    productNutritionJpaEntity.totalAmount,
                    productNutritionJpaEntity.flavor,
                    productNutritionJpaEntity.size,
                    productNutritionJpaEntity.calorie,
                    productNutritionJpaEntity.sugars,
                    productNutritionJpaEntity.protein,
                    productNutritionJpaEntity.saturatedFat,
                    productNutritionJpaEntity.natrium,
                    productNutritionJpaEntity.carbohydrate,
                    productNutritionJpaEntity.cholesterol,
                    productNutritionJpaEntity.fat,
                    productNutritionJpaEntity.transFat,
                    productNutritionJpaEntity.caffeine,
                    productNutritionJpaEntity.setMenu
                ))
                .from(productNutritionJpaEntity)
                .where(productNutritionJpaEntity.productId.eq(productId))
                .fetchFirst()
        );
    }

    @Override
    public List<String> findAllergenTypes(Long productId) {
        return queryFactory
            .select(productAllergenJpaEntity.allergenType.stringValue())
            .from(productAllergenJpaEntity)
            .where(productAllergenJpaEntity.productId.eq(productId))
            .orderBy(productAllergenJpaEntity.id.asc())
            .fetch();
    }

    private BooleanExpression existsExposureHours(NumberPath<Long> productId) {
        return JPAExpressions
            .selectOne()
            .from(subExposureHour)
            .where(subExposureHour.productId.eq(productId))
            .exists();
    }

    private ProductManagementDetailResult withResolvedImageUrl(ProductManagementDetailResult row) {
        return new ProductManagementDetailResult(
            row.id(),
            row.shopId(),
            row.productCategoryId(),
            row.productCategoryName(),
            row.name(),
            row.composition(),
            row.description(),
            row.originalPrice(),
            row.discountPrice(),
            row.singleServing(),
            row.spiciness(),
            row.representative(),
            row.ratingExcluded(),
            row.soldOut(),
            row.visible(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.vegetarianType(),
            row.weightText(),
            row.exposureScheduled()
        );
    }

    @Override
    public List<ProductCategoryResult> findProductCategories(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ProductCategoryResult.class,
                productCategoryJpaEntity.id,
                productCategoryJpaEntity.shopId,
                productCategoryJpaEntity.name,
                productCategoryJpaEntity.description,
                productCategoryJpaEntity.sort,
                productCategoryJpaEntity.visible
            ))
            .from(productCategoryJpaEntity)
            .where(productCategoryJpaEntity.shopId.eq(shopId), productCategoryJpaEntity.visible.eq(true))
            .orderBy(productCategoryJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductCategoryManagementResult> findProductCategoriesForManagement(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ProductCategoryManagementResult.class,
                productCategoryJpaEntity.id,
                productCategoryJpaEntity.shopId,
                productCategoryJpaEntity.name,
                productCategoryJpaEntity.description,
                productCategoryJpaEntity.sort,
                productCategoryJpaEntity.visible,
                JPAExpressions
                    .select(subCategoryProduct.count())
                    .from(subCategoryProduct)
                    .where(
                        subCategoryProduct.productCategoryId.eq(productCategoryJpaEntity.id),
                        subCategoryProduct.deleted.isFalse()
                    )
            ))
            .from(productCategoryJpaEntity)
            .where(productCategoryJpaEntity.shopId.eq(shopId))
            .orderBy(productCategoryJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductOptionGroupManagementResult> findProductOptionGroupsForManagement(Long shopId) {
        Expression<Long> linkedProductCount = JPAExpressions
            .select(subOptionGroupLink.count())
            .from(subOptionGroupLink)
            .where(subOptionGroupLink.optionGroupId.eq(productOptionGroupJpaEntity.id));

        List<Tuple> groups = queryFactory
            .selectDistinct(
                productOptionGroupJpaEntity.id,
                productOptionGroupJpaEntity.name,
                productOptionGroupJpaEntity.description,
                productOptionGroupJpaEntity.required,
                productOptionGroupJpaEntity.multipleSelect,
                productOptionGroupJpaEntity.minSelect,
                productOptionGroupJpaEntity.maxSelect,
                productOptionGroupLinkJpaEntity.sort,
                productOptionGroupJpaEntity.visible,
                productOptionGroupJpaEntity.groupType,
                linkedProductCount
            )
            .from(productOptionGroupJpaEntity)
            .innerJoin(productOptionGroupLinkJpaEntity)
            .on(productOptionGroupLinkJpaEntity.optionGroupId.eq(productOptionGroupJpaEntity.id))
            .innerJoin(productJpaEntity).on(productOptionGroupLinkJpaEntity.productId.eq(productJpaEntity.id))
            .where(productJpaEntity.shopId.eq(shopId), notDeleted())
            .orderBy(productOptionGroupLinkJpaEntity.sort.asc(), productOptionGroupJpaEntity.id.asc())
            .fetch();

        if (groups.isEmpty()) {
            return List.of();
        }

        Map<Long, Tuple> groupById = new LinkedHashMap<>();
        for (Tuple tuple : groups) {
            groupById.putIfAbsent(tuple.get(productOptionGroupJpaEntity.id), tuple);
        }

        List<Long> groupIds = List.copyOf(groupById.keySet());
        Map<Long, List<ProductOptionManagementResult>> optionsByGroupId = findOptionsForManagement(groupIds);

        return groupById.values().stream()
            .map(tuple -> {
                Long groupId = tuple.get(productOptionGroupJpaEntity.id);
                Long linkedCount = tuple.get(linkedProductCount);
                return new ProductOptionGroupManagementResult(
                    groupId,
                    tuple.get(productOptionGroupJpaEntity.name),
                    tuple.get(productOptionGroupJpaEntity.description),
                    Boolean.TRUE.equals(tuple.get(productOptionGroupJpaEntity.required)),
                    Boolean.TRUE.equals(tuple.get(productOptionGroupJpaEntity.multipleSelect)),
                    tuple.get(productOptionGroupJpaEntity.minSelect),
                    tuple.get(productOptionGroupJpaEntity.maxSelect),
                    tuple.get(productOptionGroupLinkJpaEntity.sort),
                    Boolean.TRUE.equals(tuple.get(productOptionGroupJpaEntity.visible)),
                    groupTypeNameOf(tuple.get(productOptionGroupJpaEntity.groupType)),
                    linkedCount != null ? linkedCount : 0L,
                    optionsByGroupId.getOrDefault(groupId, List.of())
                );
            })
            .toList();
    }

    private Map<Long, List<ProductOptionManagementResult>> findOptionsForManagement(List<Long> groupIds) {
        NumberExpression<Long> optionGroupId = productOptionJpaEntity.optionGroupId;
        return queryFactory
            .select(
                optionGroupId,
                productOptionJpaEntity.id,
                productOptionJpaEntity.name,
                productOptionJpaEntity.additionalPrice,
                productOptionJpaEntity.sort,
                productOptionJpaEntity.soldOut,
                productOptionJpaEntity.visible,
                productOptionJpaEntity.cupCount,
                productOptionJpaEntity.personalCupDiscountAmount
            )
            .from(productOptionJpaEntity)
            .where(optionGroupId.in(groupIds))
            .orderBy(productOptionJpaEntity.sort.asc())
            .fetch()
            .stream()
            .filter(tuple -> tuple.get(optionGroupId) != null)
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(optionGroupId)),
                LinkedHashMap::new,
                Collectors.mapping(
                    tuple -> new ProductOptionManagementResult(
                        tuple.get(productOptionJpaEntity.id),
                        tuple.get(productOptionJpaEntity.name),
                        tuple.get(productOptionJpaEntity.additionalPrice),
                        tuple.get(productOptionJpaEntity.sort),
                        Boolean.TRUE.equals(tuple.get(productOptionJpaEntity.soldOut)),
                        Boolean.TRUE.equals(tuple.get(productOptionJpaEntity.visible)),
                        tuple.get(productOptionJpaEntity.cupCount),
                        tuple.get(productOptionJpaEntity.personalCupDiscountAmount)
                    ),
                    Collectors.toList()
                )
            ));
    }

    @Override
    public List<ProductOptionGroupLinkedProductResult> findLinkedProductsByOptionGroupId(Long optionGroupId) {
        return queryFactory
            .select(productJpaEntity.id, productJpaEntity.shopId, productJpaEntity.name)
            .from(productOptionGroupLinkJpaEntity)
            .innerJoin(productJpaEntity).on(productOptionGroupLinkJpaEntity.productId.eq(productJpaEntity.id))
            .where(productOptionGroupLinkJpaEntity.optionGroupId.eq(optionGroupId), notDeleted())
            .orderBy(productOptionGroupLinkJpaEntity.sort.asc(), productJpaEntity.id.asc())
            .fetch()
            .stream()
            .map(tuple -> new ProductOptionGroupLinkedProductResult(
                tuple.get(productJpaEntity.id),
                tuple.get(productJpaEntity.shopId),
                tuple.get(productJpaEntity.name)
            ))
            .toList();
    }

    @Override
    public Map<Long, List<ProductOptionGroupLinkedProductResult>> findLinkedProductsByShop(Long shopId) {
        return queryFactory
            .select(
                productOptionGroupLinkJpaEntity.optionGroupId,
                productJpaEntity.id,
                productJpaEntity.shopId,
                productJpaEntity.name
            )
            .from(productOptionGroupLinkJpaEntity)
            .innerJoin(productJpaEntity).on(productOptionGroupLinkJpaEntity.productId.eq(productJpaEntity.id))
            .where(productJpaEntity.shopId.eq(shopId), notDeleted())
            .orderBy(productOptionGroupLinkJpaEntity.sort.asc(), productJpaEntity.id.asc())
            .fetch()
            .stream()
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(productOptionGroupLinkJpaEntity.optionGroupId)),
                LinkedHashMap::new,
                Collectors.mapping(
                    tuple -> new ProductOptionGroupLinkedProductResult(
                        tuple.get(productJpaEntity.id),
                        tuple.get(productJpaEntity.shopId),
                        tuple.get(productJpaEntity.name)
                    ),
                    Collectors.toList()
                )
            ));
    }

    @Override
    public List<ProductOptionGroupMergeCandidateResult> findOptionGroupMergeCandidates(Long shopId) {
        Query query = entityManager.createNativeQuery(MERGE_CANDIDATE_SQL);
        query.setParameter("shopId", shopId);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
            .map(row -> new ProductOptionGroupMergeCandidateResult(
                toLong(row[0]),
                (String) row[1],
                toInteger(row[2]),
                toInteger(row[3]),
                (String) row[4]
            ))
            .toList();
    }

    @Override
    public Set<String> findOptionGroupMergeExcludedSignatures(Long shopId) {
        return Set.copyOf(queryFactory
            .select(productOptionGroupMergeExclusionJpaEntity.groupSignature)
            .from(productOptionGroupMergeExclusionJpaEntity)
            .where(productOptionGroupMergeExclusionJpaEntity.shopId.eq(shopId))
            .fetch());
    }

    private static String groupTypeNameOf(ProductOptionGroupType groupType) {
        return groupType == null ? ProductOptionGroupType.NORMAL.name() : groupType.name();
    }

    private static Long toLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private static Integer toInteger(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }

    @Override
    public List<ProductAvailabilityItemResult> findProductAvailability(ProductAvailabilitySearchCondition condition) {
        return queryFactory
            .select(
                productCategoryJpaEntity.id,
                productCategoryJpaEntity.name,
                productCategoryJpaEntity.sort,
                productJpaEntity.id,
                productJpaEntity.name,
                productJpaEntity.originalPrice,
                productJpaEntity.discountInfo.discountPrice,
                uploadedFileJpaEntity.filePath,
                productJpaEntity.soldOut,
                productJpaEntity.soldOutUntil,
                productJpaEntity.visible,
                productJpaEntity.representative,
                productShopLinkJpaEntity.sort
            )
            .from(productShopLinkJpaEntity)
            .innerJoin(productJpaEntity).on(productJpaEntity.id.eq(productShopLinkJpaEntity.productId))
            .leftJoin(productCategoryJpaEntity)
            .on(productShopLinkJpaEntity.productCategoryId.eq(productCategoryJpaEntity.id))
            .leftJoin(productImageJpaEntity).on(representativeImageOf(productJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(
                productShopLinkJpaEntity.shopId.eq(condition.shopId()),
                nameContains(condition.keyword()),
                soldOutOrHidden(condition.soldOutOnly(), condition.hiddenOnly()),
                notDeleted()
            )
            .orderBy(productCategoryJpaEntity.sort.asc().nullsLast(), productShopLinkJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(tuple -> new ProductAvailabilityItemResult(
                tuple.get(productCategoryJpaEntity.id),
                tuple.get(productCategoryJpaEntity.name),
                tuple.get(productCategoryJpaEntity.sort),
                tuple.get(productJpaEntity.id),
                tuple.get(productJpaEntity.name),
                tuple.get(productJpaEntity.originalPrice),
                tuple.get(productJpaEntity.discountInfo.discountPrice),
                fileUrlResolver.resolve(tuple.get(uploadedFileJpaEntity.filePath)),
                Boolean.TRUE.equals(tuple.get(productJpaEntity.soldOut)),
                tuple.get(productJpaEntity.soldOutUntil),
                Boolean.TRUE.equals(tuple.get(productJpaEntity.visible)),
                Boolean.TRUE.equals(tuple.get(productJpaEntity.representative)),
                tuple.get(productShopLinkJpaEntity.sort)
            ))
            .toList();
    }

    @Override
    public List<ProductOptionAvailabilityGroupResult> findProductOptionAvailability(
        ProductAvailabilitySearchCondition condition
    ) {
        List<ProductOptionAvailabilityGroupResult> result = new ArrayList<>();
        result.addAll(findNormalOptionGroupsForAvailability(condition));
        result.addAll(findCommonOptionGroupsForAvailability(condition));
        return result;
    }

    private List<ProductOptionAvailabilityGroupResult> findNormalOptionGroupsForAvailability(
        ProductAvailabilitySearchCondition condition
    ) {
        List<Long> groupIds = queryFactory
            .selectDistinct(productOptionGroupJpaEntity.id)
            .from(productOptionGroupJpaEntity)
            .innerJoin(productOptionGroupLinkJpaEntity)
            .on(productOptionGroupLinkJpaEntity.optionGroupId.eq(productOptionGroupJpaEntity.id))
            .innerJoin(productJpaEntity).on(productOptionGroupLinkJpaEntity.productId.eq(productJpaEntity.id))
            .where(
                productJpaEntity.shopId.eq(condition.shopId()),
                notDeleted(),
                normalOptionMatchExists(condition)
            )
            .fetch();

        if (groupIds.isEmpty()) {
            return List.of();
        }

        List<Tuple> groups = queryFactory
            .select(
                productOptionGroupJpaEntity.id,
                productOptionGroupJpaEntity.name,
                productOptionGroupJpaEntity.required,
                productOptionGroupJpaEntity.minSelect,
                productOptionGroupJpaEntity.maxSelect,
                productOptionGroupJpaEntity.sort,
                productJpaEntity.name
            )
            .from(productOptionGroupJpaEntity)
            .innerJoin(productOptionGroupLinkJpaEntity)
            .on(productOptionGroupLinkJpaEntity.optionGroupId.eq(productOptionGroupJpaEntity.id))
            .innerJoin(productJpaEntity).on(productOptionGroupLinkJpaEntity.productId.eq(productJpaEntity.id))
            .where(productOptionGroupJpaEntity.id.in(groupIds), notDeleted())
            .orderBy(productOptionGroupLinkJpaEntity.sort.asc())
            .fetch();

        Map<Long, List<String>> linkedProductNamesByGroupId = new LinkedHashMap<>();
        Map<Long, Tuple> groupById = new LinkedHashMap<>();
        for (Tuple tuple : groups) {
            Long groupId = tuple.get(productOptionGroupJpaEntity.id);
            groupById.putIfAbsent(groupId, tuple);
            linkedProductNamesByGroupId
                .computeIfAbsent(groupId, key -> new ArrayList<>())
                .add(tuple.get(productJpaEntity.name));
        }

        Map<Long, List<ProductOptionAvailabilityItemResult>> optionsByGroupId =
            findNormalOptionsForAvailability(groupIds, condition);

        return groupById.values().stream()
            .map(tuple -> {
                Long groupId = tuple.get(productOptionGroupJpaEntity.id);
                return new ProductOptionAvailabilityGroupResult(
                    groupId,
                    "NORMAL",
                    tuple.get(productOptionGroupJpaEntity.name),
                    Boolean.TRUE.equals(tuple.get(productOptionGroupJpaEntity.required)),
                    tuple.get(productOptionGroupJpaEntity.minSelect),
                    tuple.get(productOptionGroupJpaEntity.maxSelect),
                    linkedProductNamesByGroupId.getOrDefault(groupId, List.of()),
                    tuple.get(productOptionGroupJpaEntity.sort),
                    optionsByGroupId.getOrDefault(groupId, List.of())
                );
            })
            .toList();
    }

    private Map<Long, List<ProductOptionAvailabilityItemResult>> findNormalOptionsForAvailability(
        List<Long> groupIds,
        ProductAvailabilitySearchCondition condition
    ) {
        NumberExpression<Long> optionGroupId = productOptionJpaEntity.optionGroupId;
        return queryFactory
            .select(
                optionGroupId,
                productOptionJpaEntity.id,
                productOptionJpaEntity.name,
                productOptionJpaEntity.additionalPrice,
                productOptionJpaEntity.soldOut,
                productOptionJpaEntity.soldOutUntil,
                productOptionJpaEntity.visible,
                productOptionJpaEntity.sort
            )
            .from(productOptionJpaEntity)
            .where(
                optionGroupId.in(groupIds),
                optionNameContains(condition.keyword()),
                normalOptionSoldOutOrHidden(condition.soldOutOnly(), condition.hiddenOnly())
            )
            .orderBy(productOptionJpaEntity.sort.asc())
            .fetch()
            .stream()
            .filter(tuple -> tuple.get(optionGroupId) != null)
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(optionGroupId)),
                LinkedHashMap::new,
                Collectors.mapping(
                    tuple -> new ProductOptionAvailabilityItemResult(
                        tuple.get(productOptionJpaEntity.id),
                        "NORMAL",
                        tuple.get(productOptionJpaEntity.name),
                        tuple.get(productOptionJpaEntity.additionalPrice),
                        Boolean.TRUE.equals(tuple.get(productOptionJpaEntity.soldOut)),
                        tuple.get(productOptionJpaEntity.soldOutUntil),
                        Boolean.TRUE.equals(tuple.get(productOptionJpaEntity.visible)),
                        tuple.get(productOptionJpaEntity.sort)
                    ),
                    Collectors.toList()
                )
            ));
    }

    private List<ProductOptionAvailabilityGroupResult> findCommonOptionGroupsForAvailability(
        ProductAvailabilitySearchCondition condition
    ) {
        List<Long> groupIds = queryFactory
            .selectDistinct(productCommonOptionGroupJpaEntity.id)
            .from(productCommonOptionGroupJpaEntity)
            .innerJoin(productCommonOptionGroupLinkJpaEntity)
            .on(productCommonOptionGroupLinkJpaEntity.optionGroupId.eq(productCommonOptionGroupJpaEntity.id))
            .innerJoin(productJpaEntity)
            .on(productCommonOptionGroupLinkJpaEntity.productId.eq(productJpaEntity.id))
            .where(
                productJpaEntity.shopId.eq(condition.shopId()),
                notDeleted(),
                commonOptionMatchExists(condition)
            )
            .fetch();

        if (groupIds.isEmpty()) {
            return List.of();
        }

        List<Tuple> groups = queryFactory
            .select(
                productCommonOptionGroupJpaEntity.id,
                productCommonOptionGroupJpaEntity.name,
                productCommonOptionGroupJpaEntity.required,
                productCommonOptionGroupJpaEntity.minSelect,
                productCommonOptionGroupJpaEntity.maxSelect,
                productCommonOptionGroupJpaEntity.sort,
                productJpaEntity.name
            )
            .from(productCommonOptionGroupJpaEntity)
            .innerJoin(productCommonOptionGroupLinkJpaEntity)
            .on(productCommonOptionGroupLinkJpaEntity.optionGroupId.eq(productCommonOptionGroupJpaEntity.id))
            .innerJoin(productJpaEntity)
            .on(productCommonOptionGroupLinkJpaEntity.productId.eq(productJpaEntity.id))
            .where(productCommonOptionGroupJpaEntity.id.in(groupIds), notDeleted())
            .orderBy(productCommonOptionGroupLinkJpaEntity.sort.asc())
            .fetch();

        Map<Long, List<String>> linkedProductNamesByGroupId = new LinkedHashMap<>();
        Map<Long, Tuple> groupById = new LinkedHashMap<>();
        for (Tuple tuple : groups) {
            Long groupId = tuple.get(productCommonOptionGroupJpaEntity.id);
            groupById.putIfAbsent(groupId, tuple);
            linkedProductNamesByGroupId
                .computeIfAbsent(groupId, key -> new ArrayList<>())
                .add(tuple.get(productJpaEntity.name));
        }

        Map<Long, List<ProductOptionAvailabilityItemResult>> optionsByGroupId =
            findCommonOptionsForAvailability(groupIds, condition);

        return groupById.values().stream()
            .map(tuple -> {
                Long groupId = tuple.get(productCommonOptionGroupJpaEntity.id);
                return new ProductOptionAvailabilityGroupResult(
                    groupId,
                    "COMMON",
                    tuple.get(productCommonOptionGroupJpaEntity.name),
                    Boolean.TRUE.equals(tuple.get(productCommonOptionGroupJpaEntity.required)),
                    tuple.get(productCommonOptionGroupJpaEntity.minSelect),
                    tuple.get(productCommonOptionGroupJpaEntity.maxSelect),
                    linkedProductNamesByGroupId.getOrDefault(groupId, List.of()),
                    tuple.get(productCommonOptionGroupJpaEntity.sort),
                    optionsByGroupId.getOrDefault(groupId, List.of())
                );
            })
            .toList();
    }

    private Map<Long, List<ProductOptionAvailabilityItemResult>> findCommonOptionsForAvailability(
        List<Long> groupIds,
        ProductAvailabilitySearchCondition condition
    ) {
        NumberExpression<Long> commonOptionGroupId = productCommonOptionJpaEntity.optionGroupId;
        return queryFactory
            .select(
                commonOptionGroupId,
                productCommonOptionJpaEntity.id,
                productCommonOptionJpaEntity.name,
                productCommonOptionJpaEntity.additionalPrice,
                productCommonOptionJpaEntity.soldOut,
                productCommonOptionJpaEntity.soldOutUntil,
                productCommonOptionJpaEntity.visible,
                productCommonOptionJpaEntity.sort
            )
            .from(productCommonOptionJpaEntity)
            .where(
                commonOptionGroupId.in(groupIds),
                commonOptionNameContainsItem(condition.keyword()),
                commonOptionSoldOutOrHidden(condition.soldOutOnly(), condition.hiddenOnly())
            )
            .orderBy(productCommonOptionJpaEntity.sort.asc())
            .fetch()
            .stream()
            .filter(tuple -> tuple.get(commonOptionGroupId) != null)
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(commonOptionGroupId)),
                LinkedHashMap::new,
                Collectors.mapping(
                    tuple -> new ProductOptionAvailabilityItemResult(
                        tuple.get(productCommonOptionJpaEntity.id),
                        "COMMON",
                        tuple.get(productCommonOptionJpaEntity.name),
                        tuple.get(productCommonOptionJpaEntity.additionalPrice),
                        Boolean.TRUE.equals(tuple.get(productCommonOptionJpaEntity.soldOut)),
                        tuple.get(productCommonOptionJpaEntity.soldOutUntil),
                        Boolean.TRUE.equals(tuple.get(productCommonOptionJpaEntity.visible)),
                        tuple.get(productCommonOptionJpaEntity.sort)
                    ),
                    Collectors.toList()
                )
            ));
    }

    @Override
    public List<ProductImageManagementResult> findProductImagesForManagement(Long productId) {
        return queryFactory
            .select(Projections.constructor(ProductImageManagementResult.class,
                productImageJpaEntity.id,
                productImageFile.filePath,
                productImageJpaEntity.sort,
                productImageJpaEntity.visible
            ))
            .from(productImageJpaEntity)
            .leftJoin(productImageFile).on(productImageFile.id.eq(productImageJpaEntity.imageFileId))
            .where(productImageJpaEntity.productId.eq(productId))
            .orderBy(productImageJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
    }

    @Override
    public boolean existsProductInShop(Long productId, Long shopId) {
        Integer found = queryFactory
            .selectOne()
            .from(productJpaEntity)
            .leftJoin(productShopLinkJpaEntity)
            .on(
                productShopLinkJpaEntity.productId.eq(productJpaEntity.id),
                productShopLinkJpaEntity.shopId.eq(shopId)
            )
            .where(
                productJpaEntity.id.eq(productId),
                notDeleted(),
                productShopLinkJpaEntity.id.isNotNull().or(productJpaEntity.shopId.eq(shopId))
            )
            .fetchFirst();
        return found != null;
    }

    @Override
    public List<ProductImageChangeRequestResult> findImageChangeRequests(Long productId) {
        return imageChangeRequestProjection()
            .where(productImageChangeRequestJpaEntity.productId.eq(productId))
            .orderBy(productImageChangeRequestJpaEntity.id.desc())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
    }

    @Override
    public PageResult<ProductImageChangeRequestResult> findImageChangeRequestPage(
        ApprovalStatus status,
        PageQuery pageQuery
    ) {
        Long total = queryFactory
            .select(productImageChangeRequestJpaEntity.count())
            .from(productImageChangeRequestJpaEntity)
            .where(imageChangeStatusEq(status))
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ProductImageChangeRequestResult> content = imageChangeRequestProjection()
            .where(imageChangeStatusEq(status))
            .orderBy(productImageChangeRequestJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public List<ProductVegetarianRequestResult> findVegetarianRequests(Long productId) {
        return vegetarianRequestProjection()
            .where(productVegetarianRequestJpaEntity.productId.eq(productId))
            .orderBy(productVegetarianRequestJpaEntity.id.desc())
            .fetch();
    }

    @Override
    public PageResult<ProductVegetarianRequestResult> findVegetarianRequestPage(
        ApprovalStatus status,
        PageQuery pageQuery
    ) {
        Long total = queryFactory
            .select(productVegetarianRequestJpaEntity.count())
            .from(productVegetarianRequestJpaEntity)
            .where(vegetarianStatusEq(status))
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ProductVegetarianRequestResult> content = vegetarianRequestProjection()
            .where(vegetarianStatusEq(status))
            .orderBy(productVegetarianRequestJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<ProductRepresentativeRequestResult> findRepresentativeRequestPage(
        ApprovalStatus status,
        PageQuery pageQuery
    ) {
        Long total = queryFactory
            .select(productRepresentativeRequestJpaEntity.count())
            .from(productRepresentativeRequestJpaEntity)
            .where(representativeStatusEq(status))
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ProductRepresentativeRequestResult> content = representativeRequestProjection()
            .where(representativeStatusEq(status))
            .orderBy(productRepresentativeRequestJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public List<PopularProductItemResult> findPopularProducts(Long shopId) {
        LocalDateTime now = nowInServiceZone();

        List<PopularProductItemResult> representatives = popularProductProjection()
            .where(
                productJpaEntity.shopId.eq(shopId),
                productJpaEntity.representative.isTrue(),
                orderableNow(now)
            )
            .orderBy(productJpaEntity.rating.desc(), productJpaEntity.id.asc())
            .limit(POPULAR_PRODUCT_LIMIT)
            .fetch();

        List<PopularProductItemResult> merged = new ArrayList<>(representatives);
        int remaining = POPULAR_PRODUCT_LIMIT - merged.size();
        if (remaining <= 0) {
            return merged.stream().map(this::withResolvedImageUrl).toList();
        }

        Set<Long> filledIds = merged.stream()
            .map(PopularProductItemResult::id)
            .collect(Collectors.toSet());

        List<PopularProductItemResult> popular = popularProductProjection()
            .where(
                productJpaEntity.shopId.eq(shopId),
                orderableNow(now),
                filledIds.isEmpty() ? null : productJpaEntity.id.notIn(filledIds),
                soldQuantityOf(shopId, now).gt(0L)
            )
            .orderBy(soldQuantityOf(shopId, now).desc(), productJpaEntity.id.asc())
            .limit(remaining)
            .fetch();

        merged.addAll(popular);
        return merged.stream().map(this::withResolvedImageUrl).toList();
    }

    @Override
    public Optional<ProductVegetarianSettingResult> findVegetarianSetting(Long productId) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(ProductVegetarianSettingResult.class,
                    productJpaEntity.id,
                    productJpaEntity.shopId,
                    productJpaEntity.vegetarianType
                ))
                .from(productJpaEntity)
                .where(productJpaEntity.id.eq(productId), notDeleted())
                .fetchFirst()
        );
    }

    @Override
    public Optional<ProductExposurePeriodResult> findExposurePeriod(Long productId) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(ProductExposurePeriodResult.class,
                    productJpaEntity.id,
                    productJpaEntity.shopId,
                    productJpaEntity.exposureStartDate,
                    productJpaEntity.exposureEndDate
                ))
                .from(productJpaEntity)
                .where(productJpaEntity.id.eq(productId), notDeleted())
                .fetchFirst()
        );
    }

    private com.querydsl.jpa.JPQLQuery<ProductImageChangeRequestResult> imageChangeRequestProjection() {
        return queryFactory
            .select(Projections.constructor(ProductImageChangeRequestResult.class,
                productImageChangeRequestJpaEntity.id,
                productImageChangeRequestJpaEntity.productId,
                productJpaEntity.shopId,
                productJpaEntity.name,
                imageChangeRequestFile.filePath,
                productImageChangeRequestJpaEntity.status,
                productImageChangeRequestJpaEntity.rejectReason
            ))
            .from(productImageChangeRequestJpaEntity)
            .innerJoin(productJpaEntity).on(productJpaEntity.id.eq(productImageChangeRequestJpaEntity.productId))
            .leftJoin(imageChangeRequestFile)
            .on(imageChangeRequestFile.id.eq(productImageChangeRequestJpaEntity.imageFileId));
    }

    private com.querydsl.jpa.JPQLQuery<ProductVegetarianRequestResult> vegetarianRequestProjection() {
        return queryFactory
            .select(Projections.constructor(ProductVegetarianRequestResult.class,
                productVegetarianRequestJpaEntity.id,
                productVegetarianRequestJpaEntity.productId,
                productJpaEntity.shopId,
                productJpaEntity.name,
                productVegetarianRequestJpaEntity.vegetarianType,
                productVegetarianRequestJpaEntity.ingredients,
                productVegetarianRequestJpaEntity.description,
                productVegetarianRequestJpaEntity.status,
                productVegetarianRequestJpaEntity.rejectReason
            ))
            .from(productVegetarianRequestJpaEntity)
            .innerJoin(productJpaEntity).on(productJpaEntity.id.eq(productVegetarianRequestJpaEntity.productId));
    }

    private BooleanExpression imageChangeStatusEq(ApprovalStatus status) {
        return status != null ? productImageChangeRequestJpaEntity.status.eq(status) : null;
    }

    private BooleanExpression vegetarianStatusEq(ApprovalStatus status) {
        return status != null ? productVegetarianRequestJpaEntity.status.eq(status) : null;
    }

    private BooleanExpression representativeStatusEq(ApprovalStatus status) {
        return status != null ? productRepresentativeRequestJpaEntity.status.eq(status) : null;
    }

    private com.querydsl.jpa.JPQLQuery<ProductRepresentativeRequestResult> representativeRequestProjection() {
        return queryFactory
            .select(Projections.constructor(ProductRepresentativeRequestResult.class,
                productRepresentativeRequestJpaEntity.id,
                productRepresentativeRequestJpaEntity.productId,
                productRepresentativeRequestJpaEntity.shopId,
                shopJpaEntity.name,
                productJpaEntity.name,
                uploadedFileJpaEntity.filePath,
                productRepresentativeRequestJpaEntity.status,
                productRepresentativeRequestJpaEntity.rejectReason
            ))
            .from(productRepresentativeRequestJpaEntity)
            .innerJoin(productJpaEntity)
            .on(productJpaEntity.id.eq(productRepresentativeRequestJpaEntity.productId))
            .leftJoin(shopJpaEntity).on(shopJpaEntity.id.eq(productRepresentativeRequestJpaEntity.shopId))
            .leftJoin(productImageJpaEntity).on(representativeImageOf(productJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id));
    }

    private com.querydsl.jpa.JPQLQuery<PopularProductItemResult> popularProductProjection() {
        return queryFactory
            .select(Projections.constructor(PopularProductItemResult.class,
                productJpaEntity.id,
                productJpaEntity.name,
                uploadedFileJpaEntity.filePath,
                productJpaEntity.originalPrice,
                productJpaEntity.discountInfo.discountPrice,
                productJpaEntity.discountInfo.discountRate,
                productJpaEntity.rating,
                productJpaEntity.reviewCount,
                productJpaEntity.representative,
                productJpaEntity.spiciness,
                soldQuantityOf(productJpaEntity.shopId, nowInServiceZone())
            ))
            .from(productJpaEntity)
            .leftJoin(productImageJpaEntity).on(representativeImageOf(productJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id));
    }

    private NumberExpression<Long> soldQuantityOf(
        com.querydsl.core.types.Expression<Long> shopIdExpression,
        LocalDateTime now
    ) {
        return com.querydsl.core.types.dsl.Expressions.numberTemplate(
            Long.class,
            "{0}",
            JPAExpressions
                .select(orderProductJpaEntity.quantity.sumLong().coalesce(0L))
                .from(orderProductJpaEntity)
                .innerJoin(orderJpaEntity).on(orderJpaEntity.id.eq(orderProductJpaEntity.orderId))
                .where(
                    orderProductJpaEntity.productId.eq(productJpaEntity.id),
                    orderJpaEntity.shopId.eq(shopIdExpression),
                    orderJpaEntity.orderStatus.eq(OrderStatus.COMPLETED),
                    orderJpaEntity.createdAt.goe(now.minusDays(POPULAR_PRODUCT_WINDOW_DAYS))
                )
        ).coalesce(0L);
    }

    private NumberExpression<Long> soldQuantityOf(Long shopId, LocalDateTime now) {
        return soldQuantityOf(com.querydsl.core.types.dsl.Expressions.constant(shopId), now);
    }

    private BooleanExpression orderableNow(LocalDateTime now) {
        return productJpaEntity.visible.isTrue()
            .and(productJpaEntity.soldOut.isFalse())
            .and(notDeleted())
            .and(exposedNow(now));
    }

    private PopularProductItemResult withResolvedImageUrl(PopularProductItemResult row) {
        return new PopularProductItemResult(
            row.id(),
            row.name(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.originalPrice(),
            row.discountPrice(),
            row.discountRate(),
            row.rating(),
            row.reviewCount(),
            row.representative(),
            row.spiciness(),
            row.salesQuantity()
        );
    }

    private ProductRepresentativeRequestResult withResolvedImageUrl(ProductRepresentativeRequestResult row) {
        return new ProductRepresentativeRequestResult(
            row.id(),
            row.productId(),
            row.shopId(),
            row.shopName(),
            row.productName(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.status(),
            row.rejectReason()
        );
    }

    private ProductImageManagementResult withResolvedImageUrl(ProductImageManagementResult row) {
        return new ProductImageManagementResult(
            row.id(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.sort(),
            row.visible()
        );
    }

    private ProductImageChangeRequestResult withResolvedImageUrl(ProductImageChangeRequestResult row) {
        return new ProductImageChangeRequestResult(
            row.id(),
            row.productId(),
            row.shopId(),
            row.productName(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.status(),
            row.rejectReason()
        );
    }

    @Override
    public Optional<ProductBbqSyncTargetResult> findFirstBbqSyncTarget() {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(ProductBbqSyncTargetResult.class,
                productBbqJpaEntity.productId,
                    productBbqJpaEntity.bbqMenuId,
                    productJpaEntity.name
                ))
                .from(productBbqJpaEntity)
                .innerJoin(productJpaEntity).on(productBbqJpaEntity.productId.eq(productJpaEntity.id))
                .where(productBbqJpaEntity.optionsSynced.eq(false), notDeleted())
                .fetchFirst()
        );
    }

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

    private BooleanExpression representativeImageOf(NumberPath<Long> productIdPath) {
        return productImageJpaEntity.productId.eq(productIdPath)
            .and(productImageJpaEntity.visible.eq(true))
            .and(productImageJpaEntity.sort.eq(
                JPAExpressions
                    .select(subProductImage.sort.min())
                    .from(subProductImage)
                    .where(subProductImage.productId.eq(productIdPath)
                        .and(subProductImage.visible.eq(true)))
            ));
    }

    private Map<Long, String> findRepresentativeImagePaths(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        NumberExpression<Long> imageProductId = productImageJpaEntity.productId;
        return queryFactory
            .select(imageProductId, uploadedFileJpaEntity.filePath)
            .from(productImageJpaEntity)
            .innerJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(
                imageProductId.in(productIds),
                productImageJpaEntity.visible.eq(true),
                productImageJpaEntity.sort.eq(
                    JPAExpressions
                        .select(subProductImage.sort.min())
                        .from(subProductImage)
                        .where(subProductImage.productId.eq(imageProductId)
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

    private LocalDateTime nowInServiceZone() {
        return LocalDateTime.now(SERVICE_ZONE);
    }

    private BooleanExpression todayDiscountSearchable(LocalDateTime now) {
        return productJpaEntity.discountInfo.discountPrice.isNotNull()
            .and(productJpaEntity.visible.eq(true))
            .and(notDeleted())
            .and(exposedNow(now));
    }

    private BooleanExpression exposedNow(LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        LocalTime time = now.toLocalTime();

        BooleanExpression startNotAfterToday = productJpaEntity.exposureStartDate.isNull()
            .or(productJpaEntity.exposureStartDate.loe(today));
        BooleanExpression endNotBeforeToday = productJpaEntity.exposureEndDate.isNull()
            .or(productJpaEntity.exposureEndDate.goe(today));
        BooleanExpression withinPeriod = startNotAfterToday.and(endNotBeforeToday);

        BooleanExpression noHourRows = JPAExpressions
            .selectOne()
            .from(subExposureHour)
            .where(subExposureHour.productId.eq(productJpaEntity.id))
            .notExists();

        BooleanExpression todayBranch = dayTypeMatches(today.getDayOfWeek())
            .and(coversTime(time));
        BooleanExpression previousDayBranch =
            dayTypeMatches(today.minusDays(1).getDayOfWeek())
                .and(coversAsOvernightTail(time));

        BooleanExpression withinSomeHour = JPAExpressions
            .selectOne()
            .from(subExposureHour)
            .where(
                subExposureHour.productId.eq(productJpaEntity.id),
                todayBranch.or(previousDayBranch)
            )
            .exists();

        return withinPeriod.and(noHourRows.or(withinSomeHour));
    }

    private BooleanExpression dayTypeMatches(java.time.DayOfWeek dayOfWeek) {
        boolean weekend = dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY;
        return subExposureHour.dayType.eq(DayType.DAILY)
            .or(subExposureHour.dayType.eq(weekend ? DayType.WEEKEND : DayType.WEEKDAY))
            .or(subExposureHour.dayType.eq(DayType.valueOf(dayOfWeek.name())));
    }

    private BooleanExpression coversTime(LocalTime time) {
        BooleanExpression allDay = subExposureHour.startTime.isNull().or(subExposureHour.endTime.isNull());
        BooleanExpression sameDay = subExposureHour.startTime.loe(time)
            .and(subExposureHour.endTime.gt(time))
            .and(subExposureHour.startTime.loe(subExposureHour.endTime));
        BooleanExpression overnightHead =
            subExposureHour.endTime.lt(subExposureHour.startTime).and(subExposureHour.startTime.loe(time));
        return allDay.or(sameDay).or(overnightHead);
    }

    private BooleanExpression coversAsOvernightTail(LocalTime time) {
        return subExposureHour.startTime.isNotNull()
            .and(subExposureHour.endTime.isNotNull())
            .and(subExposureHour.endTime.lt(subExposureHour.startTime))
            .and(subExposureHour.endTime.gt(time));
    }

    private BooleanExpression notDeleted() {
        return productJpaEntity.deleted.isFalse();
    }

    private BooleanExpression shopIdEq(Long shopId) {
        return shopId != null ? productJpaEntity.shopId.eq(shopId) : null;
    }

    private BooleanExpression categoryIdEq(Long productCategoryId) {
        return productCategoryId != null ? productJpaEntity.productCategoryId.eq(productCategoryId) : null;
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

    private BooleanExpression soldOutOrHidden(Boolean soldOutOnly, Boolean hiddenOnly) {
        boolean matchSoldOut = Boolean.TRUE.equals(soldOutOnly);
        boolean matchHidden = Boolean.TRUE.equals(hiddenOnly);

        if (matchSoldOut && matchHidden) {
            return productJpaEntity.soldOut.isTrue().or(productJpaEntity.visible.isFalse());
        }
        if (matchSoldOut) {
            return productJpaEntity.soldOut.isTrue();
        }
        if (matchHidden) {
            return productJpaEntity.visible.isFalse();
        }
        return null;
    }

    private BooleanExpression normalOptionMatchExists(ProductAvailabilitySearchCondition condition) {
        BooleanExpression keywordMatch = optionNameContains(condition.keyword());
        BooleanExpression statusMatch =
            normalOptionSoldOutOrHidden(condition.soldOutOnly(), condition.hiddenOnly());
        if (keywordMatch == null && statusMatch == null) {
            return null;
        }
        return JPAExpressions
            .selectOne()
            .from(productOptionJpaEntity)
            .where(
                productOptionJpaEntity.optionGroupId.eq(productOptionGroupJpaEntity.id),
                keywordMatch,
                statusMatch
            )
            .exists();
    }

    private BooleanExpression commonOptionMatchExists(ProductAvailabilitySearchCondition condition) {
        BooleanExpression keywordMatch = commonOptionNameContainsItem(condition.keyword());
        BooleanExpression statusMatch =
            commonOptionSoldOutOrHidden(condition.soldOutOnly(), condition.hiddenOnly());
        if (keywordMatch == null && statusMatch == null) {
            return null;
        }
        return JPAExpressions
            .selectOne()
            .from(productCommonOptionJpaEntity)
            .where(
                productCommonOptionJpaEntity.optionGroupId.eq(productCommonOptionGroupJpaEntity.id),
                keywordMatch,
                statusMatch
            )
            .exists();
    }

    private BooleanExpression optionNameContains(String keyword) {
        return StringUtils.hasText(keyword)
            ? productOptionJpaEntity.name.containsIgnoreCase(keyword)
            : null;
    }

    private BooleanExpression commonOptionNameContainsItem(String keyword) {
        return StringUtils.hasText(keyword)
            ? productCommonOptionJpaEntity.name.containsIgnoreCase(keyword)
            : null;
    }

    private BooleanExpression normalOptionSoldOutOrHidden(Boolean soldOutOnly, Boolean hiddenOnly) {
        boolean matchSoldOut = Boolean.TRUE.equals(soldOutOnly);
        boolean matchHidden = Boolean.TRUE.equals(hiddenOnly);

        if (matchSoldOut && matchHidden) {
            return productOptionJpaEntity.soldOut.isTrue().or(productOptionJpaEntity.visible.isFalse());
        }
        if (matchSoldOut) {
            return productOptionJpaEntity.soldOut.isTrue();
        }
        if (matchHidden) {
            return productOptionJpaEntity.visible.isFalse();
        }
        return null;
    }

    private BooleanExpression commonOptionSoldOutOrHidden(Boolean soldOutOnly, Boolean hiddenOnly) {
        boolean matchSoldOut = Boolean.TRUE.equals(soldOutOnly);
        boolean matchHidden = Boolean.TRUE.equals(hiddenOnly);

        if (matchSoldOut && matchHidden) {
            return productCommonOptionJpaEntity.soldOut.isTrue()
                .or(productCommonOptionJpaEntity.visible.isFalse());
        }
        if (matchSoldOut) {
            return productCommonOptionJpaEntity.soldOut.isTrue();
        }
        if (matchHidden) {
            return productCommonOptionJpaEntity.visible.isFalse();
        }
        return null;
    }

    private record BatchOptionInfo(
        Long groupId,
        String name,
        Integer additionalPrice,
        boolean common,
        Integer cupCount,
        Integer personalCupDiscountAmount
    ) {
        Long groupKey() {
            return groupKey(groupId, common);
        }

        static Long groupKey(Long groupId, boolean common) {
            return common ? -groupId : groupId;
        }
    }
}
