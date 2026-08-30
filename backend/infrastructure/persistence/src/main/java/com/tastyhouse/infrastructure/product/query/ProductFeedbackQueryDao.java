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

/**
 * 점주 화면의 고객 의견 read 어댑터.
 *
 * <p><b>왜 {@code ProductQueryDao}에 메서드를 더하지 않고 DAO를 새로 두는가</b>: {@code ProductQueryDao}는
 * 이미 2000줄이 넘고 메뉴·옵션·카테고리·승인요청을 한 클래스가 떠맡고 있다. 고객 의견은 자체 테이블과
 * 자체 집계 형태(메뉴 × 유형 group by)를 갖는 독립 조회 대상이라 별 파일로 두면 그 형태가 한눈에 보인다
 * ({@code StorePriceVerificationQueryDao}가 같은 이유로 분리돼 있다).
 *
 * <p><b>제보자 정보를 어떤 투영에도 담지 않는다.</b> 점주가 특정 손님을 식별하면 보복 우려가 있고,
 * 제보의 목적은 정보 수정이지 손님 응대가 아니다. {@code member_id}는 중복 방지 판정(write 포트)에만 쓴다.
 */
@Repository
public class ProductFeedbackQueryDao implements ProductFeedbackQueryPort {

    /**
     * 한 집계 줄에 실어 보내는 {@code ETC} 서술의 최대 건수. 무제한으로 실으면 제보가 많은 메뉴 하나가
     * 응답을 뒤덮어 다른 메뉴의 지적이 묻힌다.
     */
    private static final int MAX_CONTENTS_PER_GROUP = 10;

    private final JPAQueryFactory queryFactory;

    public ProductFeedbackQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 가게의 고객 의견을 <b>메뉴 × 유형으로 묶어</b> 접수 건수 많은 순으로 페이징 조회한다.
     *
     * <p>{@code since} 이후 접수분만 센다 — 점주는 지난 한 주치만 본다. 기준 시각을 파라미터로 받아
     * DAO가 시계를 직접 읽지 않게 한다(호출부가 같은 {@code now}로 목록·미확인 판정을 일관되게 맞춘다).
     *
     * <p>페이징을 집계 단위(메뉴 × 유형)로 하는 이유는 그것이 화면의 한 줄이기 때문이다 — 제보 건 단위로
     * 페이징하면 한 집계 줄이 페이지 경계에서 쪼개져 건수가 잘못 보인다.
     */
    @Override
    public PageResult<ProductFeedbackSummaryResult> findFeedbackSummaries(
        Long shopId,
        LocalDateTime since,
        PageQuery pageQuery
    ) {
        // 전체 건수가 아니라 "집계 줄 수"를 센다 — 화면의 한 줄이 메뉴 × 유형이므로 페이징 총계도
        // 같은 단위여야 마지막 페이지 계산이 맞는다. group by 결과의 행 수라 count(*)로는 얻을 수 없다.
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
            // 메뉴가 소프트 삭제됐어도 제보는 남으므로 inner join으로 사라지지 않게 한다 —
            // 삭제된 메뉴에 대한 지적도 점주가 원인을 파악할 근거가 된다.
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

        // 서술은 ETC 유형에만 존재한다 — 다른 유형은 유형 자체가 내용이라 실을 것이 없다.
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

    /**
     * 이 페이지에 실린 {@code ETC} 집계 줄들의 서술을 한 번에 읽어 메뉴별로 묶는다.
     *
     * <p>집계 줄마다 따로 조회하면 페이지 크기만큼 쿼리가 늘어난다(N+1). 대상 메뉴를 모아 한 번에 읽고
     * 자바에서 나눈다.
     */
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

        // 메뉴별 상한을 자바에서 적용한다 — 그룹별 LIMIT은 표준 SQL로 표현할 수 없고,
        // 창 함수를 쓰면 이 조회만 네이티브 SQL이 되어 컴파일 검증에서 벗어난다.
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
