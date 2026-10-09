package com.tastyhouse.infrastructure.jpa.product.query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.application.product.port.out.BatchOptionResult;
import com.tastyhouse.application.product.port.out.OptionGroupResult;
import com.tastyhouse.application.product.port.out.OptionResult;
import com.tastyhouse.application.product.port.out.PopularProductItemResult;
import com.tastyhouse.application.product.port.out.ProductAvailabilityItemResult;
import com.tastyhouse.application.product.port.out.ProductAvailabilitySearchCondition;
import com.tastyhouse.application.product.port.out.ProductBatchItem;
import com.tastyhouse.application.product.port.out.ProductBatchResult;
import com.tastyhouse.application.product.port.out.ProductBbqSyncQueryPort;
import com.tastyhouse.application.product.port.out.ProductBbqSyncTargetResult;
import com.tastyhouse.application.product.port.out.ProductCategoryManagementResult;
import com.tastyhouse.application.product.port.out.ProductCategoryResult;
import com.tastyhouse.application.product.port.out.ProductDetailResult;
import com.tastyhouse.application.product.port.out.ProductExposureHourResult;
import com.tastyhouse.application.product.port.out.ProductExposurePeriodResult;
import com.tastyhouse.application.product.port.out.ProductExposureWindow;
import com.tastyhouse.application.product.port.out.ProductImageChangeRequestResult;
import com.tastyhouse.application.product.port.out.ProductImageManagementResult;
import com.tastyhouse.application.product.port.out.ProductListItemResult;
import com.tastyhouse.application.product.port.out.ProductManagementDetailResult;
import com.tastyhouse.application.product.port.out.ProductManagementQueryPort;
import com.tastyhouse.application.product.port.out.ProductNutritionResult;
import com.tastyhouse.application.product.port.out.ProductOptionAvailabilityGroupResult;
import com.tastyhouse.application.product.port.out.ProductOptionAvailabilityItemResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupLinkedProductResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupManagementResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupMergeCandidateResult;
import com.tastyhouse.application.product.port.out.ProductOptionManagementResult;
import com.tastyhouse.application.product.port.out.ProductOptionsResult;
import com.tastyhouse.application.product.port.out.ProductOwnerPriceView;
import com.tastyhouse.application.product.port.out.ProductOwnerQueryPort;
import com.tastyhouse.application.product.port.out.ProductPriceResult;
import com.tastyhouse.application.product.port.out.ProductQueryPort;
import com.tastyhouse.application.product.port.out.ProductRepresentativeRequestResult;
import com.tastyhouse.application.product.port.out.ProductSearchCondition;
import com.tastyhouse.application.product.port.out.ProductVegetarianRequestResult;
import com.tastyhouse.application.product.port.out.ProductVegetarianSettingResult;
import com.tastyhouse.application.product.port.out.SearchProductItemResult;
import com.tastyhouse.application.product.port.out.ShopProductItemResult;
import com.tastyhouse.application.product.port.out.TodayDiscountProductResult;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.infrastructure.jpa.file.query.FileUrlResolver;
import com.tastyhouse.infrastructure.jpa.shared.query.IdStringRow;

import static com.tastyhouse.infrastructure.jpa.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.jpa.order.persistence.QOrderJpaEntity.orderJpaEntity;
import static com.tastyhouse.infrastructure.jpa.order.persistence.QOrderProductJpaEntity.orderProductJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductAllergenJpaEntity.productAllergenJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductBbqJpaEntity.productBbqJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductCategoryJpaEntity.productCategoryJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductCommonOptionGroupJpaEntity.productCommonOptionGroupJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductCommonOptionGroupLinkJpaEntity.productCommonOptionGroupLinkJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductCommonOptionJpaEntity.productCommonOptionJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductExposureHourJpaEntity.productExposureHourJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductImageChangeRequestJpaEntity.productImageChangeRequestJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductImageJpaEntity.productImageJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductJpaEntity.productJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductNutritionJpaEntity.productNutritionJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductOptionGroupJpaEntity.productOptionGroupJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductOptionGroupLinkJpaEntity.productOptionGroupLinkJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductOptionGroupMergeExclusionJpaEntity.productOptionGroupMergeExclusionJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductOptionJpaEntity.productOptionJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductPriceJpaEntity.productPriceJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductRepresentativeRequestJpaEntity.productRepresentativeRequestJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductShopLinkJpaEntity.productShopLinkJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductVegetarianRequestJpaEntity.productVegetarianRequestJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopJpaEntity.shopJpaEntity;

@Repository
class ProductQueryAdapter implements ProductQueryPort, ProductBbqSyncQueryPort, ProductManagementQueryPort, ProductOwnerQueryPort {

    private static final com.tastyhouse.infrastructure.jpa.product.persistence.QProductImageJpaEntity subProductImage =
        new com.tastyhouse.infrastructure.jpa.product.persistence.QProductImageJpaEntity("subProductImage");

    private static final com.tastyhouse.infrastructure.jpa.file.persistence.QUploadedFileJpaEntity
        imageChangeRequestFile =
        new com.tastyhouse.infrastructure.jpa.file.persistence.QUploadedFileJpaEntity("imageChangeRequestFile");

    private static final com.tastyhouse.infrastructure.jpa.file.persistence.QUploadedFileJpaEntity
        productImageFile =
        new com.tastyhouse.infrastructure.jpa.file.persistence.QUploadedFileJpaEntity("productImageFile");

