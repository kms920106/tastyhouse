package com.tastyhouse.infrastructure.persistence.product.query;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.ProductFeedbackQueryPort;
import com.tastyhouse.application.product.port.out.ProductFeedbackSummaryResult;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.infrastructure.persistence.shared.query.IdStringRow;

import static com.tastyhouse.infrastructure.persistence.product.persistence.QProductFeedbackJpaEntity.productFeedbackJpaEntity;
import static com.tastyhouse.infrastructure.persistence.product.persistence.QProductJpaEntity.productJpaEntity;

@Repository
class ProductFeedbackQueryAdapter implements ProductFeedbackQueryPort {

    private static final int MAX_CONTENTS_PER_GROUP = 10;

    private final JPAQueryFactory queryFactory;

    public ProductFeedbackQueryAdapter(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public PageResult<ProductFeedbackSummaryResult> findFeedbackSummaries(
        Long shopId,
        LocalDateTime since,
        String contentRequiredType,
        PageQuery pageQuery
    ) {
        long total = queryFactory
            .select(productFeedbackJpaEntity.productId)
            .from(productFeedbackJpaEntity)
            .where(
                productFeedbackJpaEntity.shopId.eq(shopId),
                productFeedbackJpaEntity.createdAt.goe(since)
            )
            .groupBy(productFeedbackJpaEntity.productId, productFeedbackJpaEntity.feedbackType)
            .fetch()
            .size();

        if (total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ProductFeedbackSummaryRow> rows = queryFactory
            .select(Projections.constructor(ProductFeedbackSummaryRow.class,
                productFeedbackJpaEntity.productId,
                productJpaEntity.name,
                productFeedbackJpaEntity.feedbackType,
                productFeedbackJpaEntity.count()
            ))
            .from(productFeedbackJpaEntity)
            .leftJoin(productJpaEntity).on(productJpaEntity.id.eq(productFeedbackJpaEntity.productId))
            .where(
                productFeedbackJpaEntity.shopId.eq(shopId),
                productFeedbackJpaEntity.createdAt.goe(since)
            )
            .groupBy(productFeedbackJpaEntity.productId, productJpaEntity.name, productFeedbackJpaEntity.feedbackType)
            .orderBy(
                productFeedbackJpaEntity.count().desc(),
                productFeedbackJpaEntity.productId.asc()
            )
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        if (rows.isEmpty()) {
            return PageResult.of(List.of(), total, pageQuery.page(), pageQuery.size());
        }

        Map<Long, List<String>> contentsByProductId = findContents(shopId, since, contentRequiredType, rows);

        List<ProductFeedbackSummaryResult> content = rows.stream()
            .map(row -> toSummary(row, contentRequiredType, contentsByProductId))
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    private ProductFeedbackSummaryResult toSummary(
        ProductFeedbackSummaryRow row,
        String contentRequiredType,
        Map<Long, List<String>> contentsByProductId
    ) {
        Long productId = row.productId();
        String feedbackType = row.feedbackType();
        Long rowCount = row.count();

        List<String> contents = contentRequiredType.equals(feedbackType)
            ? contentsByProductId.getOrDefault(productId, List.of())
            : List.of();

        return new ProductFeedbackSummaryResult(
            productId,
            row.name(),
            feedbackType,
            rowCount == null ? 0 : rowCount.intValue(),
            contents
        );
    }

    private Map<Long, List<String>> findContents(
        Long shopId,
        LocalDateTime since,
        String contentRequiredType,
        List<ProductFeedbackSummaryRow> rows
    ) {
        List<Long> contentProductIds = rows.stream()
            .filter(row -> contentRequiredType.equals(row.feedbackType()))
            .map(ProductFeedbackSummaryRow::productId)
            .distinct()
            .toList();

        if (contentProductIds.isEmpty()) {
            return Map.of();
        }

        List<IdStringRow> contentRows = queryFactory
            .select(Projections.constructor(IdStringRow.class,
                productFeedbackJpaEntity.productId,
                productFeedbackJpaEntity.content
            ))
            .from(productFeedbackJpaEntity)
            .where(
                productFeedbackJpaEntity.shopId.eq(shopId),
                productFeedbackJpaEntity.createdAt.goe(since),
                productFeedbackJpaEntity.feedbackType.eq(contentRequiredType),
                productFeedbackJpaEntity.productId.in(contentProductIds),
                productFeedbackJpaEntity.content.isNotNull()
            )
            .orderBy(productFeedbackJpaEntity.createdAt.desc())
            .fetch();

        Map<Long, List<String>> contentsByProductId = new LinkedHashMap<>();
        for (IdStringRow contentRow : contentRows) {
            Long productId = contentRow.id();
            List<String> contents = contentsByProductId.computeIfAbsent(productId, key -> new ArrayList<>());
            if (contents.size() < MAX_CONTENTS_PER_GROUP) {
                contents.add(contentRow.value());
            }
        }
        return contentsByProductId;
    }
}
