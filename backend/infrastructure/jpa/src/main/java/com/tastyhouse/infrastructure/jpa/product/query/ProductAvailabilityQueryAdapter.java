package com.tastyhouse.infrastructure.jpa.product.query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.application.product.port.out.ProductAvailabilityItemResult;
import com.tastyhouse.application.product.port.out.ProductAvailabilityQueryPort;
import com.tastyhouse.application.product.port.out.ProductAvailabilitySearchCondition;
import com.tastyhouse.application.product.port.out.ProductOptionAvailabilityGroupResult;
import com.tastyhouse.application.product.port.out.ProductOptionAvailabilityItemResult;
import com.tastyhouse.infrastructure.jpa.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.jpa.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductCategoryJpaEntity.productCategoryJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductCommonOptionGroupJpaEntity.productCommonOptionGroupJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductCommonOptionGroupLinkJpaEntity.productCommonOptionGroupLinkJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductCommonOptionJpaEntity.productCommonOptionJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductImageJpaEntity.productImageJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductJpaEntity.productJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductOptionGroupJpaEntity.productOptionGroupJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductOptionGroupLinkJpaEntity.productOptionGroupLinkJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductOptionJpaEntity.productOptionJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductShopLinkJpaEntity.productShopLinkJpaEntity;

@Repository
class ProductAvailabilityQueryAdapter implements ProductAvailabilityQueryPort {

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ProductAvailabilityQueryAdapter(
        JPAQueryFactory queryFactory,
        FileUrlResolver fileUrlResolver
    ) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
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
            .leftJoin(productImageJpaEntity).on(ProductQueryPredicates.representativeImageOf(productJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(
                productShopLinkJpaEntity.shopId.eq(condition.shopId()),
                ProductQueryPredicates.nameContains(condition.keyword()),
                soldOutOrHidden(condition.soldOutOnly(), condition.hiddenOnly()),
                ProductQueryPredicates.notDeleted()
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
                ProductQueryPredicates.notDeleted(),
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
            .where(productOptionGroupJpaEntity.id.in(groupIds), ProductQueryPredicates.notDeleted())
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
                ProductQueryPredicates.notDeleted(),
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
            .where(productCommonOptionGroupJpaEntity.id.in(groupIds), ProductQueryPredicates.notDeleted())
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
}