    private static final com.tastyhouse.infrastructure.jpa.product.persistence.QProductExposureHourJpaEntity
        subExposureHour =
        new com.tastyhouse.infrastructure.jpa.product.persistence.QProductExposureHourJpaEntity("subExposureHour");

    private static final com.tastyhouse.infrastructure.jpa.product.persistence.QProductJpaEntity subCategoryProduct =
        new com.tastyhouse.infrastructure.jpa.product.persistence.QProductJpaEntity("subCategoryProduct");

    private static final com.tastyhouse.infrastructure.jpa.product.persistence.QProductOptionGroupLinkJpaEntity
        subOptionGroupLink =
        new com.tastyhouse.infrastructure.jpa.product.persistence.QProductOptionGroupLinkJpaEntity("subOptionGroupLink");

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

    public ProductQueryAdapter(
        JPAQueryFactory queryFactory,
        FileUrlResolver fileUrlResolver,
        EntityManager entityManager
    ) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
        this.entityManager = entityManager;
    }

    @Override
    public PageResult<TodayDiscountProductResult> findTodayDiscountProducts(
        ProductExposureWindow window,
        PageQuery pageQuery
    ) {
        JPAQuery<TodayDiscountProductResult> query = queryFactory
            .select(Projections.constructor(TodayDiscountProductResult.class,
                productJpaEntity.id,
                shopJpaEntity.name,
                productJpaEntity.name,
                fileUrlResolver.urlOf(uploadedFileJpaEntity.filePath),
                productJpaEntity.originalPrice,
                productJpaEntity.discountInfo.discountPrice,
                productJpaEntity.discountInfo.discountRate
            ))
            .from(productJpaEntity)
            .innerJoin(shopJpaEntity).on(productJpaEntity.shopId.eq(shopJpaEntity.id))
            .leftJoin(productImageJpaEntity).on(representativeImageOf(productJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(todayDiscountSearchable(window))
            .orderBy(productJpaEntity.discountInfo.discountRate.desc());

        long total = countTodayDiscountProducts(window);

        List<TodayDiscountProductResult> products = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(products, total, pageQuery.page(), pageQuery.size());
    }

    private long countTodayDiscountProducts(ProductExposureWindow window) {
        Long total = queryFactory
            .select(productJpaEntity.count())
            .from(productJpaEntity)
            .innerJoin(shopJpaEntity).on(productJpaEntity.shopId.eq(shopJpaEntity.id))
            .where(todayDiscountSearchable(window))
            .fetchOne();

        return total == null ? 0L : total;
    }

    @Override
    public PageResult<SearchProductItemResult> searchByKeyword(
        String keyword,
        ProductExposureWindow window,
        PageQuery pageQuery
    ) {
        BooleanExpression searchable = productJpaEntity.name.containsIgnoreCase(keyword)
            .and(productJpaEntity.visible.eq(true))
            .and(notDeleted())
            .and(productJpaEntity.soldOut.eq(false))
            .and(shopJpaEntity.permanentlyClosed.eq(false))
            .and(shopJpaEntity.hidden.eq(false))
            .and(exposedNow(window));

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
                fileUrlResolver.urlOf(uploadedFileJpaEntity.filePath),
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
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public ProductOptionsResult findProductOptions(Long productId, String commonOptionGroupType) {
        List<OptionGroupResult> result = new ArrayList<>();
        result.addAll(findNormalOptionGroups(productId));
        result.addAll(findCommonOptionGroups(productId, commonOptionGroupType));
        return new ProductOptionsResult(result);
    }

    private List<OptionGroupResult> findNormalOptionGroups(Long productId) {
        List<ProductOptionGroupRow> groups = queryFactory
            .select(Projections.constructor(ProductOptionGroupRow.class,
                productOptionGroupJpaEntity.id,
                productOptionGroupJpaEntity.name,
                productOptionGroupJpaEntity.description,
                productOptionGroupJpaEntity.required,
                productOptionGroupJpaEntity.multipleSelect,
                productOptionGroupJpaEntity.minSelect,
                productOptionGroupJpaEntity.maxSelect,
                productOptionGroupJpaEntity.groupType
            ))
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

        List<Long> groupIds = groups.stream().map(ProductOptionGroupRow::id).toList();
        NumberExpression<Long> optionGroupId = productOptionJpaEntity.optionGroupId;
        Map<Long, List<OptionResult>> optionsByGroupId = queryFactory
            .select(Projections.constructor(ProductOptionRow.class,
                optionGroupId,
                productOptionJpaEntity.id,
                productOptionJpaEntity.name,
                productOptionJpaEntity.additionalPrice,
                productOptionJpaEntity.soldOut,
                productOptionJpaEntity.cupCount,
                productOptionJpaEntity.personalCupDiscountAmount
            ))
            .from(productOptionJpaEntity)
            .where(optionGroupId.in(groupIds), productOptionJpaEntity.visible.eq(true))
            .orderBy(productOptionJpaEntity.sort.asc())
            .fetch()
            .stream()
            .filter(row -> row.optionGroupId() != null)
            .collect(Collectors.groupingBy(
                row -> Objects.requireNonNull(row.optionGroupId()),
                LinkedHashMap::new,
                Collectors.mapping(
                    row -> new OptionResult(
                        row.id(),
                        row.name(),
                        row.additionalPrice(),
                        Boolean.TRUE.equals(row.soldOut()),
                        row.cupCount(),
                        null,
                        row.personalCupDiscountAmount()
                    ),
                    Collectors.toList()
                )
            ));

        return groups.stream()
            .map(row -> new OptionGroupResult(
                row.id(),
                row.name(),
                row.description(),
                Boolean.TRUE.equals(row.required()),
                Boolean.TRUE.equals(row.multipleSelect()),
                row.minSelect(),
                row.maxSelect(),
                false,
                row.groupType(),
                optionsByGroupId.getOrDefault(row.id(), Collections.emptyList())
            ))
            .toList();
    }

    private List<OptionGroupResult> findCommonOptionGroups(Long productId, String commonOptionGroupType) {
        List<ProductCommonOptionGroupRow> groups = queryFactory
            .select(Projections.constructor(ProductCommonOptionGroupRow.class,
                productCommonOptionGroupJpaEntity.id,
                productCommonOptionGroupJpaEntity.name,
                productCommonOptionGroupJpaEntity.description,
                productCommonOptionGroupJpaEntity.required,
                productCommonOptionGroupJpaEntity.multipleSelect,
                productCommonOptionGroupJpaEntity.minSelect,
                productCommonOptionGroupJpaEntity.maxSelect
            ))
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

        List<Long> groupIds = groups.stream().map(ProductCommonOptionGroupRow::id).toList();
        NumberExpression<Long> commonOptionGroupId = productCommonOptionJpaEntity.optionGroupId;
        Map<Long, List<OptionResult>> optionsByGroupId = queryFactory
            .select(Projections.constructor(ProductCommonOptionRow.class,
                commonOptionGroupId,
                productCommonOptionJpaEntity.id,
                productCommonOptionJpaEntity.name,
                productCommonOptionJpaEntity.additionalPrice,
                productCommonOptionJpaEntity.soldOut
            ))
            .from(productCommonOptionJpaEntity)
            .where(
                commonOptionGroupId.in(groupIds),
                productCommonOptionJpaEntity.visible.eq(true)
            )
            .orderBy(productCommonOptionJpaEntity.sort.asc())
            .fetch()
            .stream()
            .filter(row -> row.commonOptionGroupId() != null)
            .collect(Collectors.groupingBy(
                row -> Objects.requireNonNull(row.commonOptionGroupId()),
                LinkedHashMap::new,
                Collectors.mapping(
                    row -> new OptionResult(
                        row.id(),
                        row.name(),
                        row.additionalPrice(),
                        Boolean.TRUE.equals(row.soldOut()),
                        null,
                        0,
                        null
                    ),
                    Collectors.toList()
                )
            ));

        return groups.stream()
            .map(row -> new OptionGroupResult(
                row.id(),
                row.name(),
                row.description(),
                Boolean.TRUE.equals(row.required()),
                Boolean.TRUE.equals(row.multipleSelect()),
                row.minSelect(),
                row.maxSelect(),
                true,
                commonOptionGroupType,
                optionsByGroupId.getOrDefault(row.id(), Collections.emptyList())
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

        Map<Long, ProductSummaryRow> productById = findActiveProductSummaries(productIds);
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
                    null,
                    optionInfo.personalCupDiscountAmount()
                ));
            }
        }

        return optionsByProductId.entrySet().stream()
            .map(entry -> {
                ProductSummaryRow product = productById.get(entry.getKey());
                if (product == null) {
                    return new ProductBatchResult(entry.getKey(), false, null, null, null, null, null, List.of());
                }
                return new ProductBatchResult(
                    product.id(),
                    true,
                    product.name(),
                    fileUrlResolver.resolve(imagePathByProductId.get(entry.getKey())),
                    product.originalPrice(),
                    product.discountPrice(),
                    product.discountRate(),
                    entry.getValue()
                );
            })
            .toList();
    }

    private Map<Long, ProductSummaryRow> findActiveProductSummaries(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        return queryFactory
            .select(Projections.constructor(ProductSummaryRow.class,
                productJpaEntity.id,
                productJpaEntity.name,
                productJpaEntity.originalPrice,
                productJpaEntity.discountInfo.discountPrice,
                productJpaEntity.discountInfo.discountRate
            ))
            .from(productJpaEntity)
            .where(productJpaEntity.id.in(productIds), productJpaEntity.visible.eq(true), notDeleted())
            .fetch()
            .stream()
            .filter(row -> row.id() != null)
            .collect(Collectors.toMap(
                ProductSummaryRow::id,
                row -> row,
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
            .select(Projections.constructor(ProductBatchOptionRow.class,
                productOptionJpaEntity.id,
                optionGroupId,
                productOptionJpaEntity.name,
                productOptionJpaEntity.additionalPrice,
                productOptionJpaEntity.cupCount,
                productOptionJpaEntity.personalCupDiscountAmount
            ))
            .from(productOptionJpaEntity)
            .where(productOptionJpaEntity.id.in(optionIds), productOptionJpaEntity.visible.eq(true))
            .fetch()
            .forEach(row -> optionById.put(
                row.id(),
                new BatchOptionInfo(
                    row.optionGroupId(),
                    row.name(),
                    row.additionalPrice(),
                    false,
                    row.cupCount(),
                    row.personalCupDiscountAmount()
                )
            ));

        queryFactory
            .select(Projections.constructor(ProductBatchCommonOptionRow.class,
                productCommonOptionJpaEntity.id,
                commonOptionGroupId,
                productCommonOptionJpaEntity.name,
                productCommonOptionJpaEntity.additionalPrice
            ))
            .from(productCommonOptionJpaEntity)
            .where(productCommonOptionJpaEntity.id.in(optionIds), productCommonOptionJpaEntity.visible.eq(true))
            .fetch()
            .forEach(row -> optionById.putIfAbsent(
                row.id(),
                new BatchOptionInfo(
                    row.commonOptionGroupId(),
                    row.name(),
                    row.additionalPrice(),
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
                .select(Projections.constructor(ProductOptionGroupLinkRow.class,
                    productOptionGroupLinkJpaEntity.optionGroupId,
                    productOptionGroupLinkJpaEntity.productId
                ))
                .from(productOptionGroupLinkJpaEntity)
                .where(productOptionGroupLinkJpaEntity.optionGroupId.in(normalGroupIds))
                .fetch()
                .forEach(row -> linkedProductIdsByGroupKey
                    .computeIfAbsent(
                        BatchOptionInfo.groupKey(row.optionGroupId(), false),
                        key -> new HashSet<>())
                    .add(row.productId()));
        }

        if (!commonGroupIds.isEmpty()) {
            queryFactory
                .select(Projections.constructor(ProductOptionGroupLinkRow.class,
                    productCommonOptionGroupLinkJpaEntity.optionGroupId,
                    productCommonOptionGroupLinkJpaEntity.productId
                ))
                .from(productCommonOptionGroupLinkJpaEntity)
                .where(productCommonOptionGroupLinkJpaEntity.optionGroupId.in(commonGroupIds))
                .fetch()
                .forEach(row -> linkedProductIdsByGroupKey
                    .computeIfAbsent(
                        BatchOptionInfo.groupKey(row.optionGroupId(), true),
                        key -> new HashSet<>())
                    .add(row.productId()));
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
    public List<ShopProductItemResult> findShopProducts(Long shopId, ProductExposureWindow window) {
        return queryFactory
            .select(Projections.constructor(ShopProductItemResult.class,
                productJpaEntity.id,
                productShopLinkJpaEntity.productCategoryId,
                productJpaEntity.name,
                fileUrlResolver.urlOf(uploadedFileJpaEntity.filePath),
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
                exposedNow(window))
            .orderBy(
                productJpaEntity.representative.desc(),
                productJpaEntity.rating.desc(),
                productJpaEntity.id.asc()
            )
            .fetch();
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
        ProductDetailResult result = queryFactory
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
            .fetchOne();
        return Optional.ofNullable(result);
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
        ProductManagementDetailResult result = queryFactory
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
                fileUrlResolver.urlOf(uploadedFileJpaEntity.filePath),
                productJpaEntity.vegetarianType.stringValue(),
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
            .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<ProductNutritionResult> findNutrition(Long productId) {
        ProductNutritionResult result = queryFactory
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
            .fetchFirst();
        return Optional.ofNullable(result);
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

        List<ProductOptionGroupManagementRow> groups = queryFactory
            .selectDistinct(Projections.constructor(ProductOptionGroupManagementRow.class,
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
            ))
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

        Map<Long, ProductOptionGroupManagementRow> groupById = new LinkedHashMap<>();
        for (ProductOptionGroupManagementRow row : groups) {
            groupById.putIfAbsent(row.id(), row);
        }

        List<Long> groupIds = List.copyOf(groupById.keySet());
        Map<Long, List<ProductOptionManagementResult>> optionsByGroupId = findOptionsForManagement(groupIds);

        return groupById.values().stream()
            .map(row -> {
                Long groupId = row.id();
                Long linkedCount = row.linkedProductCount();
                return new ProductOptionGroupManagementResult(
                    groupId,
                    row.name(),
                    row.description(),
                    Boolean.TRUE.equals(row.required()),
                    Boolean.TRUE.equals(row.multipleSelect()),
                    row.minSelect(),
                    row.maxSelect(),
                    row.sort(),
                    Boolean.TRUE.equals(row.visible()),
                    row.groupType(),
                    linkedCount != null ? linkedCount : 0L,
                    optionsByGroupId.getOrDefault(groupId, List.of())
                );
            })
            .toList();
    }

    private Map<Long, List<ProductOptionManagementResult>> findOptionsForManagement(List<Long> groupIds) {
        NumberExpression<Long> optionGroupId = productOptionJpaEntity.optionGroupId;
        return queryFactory
            .select(Projections.constructor(ProductOptionManagementRow.class,
                optionGroupId,
                productOptionJpaEntity.id,
                productOptionJpaEntity.name,
                productOptionJpaEntity.additionalPrice,
                productOptionJpaEntity.sort,
                productOptionJpaEntity.soldOut,
                productOptionJpaEntity.visible,
                productOptionJpaEntity.cupCount,
                productOptionJpaEntity.personalCupDiscountAmount
            ))
            .from(productOptionJpaEntity)
            .where(optionGroupId.in(groupIds))
            .orderBy(productOptionJpaEntity.sort.asc())
            .fetch()
            .stream()
            .filter(row -> row.optionGroupId() != null)
            .collect(Collectors.groupingBy(
                row -> Objects.requireNonNull(row.optionGroupId()),
                LinkedHashMap::new,
                Collectors.mapping(
                    row -> new ProductOptionManagementResult(
                        row.id(),
                        row.name(),
                        row.additionalPrice(),
                        row.sort(),
                        Boolean.TRUE.equals(row.soldOut()),
                        Boolean.TRUE.equals(row.visible()),
                        row.cupCount(),
                        row.personalCupDiscountAmount()
                    ),
                    Collectors.toList()
                )
            ));
    }

    @Override
    public List<ProductOptionGroupLinkedProductResult> findLinkedProductsByOptionGroupId(Long optionGroupId) {
        return queryFactory
            .select(Projections.constructor(ProductOptionGroupLinkedProductResult.class,
                productJpaEntity.id,
                productJpaEntity.shopId,
                productJpaEntity.name
            ))
            .from(productOptionGroupLinkJpaEntity)
            .innerJoin(productJpaEntity).on(productOptionGroupLinkJpaEntity.productId.eq(productJpaEntity.id))
            .where(productOptionGroupLinkJpaEntity.optionGroupId.eq(optionGroupId), notDeleted())
            .orderBy(productOptionGroupLinkJpaEntity.sort.asc(), productJpaEntity.id.asc())
            .fetch();
    }

    @Override
    public Map<Long, List<ProductOptionGroupLinkedProductResult>> findLinkedProductsByShop(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ProductLinkedProductRow.class,
                productOptionGroupLinkJpaEntity.optionGroupId,
                productJpaEntity.id,
                productJpaEntity.shopId,
                productJpaEntity.name
            ))
            .from(productOptionGroupLinkJpaEntity)
            .innerJoin(productJpaEntity).on(productOptionGroupLinkJpaEntity.productId.eq(productJpaEntity.id))
            .where(productJpaEntity.shopId.eq(shopId), notDeleted())
            .orderBy(productOptionGroupLinkJpaEntity.sort.asc(), productJpaEntity.id.asc())
            .fetch()
            .stream()
            .collect(Collectors.groupingBy(
                row -> Objects.requireNonNull(row.optionGroupId()),
                LinkedHashMap::new,
                Collectors.mapping(
                    row -> new ProductOptionGroupLinkedProductResult(
                        row.id(),
                        row.shopId(),
                        row.name()
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

    private static Long toLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private static Integer toInteger(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }

    @Override
    public List<ProductAvailabilityItemResult> findProductAvailability(ProductAvailabilitySearchCondition condition) {
        return queryFactory
            .select(Projections.constructor(ProductAvailabilityRow.class,
                productCategoryJpaEntity.id,
                productCategoryJpaEntity.name,
                productCategoryJpaEntity.sort,
                productJpaEntity.id,
                productJpaEntity.name,
                productJpaEntity.originalPrice,
                productJpaEntity.discountInfo.discountPrice,
                fileUrlResolver.urlOf(uploadedFileJpaEntity.filePath),
                productJpaEntity.soldOut,
                productJpaEntity.soldOutUntil,
                productJpaEntity.visible,
                productJpaEntity.representative,
                productShopLinkJpaEntity.sort
            ))
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
            .map(row -> new ProductAvailabilityItemResult(
                row.categoryId(),
                row.categoryName(),
                row.categorySort(),
                row.productId(),
                row.productName(),
                row.originalPrice(),
                row.discountPrice(),
                row.imageUrl(),
                Boolean.TRUE.equals(row.soldOut()),
                row.soldOutUntil(),
                Boolean.TRUE.equals(row.visible()),
                Boolean.TRUE.equals(row.representative()),
                row.linkSort()
            ))
            .toList();
    }

    @Override
    public List<ProductOptionAvailabilityGroupResult> findProductOptionAvailability(
        ProductAvailabilitySearchCondition condition,
        String normalOptionType,
        String commonOptionType
    ) {
        List<ProductOptionAvailabilityGroupResult> result = new ArrayList<>();
        result.addAll(findNormalOptionGroupsForAvailability(condition, normalOptionType));
        result.addAll(findCommonOptionGroupsForAvailability(condition, commonOptionType));
        return result;
    }

    private List<ProductOptionAvailabilityGroupResult> findNormalOptionGroupsForAvailability(
        ProductAvailabilitySearchCondition condition,
        String normalOptionType
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

        List<ProductOptionAvailabilityGroupRow> groups = queryFactory
            .select(Projections.constructor(ProductOptionAvailabilityGroupRow.class,
                productOptionGroupJpaEntity.id,
                productOptionGroupJpaEntity.name,
                productOptionGroupJpaEntity.required,
                productOptionGroupJpaEntity.minSelect,
                productOptionGroupJpaEntity.maxSelect,
                productOptionGroupJpaEntity.sort,
                productJpaEntity.name
            ))
            .from(productOptionGroupJpaEntity)
            .innerJoin(productOptionGroupLinkJpaEntity)
            .on(productOptionGroupLinkJpaEntity.optionGroupId.eq(productOptionGroupJpaEntity.id))
            .innerJoin(productJpaEntity).on(productOptionGroupLinkJpaEntity.productId.eq(productJpaEntity.id))
            .where(productOptionGroupJpaEntity.id.in(groupIds), notDeleted())
            .orderBy(productOptionGroupLinkJpaEntity.sort.asc())
            .fetch();

        Map<Long, List<String>> linkedProductNamesByGroupId = new LinkedHashMap<>();
        Map<Long, ProductOptionAvailabilityGroupRow> groupById = new LinkedHashMap<>();
        for (ProductOptionAvailabilityGroupRow row : groups) {
            Long groupId = row.id();
            groupById.putIfAbsent(groupId, row);
            linkedProductNamesByGroupId
                .computeIfAbsent(groupId, key -> new ArrayList<>())
                .add(row.productName());
        }

        Map<Long, List<ProductOptionAvailabilityItemResult>> optionsByGroupId =
            findNormalOptionsForAvailability(groupIds, condition, normalOptionType);

        return groupById.values().stream()
            .map(row -> {
                Long groupId = row.id();
                return new ProductOptionAvailabilityGroupResult(
                    groupId,
                    normalOptionType,
                    row.name(),
                    Boolean.TRUE.equals(row.required()),
                    row.minSelect(),
                    row.maxSelect(),
                    linkedProductNamesByGroupId.getOrDefault(groupId, List.of()),
                    row.sort(),
                    optionsByGroupId.getOrDefault(groupId, List.of())
                );
            })
            .toList();
    }

    private Map<Long, List<ProductOptionAvailabilityItemResult>> findNormalOptionsForAvailability(
        List<Long> groupIds,
        ProductAvailabilitySearchCondition condition,
        String normalOptionType
    ) {
        NumberExpression<Long> optionGroupId = productOptionJpaEntity.optionGroupId;
        return queryFactory
            .select(Projections.constructor(ProductOptionAvailabilityRow.class,
                optionGroupId,
                productOptionJpaEntity.id,
                productOptionJpaEntity.name,
                productOptionJpaEntity.additionalPrice,
                productOptionJpaEntity.soldOut,
                productOptionJpaEntity.soldOutUntil,
                productOptionJpaEntity.visible,
                productOptionJpaEntity.sort
            ))
            .from(productOptionJpaEntity)
            .where(
                optionGroupId.in(groupIds),
                optionNameContains(condition.keyword()),
                normalOptionSoldOutOrHidden(condition.soldOutOnly(), condition.hiddenOnly())
            )
            .orderBy(productOptionJpaEntity.sort.asc())
            .fetch()
            .stream()
            .filter(row -> row.optionGroupId() != null)
            .collect(Collectors.groupingBy(
                row -> Objects.requireNonNull(row.optionGroupId()),
                LinkedHashMap::new,
                Collectors.mapping(
                    row -> new ProductOptionAvailabilityItemResult(
                        row.id(),
                        normalOptionType,
                        row.name(),
                        row.additionalPrice(),
                        Boolean.TRUE.equals(row.soldOut()),
                        row.soldOutUntil(),
                        Boolean.TRUE.equals(row.visible()),
                        row.sort()
                    ),
                    Collectors.toList()
                )
            ));
    }

    private List<ProductOptionAvailabilityGroupResult> findCommonOptionGroupsForAvailability(
        ProductAvailabilitySearchCondition condition,
        String commonOptionType
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

        List<ProductOptionAvailabilityGroupRow> groups = queryFactory
            .select(Projections.constructor(ProductOptionAvailabilityGroupRow.class,
                productCommonOptionGroupJpaEntity.id,
                productCommonOptionGroupJpaEntity.name,
                productCommonOptionGroupJpaEntity.required,
                productCommonOptionGroupJpaEntity.minSelect,
                productCommonOptionGroupJpaEntity.maxSelect,
                productCommonOptionGroupJpaEntity.sort,
                productJpaEntity.name
            ))
            .from(productCommonOptionGroupJpaEntity)
            .innerJoin(productCommonOptionGroupLinkJpaEntity)
            .on(productCommonOptionGroupLinkJpaEntity.optionGroupId.eq(productCommonOptionGroupJpaEntity.id))
            .innerJoin(productJpaEntity)
            .on(productCommonOptionGroupLinkJpaEntity.productId.eq(productJpaEntity.id))
            .where(productCommonOptionGroupJpaEntity.id.in(groupIds), notDeleted())
            .orderBy(productCommonOptionGroupLinkJpaEntity.sort.asc())
            .fetch();

        Map<Long, List<String>> linkedProductNamesByGroupId = new LinkedHashMap<>();
        Map<Long, ProductOptionAvailabilityGroupRow> groupById = new LinkedHashMap<>();
        for (ProductOptionAvailabilityGroupRow row : groups) {
            Long groupId = row.id();
            groupById.putIfAbsent(groupId, row);
            linkedProductNamesByGroupId
                .computeIfAbsent(groupId, key -> new ArrayList<>())
                .add(row.productName());
        }

        Map<Long, List<ProductOptionAvailabilityItemResult>> optionsByGroupId =
            findCommonOptionsForAvailability(groupIds, condition, commonOptionType);

        return groupById.values().stream()
            .map(row -> {
                Long groupId = row.id();
                return new ProductOptionAvailabilityGroupResult(
                    groupId,
                    commonOptionType,
                    row.name(),
                    Boolean.TRUE.equals(row.required()),
                    row.minSelect(),
                    row.maxSelect(),
                    linkedProductNamesByGroupId.getOrDefault(groupId, List.of()),
                    row.sort(),
                    optionsByGroupId.getOrDefault(groupId, List.of())
                );
            })
            .toList();
    }

    private Map<Long, List<ProductOptionAvailabilityItemResult>> findCommonOptionsForAvailability(
        List<Long> groupIds,
        ProductAvailabilitySearchCondition condition,
        String commonOptionType
    ) {
        NumberExpression<Long> commonOptionGroupId = productCommonOptionJpaEntity.optionGroupId;
        return queryFactory
            .select(Projections.constructor(ProductOptionAvailabilityRow.class,
                commonOptionGroupId,
                productCommonOptionJpaEntity.id,
                productCommonOptionJpaEntity.name,
                productCommonOptionJpaEntity.additionalPrice,
                productCommonOptionJpaEntity.soldOut,
                productCommonOptionJpaEntity.soldOutUntil,
                productCommonOptionJpaEntity.visible,
                productCommonOptionJpaEntity.sort
            ))
            .from(productCommonOptionJpaEntity)
            .where(
                commonOptionGroupId.in(groupIds),
                commonOptionNameContainsItem(condition.keyword()),
                commonOptionSoldOutOrHidden(condition.soldOutOnly(), condition.hiddenOnly())
            )
            .orderBy(productCommonOptionJpaEntity.sort.asc())
            .fetch()
            .stream()
            .filter(row -> row.optionGroupId() != null)
            .collect(Collectors.groupingBy(
                row -> Objects.requireNonNull(row.optionGroupId()),
                LinkedHashMap::new,
                Collectors.mapping(
                    row -> new ProductOptionAvailabilityItemResult(
                        row.id(),
                        commonOptionType,
                        row.name(),
                        row.additionalPrice(),
                        Boolean.TRUE.equals(row.soldOut()),
                        row.soldOutUntil(),
                        Boolean.TRUE.equals(row.visible()),
                        row.sort()
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
                fileUrlResolver.urlOf(productImageFile.filePath),
                productImageJpaEntity.sort,
                productImageJpaEntity.visible
            ))
            .from(productImageJpaEntity)
            .leftJoin(productImageFile).on(productImageFile.id.eq(productImageJpaEntity.imageFileId))
            .where(productImageJpaEntity.productId.eq(productId))
            .orderBy(productImageJpaEntity.sort.asc())
            .fetch();
    }

    @Override
    public boolean existsProductInShop(Long productId, Long shopId) {
        return queryFactory
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
            .fetchFirst() != null;
    }

    @Override
    public List<ProductImageChangeRequestResult> findImageChangeRequests(Long productId) {
        return imageChangeRequestProjection()
            .where(productImageChangeRequestJpaEntity.productId.eq(productId))
            .orderBy(productImageChangeRequestJpaEntity.id.desc())
            .fetch();
    }

    @Override
    public PageResult<ProductImageChangeRequestResult> findImageChangeRequestPage(
        String status,
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
            .fetch();

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
        String status,
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
        String status,
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
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public List<PopularProductItemResult> findPopularProducts(
        Long shopId,
        String soldOrderStatus,
        ProductExposureWindow window
    ) {
        LocalDateTime now = window.now();

        List<PopularProductItemResult> representatives = popularProductProjection(now, soldOrderStatus)
            .where(
                productJpaEntity.shopId.eq(shopId),
                productJpaEntity.representative.isTrue(),
                orderableNow(window)
            )
            .orderBy(productJpaEntity.rating.desc(), productJpaEntity.id.asc())
            .limit(POPULAR_PRODUCT_LIMIT)
            .fetch();

        List<PopularProductItemResult> merged = new ArrayList<>(representatives);
        int remaining = POPULAR_PRODUCT_LIMIT - merged.size();
        if (remaining <= 0) {
            return List.copyOf(merged);
        }

        Set<Long> filledIds = merged.stream()
            .map(PopularProductItemResult::id)
            .collect(Collectors.toSet());

        List<PopularProductItemResult> popular = popularProductProjection(now, soldOrderStatus)
            .where(
                productJpaEntity.shopId.eq(shopId),
                orderableNow(window),
                filledIds.isEmpty() ? null : productJpaEntity.id.notIn(filledIds),
                soldQuantityOf(shopId, now, soldOrderStatus).gt(0L)
            )
            .orderBy(soldQuantityOf(shopId, now, soldOrderStatus).desc(), productJpaEntity.id.asc())
            .limit(remaining)
            .fetch();

        merged.addAll(popular);
        return List.copyOf(merged);
    }

    @Override
    public Optional<ProductVegetarianSettingResult> findVegetarianSetting(Long productId) {
        ProductVegetarianSettingResult result = queryFactory
            .select(Projections.constructor(ProductVegetarianSettingResult.class,
                productJpaEntity.id,
                productJpaEntity.shopId,
                productJpaEntity.vegetarianType
            ))
            .from(productJpaEntity)
            .where(productJpaEntity.id.eq(productId), notDeleted())
            .fetchFirst();
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<ProductExposurePeriodResult> findExposurePeriod(Long productId) {
        ProductExposurePeriodResult result = queryFactory
            .select(Projections.constructor(ProductExposurePeriodResult.class,
                productJpaEntity.id,
                productJpaEntity.shopId,
                productJpaEntity.exposureStartDate,
                productJpaEntity.exposureEndDate
            ))
            .from(productJpaEntity)
            .where(productJpaEntity.id.eq(productId), notDeleted())
            .fetchFirst();
        return Optional.ofNullable(result);
    }

    @Override
    public List<ProductExposureHourResult> findExposureHours(Long productId) {
        return queryFactory
            .select(Projections.constructor(ProductExposureHourResult.class,
                productExposureHourJpaEntity.dayType,
                productExposureHourJpaEntity.startTime,
                productExposureHourJpaEntity.endTime
            ))
            .from(productExposureHourJpaEntity)
            .where(productExposureHourJpaEntity.productId.eq(productId))
            .fetch();
    }

    @Override
    public List<ProductOwnerPriceView> findPrices(Long productId) {
        return queryFactory
            .select(Projections.constructor(ProductOwnerPriceView.class,
                productPriceJpaEntity.id,
                productPriceJpaEntity.priceName,
                productPriceJpaEntity.deliveryPrice,
                productPriceJpaEntity.storePrice,
                productPriceJpaEntity.pickupPrice,
                productPriceJpaEntity.sort
            ))
            .from(productPriceJpaEntity)
            .where(productPriceJpaEntity.productId.eq(productId))
            .orderBy(productPriceJpaEntity.sort.asc())
            .fetch();
    }

    private com.querydsl.jpa.JPQLQuery<ProductImageChangeRequestResult> imageChangeRequestProjection() {
        return queryFactory
            .select(Projections.constructor(ProductImageChangeRequestResult.class,
                productImageChangeRequestJpaEntity.id,
                productImageChangeRequestJpaEntity.productId,
                productJpaEntity.shopId,
                productJpaEntity.name,
                fileUrlResolver.urlOf(imageChangeRequestFile.filePath),
                productImageChangeRequestJpaEntity.status.stringValue(),
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
                productVegetarianRequestJpaEntity.vegetarianType.stringValue(),
                productVegetarianRequestJpaEntity.ingredients,
                productVegetarianRequestJpaEntity.description,
                productVegetarianRequestJpaEntity.status.stringValue(),
                productVegetarianRequestJpaEntity.rejectReason
            ))
            .from(productVegetarianRequestJpaEntity)
            .innerJoin(productJpaEntity).on(productJpaEntity.id.eq(productVegetarianRequestJpaEntity.productId));
    }

    private BooleanExpression imageChangeStatusEq(String status) {
        return status != null ? productImageChangeRequestJpaEntity.status.eq(status) : null;
    }

    private BooleanExpression vegetarianStatusEq(String status) {
        return status != null ? productVegetarianRequestJpaEntity.status.eq(status) : null;
    }

    private BooleanExpression representativeStatusEq(String status) {
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
                fileUrlResolver.urlOf(uploadedFileJpaEntity.filePath),
                productRepresentativeRequestJpaEntity.status.stringValue(),
                productRepresentativeRequestJpaEntity.rejectReason
            ))
            .from(productRepresentativeRequestJpaEntity)
            .innerJoin(productJpaEntity)
            .on(productJpaEntity.id.eq(productRepresentativeRequestJpaEntity.productId))
            .leftJoin(shopJpaEntity).on(shopJpaEntity.id.eq(productRepresentativeRequestJpaEntity.shopId))
            .leftJoin(productImageJpaEntity).on(representativeImageOf(productJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id));
    }

    private com.querydsl.jpa.JPQLQuery<PopularProductItemResult> popularProductProjection(
        LocalDateTime now,
        String soldOrderStatus
    ) {
        return queryFactory
            .select(Projections.constructor(PopularProductItemResult.class,
                productJpaEntity.id,
                productJpaEntity.name,
                fileUrlResolver.urlOf(uploadedFileJpaEntity.filePath),
                productJpaEntity.originalPrice,
                productJpaEntity.discountInfo.discountPrice,
                productJpaEntity.discountInfo.discountRate,
                productJpaEntity.rating,
                productJpaEntity.reviewCount,
                productJpaEntity.representative,
                productJpaEntity.spiciness,
                soldQuantityOf(productJpaEntity.shopId, now, soldOrderStatus)
            ))
            .from(productJpaEntity)
            .leftJoin(productImageJpaEntity).on(representativeImageOf(productJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id));
    }

    private NumberExpression<Long> soldQuantityOf(
        com.querydsl.core.types.Expression<Long> shopIdExpression,
        LocalDateTime now,
        String soldOrderStatus
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
                    orderJpaEntity.orderStatus.stringValue().eq(soldOrderStatus),
                    orderJpaEntity.createdAt.goe(now.minusDays(POPULAR_PRODUCT_WINDOW_DAYS))
                )
        ).coalesce(0L);
    }

    private NumberExpression<Long> soldQuantityOf(Long shopId, LocalDateTime now, String soldOrderStatus) {
        return soldQuantityOf(com.querydsl.core.types.dsl.Expressions.constant(shopId), now, soldOrderStatus);
    }

    private BooleanExpression orderableNow(ProductExposureWindow window) {
        return productJpaEntity.visible.isTrue()
            .and(productJpaEntity.soldOut.isFalse())
            .and(notDeleted())
            .and(exposedNow(window));
    }

    @Override
    public Optional<ProductBbqSyncTargetResult> findFirstBbqSyncTarget() {
        ProductBbqSyncTargetResult result = queryFactory
            .select(Projections.constructor(ProductBbqSyncTargetResult.class,
                productBbqJpaEntity.productId,
                productBbqJpaEntity.bbqMenuId,
                productJpaEntity.name
            ))
            .from(productBbqJpaEntity)
            .innerJoin(productJpaEntity).on(productBbqJpaEntity.productId.eq(productJpaEntity.id))
            .where(productBbqJpaEntity.optionsSynced.eq(false), notDeleted())
            .fetchFirst();
        return Optional.ofNullable(result);
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
            .select(Projections.constructor(IdStringRow.class,
                imageProductId,
                uploadedFileJpaEntity.filePath
            ))
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
            .filter(row -> row.id() != null && row.value() != null)
            .collect(Collectors.toMap(
                row -> Objects.requireNonNull(row.id()),
                row -> Objects.requireNonNull(row.value()),
                (existing, ignored) -> existing
            ));
    }

    private BooleanExpression todayDiscountSearchable(ProductExposureWindow window) {
        return productJpaEntity.discountInfo.discountPrice.isNotNull()
            .and(productJpaEntity.visible.eq(true))
            .and(notDeleted())
            .and(exposedNow(window));
    }

    private BooleanExpression exposedNow(ProductExposureWindow window) {
        LocalDateTime now = window.now();
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

        BooleanExpression todayBranch = subExposureHour.dayType.in(window.todayDayTypes())
            .and(coversTime(time));
        BooleanExpression previousDayBranch =
            subExposureHour.dayType.in(window.previousDayDayTypes())
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
