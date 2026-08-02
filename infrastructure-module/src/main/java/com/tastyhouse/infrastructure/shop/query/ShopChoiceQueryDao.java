package com.tastyhouse.infrastructure.shop.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.domain.service.EditorChoicePolicy;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.product.persistence.QProductImageJpaEntity;
import com.tastyhouse.infrastructure.product.query.ProductSimpleResult;
import com.tastyhouse.infrastructure.product.query.QProductSimpleResult;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductImageJpaEntity.productImageJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductJpaEntity.productJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopChoiceJpaEntity.shopChoiceJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QStationJpaEntity.stationJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QTagJpaEntity.tagJpaEntity;

/**
 * 에디터 추천·태그·지하철역 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로 write
 * 포트({@code ShopChoiceRepository}/{@code TagRepository}/{@code ShopDetailRepository})와 역할이
 * 겹치지 않는다. 소비 모듈(web/admin-api)의 {@code Shop*QueryService}가 이 DAO를 주입해 사용한다.
 *
 * <p>{@link ShopQueryDao}(가게별 설정·관리)·{@link ShopSearchQueryDao}(목록·검색)와 함께 shop 도메인의
 * 세 번째 용도별 DAO다 — 가게에 종속되지 않는 <b>독립 조회</b>(에디터 추천 목록, 전역 태그·역 목록)를
 * 담당한다.
 */
@Repository
@RequiredArgsConstructor
public class ShopChoiceQueryDao {

    /**
     * 상품의 대표 이미지(노출 중 최소 sort)를 고르기 위한 서브쿼리 별칭.
     */
    private static final QProductImageJpaEntity subProductImage = new QProductImageJpaEntity("subProductImage");

    private final JPAQueryFactory queryFactory;

    /**
     * 에디터 추천 목록 — 가게 정보와 대표 상품 {@value EditorChoicePolicy#PRODUCT_LIMIT}건을 함께 채운다.
     * 폐업·노출정지 가게의 추천은 제외한다.
     */
    public PageResult<EditorChoiceResult> findEditorChoices(PageQuery pageQuery) {
        Long totalCount = queryFactory
            .select(shopChoiceJpaEntity.count())
            .from(shopChoiceJpaEntity)
            .fetchOne();

        if (totalCount == null || totalCount == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<Tuple> shopChoices = queryFactory
            .select(
                shopChoiceJpaEntity.id,
                shopChoiceJpaEntity.shopId,
                shopJpaEntity.name,
                shopChoiceJpaEntity.title,
                shopChoiceJpaEntity.content,
                uploadedFileJpaEntity.filePath
            )
            .from(shopChoiceJpaEntity)
            .innerJoin(shopJpaEntity).on(shopJpaEntity.id.eq(shopChoiceJpaEntity.shopId)
                .and(shopJpaEntity.permanentlyClosed.eq(false))
                .and(shopJpaEntity.hidden.eq(false)))
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopJpaEntity.thumbnailImageFileId))
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        List<Long> shopIds = shopChoices.stream()
            .map(tuple -> tuple.get(shopChoiceJpaEntity.shopId))
            .distinct()
            .toList();

        Map<Long, List<ProductSimpleResult>> productsByShopId = productsByShopId(shopIds);

        List<EditorChoiceResult> content = shopChoices.stream()
            .map(tuple -> {
                Long shopIdValue = tuple.get(shopChoiceJpaEntity.shopId);
                List<ProductSimpleResult> products = productsByShopId.getOrDefault(shopIdValue, new ArrayList<>());
                return new EditorChoiceResult(
                    tuple.get(shopChoiceJpaEntity.id),
                    shopIdValue,
                    tuple.get(shopJpaEntity.name),
                    tuple.get(shopChoiceJpaEntity.title),
                    tuple.get(shopChoiceJpaEntity.content),
                    tuple.get(uploadedFileJpaEntity.filePath),
                    products
                );
            })
            .toList();

        return PageResult.of(content, totalCount, pageQuery.page(), pageQuery.size());
    }

    /**
     * 에디터 추천 단건(수정 화면) — 가게 정보 없이 추천 본문만. 없으면 비어 있다.
     */
    public Optional<ShopChoiceDetailResult> findShopChoiceById(Long id) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(ShopChoiceDetailResult.class,
                    shopChoiceJpaEntity.id,
                    shopChoiceJpaEntity.shopId,
                    shopChoiceJpaEntity.title,
                    shopChoiceJpaEntity.content
                ))
                .from(shopChoiceJpaEntity)
                .where(shopChoiceJpaEntity.id.eq(id))
                .fetchOne()
        );
    }

    /**
     * 전체 태그 목록 — 최근 등록 순.
     */
    public List<TagResult> findAllTags() {
        return queryFactory
            .select(Projections.constructor(TagResult.class,
                tagJpaEntity.id,
                tagJpaEntity.tagName
            ))
            .from(tagJpaEntity)
            .orderBy(tagJpaEntity.id.desc())
            .fetch();
    }

    /**
     * 전체 지하철역 목록 — 역명 순.
     */
    public List<StationResult> findAllStations() {
        return queryFactory
            .select(Projections.constructor(StationResult.class,
                stationJpaEntity.id,
                stationJpaEntity.stationName
            ))
            .from(stationJpaEntity)
            .orderBy(stationJpaEntity.stationName.asc())
            .fetch();
    }

    /**
     * 가게별 대표 상품 목록. 상품의 대표 이미지는 노출 중 최소 sort 이미지를 서브쿼리로 고른다.
     */
    private Map<Long, List<ProductSimpleResult>> productsByShopId(List<Long> shopIds) {
        if (shopIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> productTuples = queryFactory
            .select(
                productJpaEntity.shopId,
                new QProductSimpleResult(
                    productJpaEntity.id,
                    shopJpaEntity.name,
                    productJpaEntity.name,
                    uploadedFileJpaEntity.filePath,
                    productJpaEntity.originalPrice,
                    productJpaEntity.discountInfo.discountPrice,
                    productJpaEntity.discountInfo.discountRate
                )
            )
            .from(productJpaEntity)
            .innerJoin(shopJpaEntity).on(shopJpaEntity.id.eq(productJpaEntity.shopId))
            .leftJoin(productImageJpaEntity).on(
                productImageJpaEntity.productId.eq(productJpaEntity.id)
                    .and(productImageJpaEntity.visible.eq(true))
                    .and(productImageJpaEntity.sort.eq(
                        JPAExpressions
                            .select(subProductImage.sort.min())
                            .from(subProductImage)
                            .where(subProductImage.productId.eq(productJpaEntity.id)
                                .and(subProductImage.visible.eq(true)))
                    ))
            )
            .leftJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(productJpaEntity.shopId.in(shopIds))
            .fetch();

        return productTuples.stream()
            .filter(tuple -> tuple.get(productJpaEntity.shopId) != null)
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(productJpaEntity.shopId)),
                Collectors.mapping(
                    tuple -> tuple.get(1, ProductSimpleResult.class),
                    Collectors.toList()
                )
            ))
            .entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream().limit(EditorChoicePolicy.PRODUCT_LIMIT).toList()
            ));
    }
}
