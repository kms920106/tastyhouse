package com.tastyhouse.infrastructure.product.query;

import com.tastyhouse.application.product.port.out.ProductFeedbackQueryPort;
import com.tastyhouse.application.product.port.out.ProductFeedbackSummaryResult;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductFeedbackType;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

import static com.tastyhouse.infrastructure.product.persistence.QProductFeedbackJpaEntity.productFeedbackJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductJpaEntity.productJpaEntity;

@Repository
public class ProductFeedbackQueryDao implements ProductFeedbackQueryPort {
    private static final int MAX_CONTENTS_PER_GROUP = 10;

    private final JPAQueryFactory queryFactory;

    public ProductFeedbackQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public PageResult<ProductFeedbackSummaryResult> findFeedbackSummaries(
        Long shopId,
        LocalDateTime since,
        PageQuery pageQuery
    ) {
        long total = queryFactory
            .select(productFeedbackJpaEntity.productId, productFeedbackJpaEntity.feedbackType)
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

        List<Tuple> rows = queryFactory
            .select(
                productFeedbackJpaEntity.productId,
                productJpaEntity.name,
                productFeedbackJpaEntity.feedbackType,
                productFeedbackJpaEntity.count()
            )
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

        Map<Long, List<String>> contentsByProductId = findEtcContents(shopId, since, rows);

        List<ProductFeedbackSummaryResult> content = rows.stream()
            .map(row -> toSummary(row, contentsByProductId))
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    private ProductFeedbackSummaryResult toSummary(Tuple row, Map<Long, List<String>> contentsByProductId) {
        Long productId = row.get(productFeedbackJpaEntity.productId);
        ProductFeedbackType feedbackType = row.get(productFeedbackJpaEntity.feedbackType);
        Long rowCount = row.get(productFeedbackJpaEntity.count());

        List<String> contents = feedbackType == ProductFeedbackType.ETC
            ? contentsByProductId.getOrDefault(productId, List.of())
            : List.of();

        return new ProductFeedbackSummaryResult(
            productId,
            row.get(productJpaEntity.name),
            feedbackType,
            rowCount == null ? 0 : rowCount.intValue(),
            contents
        );
    }

    private Map<Long, List<String>> findEtcContents(Long shopId, LocalDateTime since, List<Tuple> rows) {
        List<Long> etcProductIds = rows.stream()
            .filter(row -> row.get(productFeedbackJpaEntity.feedbackType) == ProductFeedbackType.ETC)
            .map(row -> row.get(productFeedbackJpaEntity.productId))
            .distinct()
            .toList();

        if (etcProductIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> contentRows = queryFactory
            .select(productFeedbackJpaEntity.productId, productFeedbackJpaEntity.content)
            .from(productFeedbackJpaEntity)
            .where(
                productFeedbackJpaEntity.shopId.eq(shopId),
                productFeedbackJpaEntity.createdAt.goe(since),
                productFeedbackJpaEntity.feedbackType.eq(ProductFeedbackType.ETC),
                productFeedbackJpaEntity.productId.in(etcProductIds),
                productFeedbackJpaEntity.content.isNotNull()
            )
            .orderBy(productFeedbackJpaEntity.createdAt.desc())
            .fetch();

        Map<Long, List<String>> contentsByProductId = new LinkedHashMap<>();
        for (Tuple contentRow : contentRows) {
            Long productId = contentRow.get(productFeedbackJpaEntity.productId);
            List<String> contents = contentsByProductId.computeIfAbsent(productId, key -> new ArrayList<>());
            if (contents.size() < MAX_CONTENTS_PER_GROUP) {
                contents.add(contentRow.get(productFeedbackJpaEntity.content));
            }
        }
        return contentsByProductId;
    }
}
