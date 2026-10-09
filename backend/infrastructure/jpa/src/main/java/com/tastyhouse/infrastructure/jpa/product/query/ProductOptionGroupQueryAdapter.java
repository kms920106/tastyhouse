package com.tastyhouse.infrastructure.jpa.product.query;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.ProductOptionGroupLinkedProductResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupManagementResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupMergeCandidateResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupQueryPort;
import com.tastyhouse.application.product.port.out.ProductOptionManagementResult;

import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductJpaEntity.productJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductOptionGroupJpaEntity.productOptionGroupJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductOptionGroupLinkJpaEntity.productOptionGroupLinkJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductOptionGroupMergeExclusionJpaEntity.productOptionGroupMergeExclusionJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductOptionJpaEntity.productOptionJpaEntity;

@Repository
class ProductOptionGroupQueryAdapter implements ProductOptionGroupQueryPort {

    private static final com.tastyhouse.infrastructure.jpa.product.persistence.QProductOptionGroupLinkJpaEntity
        subOptionGroupLink =
        new com.tastyhouse.infrastructure.jpa.product.persistence.QProductOptionGroupLinkJpaEntity("subOptionGroupLink");

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
    private final EntityManager entityManager;

    public ProductOptionGroupQueryAdapter(
        JPAQueryFactory queryFactory,
        EntityManager entityManager
    ) {
        this.queryFactory = queryFactory;
        this.entityManager = entityManager;
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
            .where(productJpaEntity.shopId.eq(shopId), ProductQueryPredicates.notDeleted())
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
            .where(productOptionGroupLinkJpaEntity.optionGroupId.eq(optionGroupId), ProductQueryPredicates.notDeleted())
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
            .where(productJpaEntity.shopId.eq(shopId), ProductQueryPredicates.notDeleted())
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
}
