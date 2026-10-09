package com.tastyhouse.infrastructure.jpa.product.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.BatchOptionResult;
import com.tastyhouse.application.product.port.out.OptionGroupResult;
import com.tastyhouse.application.product.port.out.OptionResult;
import com.tastyhouse.application.product.port.out.ProductBatchItem;
import com.tastyhouse.application.product.port.out.ProductBatchQueryPort;
import com.tastyhouse.application.product.port.out.ProductBatchResult;
import com.tastyhouse.application.product.port.out.ProductOptionQueryPort;
import com.tastyhouse.application.product.port.out.ProductOptionsResult;
import com.tastyhouse.infrastructure.jpa.file.query.FileUrlResolver;
import com.tastyhouse.infrastructure.jpa.shared.query.IdStringRow;

import static com.tastyhouse.infrastructure.jpa.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductCommonOptionGroupJpaEntity.productCommonOptionGroupJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductCommonOptionGroupLinkJpaEntity.productCommonOptionGroupLinkJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductCommonOptionJpaEntity.productCommonOptionJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductImageJpaEntity.productImageJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductJpaEntity.productJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductOptionGroupJpaEntity.productOptionGroupJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductOptionGroupLinkJpaEntity.productOptionGroupLinkJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductOptionJpaEntity.productOptionJpaEntity;

@Repository
class ProductOptionQueryAdapter implements ProductOptionQueryPort, ProductBatchQueryPort {

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ProductOptionQueryAdapter(
        JPAQueryFactory queryFactory,
        FileUrlResolver fileUrlResolver
    ) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
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
            .where(productJpaEntity.id.in(productIds), productJpaEntity.visible.eq(true), ProductQueryPredicates.notDeleted())
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
                        .select(ProductQueryPredicates.subProductImage.sort.min())
                        .from(ProductQueryPredicates.subProductImage)
                        .where(ProductQueryPredicates.subProductImage.productId.eq(imageProductId)
                            .and(ProductQueryPredicates.subProductImage.visible.eq(true)))
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
