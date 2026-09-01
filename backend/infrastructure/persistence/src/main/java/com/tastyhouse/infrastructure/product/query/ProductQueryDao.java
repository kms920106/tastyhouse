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
 *   <li>ceo — {@link #findProductManagementDetailById}, {@link #findProductAvailability}</li>
 *   <li>batch — {@link #findFirstBbqSyncTarget}</li>
 * </ul>
 * 상품 대표 이미지 경로를 위해 file 도메인, 가게명을 위해 shop 도메인의 Q타입을 조인한다(같은 모듈 내 참조).
 *
 * <p>조인으로 얻은 저장 경로는 {@link FileUrlResolver}로 표시용 URL까지 변환해 Result에 담는다 —
 * {@code Projections.constructor}는 생성자 직접 투영이라 변환을 투영식에 끼울 수 없어, fetch 직후 재조립한다.
 */
@Repository
public class ProductQueryDao implements ProductQueryPort, ProductBbqSyncQueryPort, ProductManagementQueryPort, ProductOwnerQueryPort {

    /**
     * 상품의 대표 이미지(노출 중 최소 sort)를 고르기 위한 서브쿼리 별칭.
     */
    private static final com.tastyhouse.infrastructure.product.persistence.QProductImageJpaEntity subProductImage =
        new com.tastyhouse.infrastructure.product.persistence.QProductImageJpaEntity("subProductImage");

    /**
     * 이미지 변경요청의 요청 파일 조인용 별칭 — 본 쿼리가 이미 {@code uploadedFileJpaEntity}를 다른
     * 목적(대표 이미지)으로 쓰므로 같은 별칭을 재사용하면 조인이 서로를 덮는다.
     */
    private static final com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity
        imageChangeRequestFile =
        new com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity("imageChangeRequestFile");

    /** 메뉴 이미지 관리 목록의 파일 조인용 별칭. */
    private static final com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity
        productImageFile =
        new com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity("productImageFile");

    /** 노출 시간대 술어용 서브쿼리 별칭. */
    private static final com.tastyhouse.infrastructure.product.persistence.QProductExposureHourJpaEntity
        subExposureHour =
        new com.tastyhouse.infrastructure.product.persistence.QProductExposureHourJpaEntity("subExposureHour");

    /** 메뉴그룹별 소속 메뉴 수를 세는 서브쿼리 별칭. */
    private static final com.tastyhouse.infrastructure.product.persistence.QProductJpaEntity subCategoryProduct =
        new com.tastyhouse.infrastructure.product.persistence.QProductJpaEntity("subCategoryProduct");

    /**
     * 옵션그룹별 연결 메뉴 수를 세는 서브쿼리 별칭 — 본 쿼리가 이미 링크 테이블을 조인하므로
     * 같은 별칭을 재사용하면 카운트가 조인된 1건으로 좁혀진다.
     */
    private static final com.tastyhouse.infrastructure.product.persistence.QProductOptionGroupLinkJpaEntity
        subOptionGroupLink =
        new com.tastyhouse.infrastructure.product.persistence.QProductOptionGroupLinkJpaEntity("subOptionGroupLink");

    /**
     * 노출 판정 기준 타임존. 술어가 {@code CURRENT_DATE}/{@code CURRENT_TIME}를 쓰지 않는 이유는
     * DB 서버 타임존에 판정이 좌우되지 않게 하기 위함이다 — 판정 시각은 애플리케이션이 정한다.
     */
    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    /** 인기 메뉴 그룹 노출 상한(PDF 기준). 사장님 추천과 판매량 순위를 합쳐 이 개수를 넘지 않는다. */
    private static final int POPULAR_PRODUCT_LIMIT = 5;

    /** 판매량 집계 창(일). 창이 없으면 오래 전 히트 메뉴가 순위를 영구히 점유한다. */
    private static final long POPULAR_PRODUCT_WINDOW_DAYS = 30L;

    /**
     * 합치기 추천 후보 SQL. 집계 1패스로 파생 키를 만들고, 그 키가 2건 이상인 것만 남긴다.
     *
     * <p>{@code GROUP_CONCAT(... ORDER BY o.name, o.additional_price)}로 옵션 순서를 정규화한다 —
     * 옵션 {@code sort}는 그룹마다 다르지만 동일성에는 영향이 없다(같은 옵션을 다른 순서로 진열했을 뿐).
     * 이 정렬 규칙은 {@code ProductOptionGroupSignature.payloadOf}와 <b>글자 단위로 일치</b>해야 한다.
     */
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

    // ── web ────────────────────────────────────────────────────────────────

    /**
     * 오늘의 할인 상품 목록 — 할인가가 설정된 노출 상품을 할인율 내림차순으로 페이징한다.
     */
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

    /**
     * 오늘의 할인 상품 총 건수 — 목록 쿼리와 같은 {@code innerJoin}(shop)·같은 where를 재현한다.
     *
     * <p>대표 이미지·파일 {@code leftJoin}은 "노출 중 최소 sort 1장"으로 좁혀져 상품당 최대 1행이라
     * 행이 늘지 않으므로 count 쿼리에서 생략한다. 가게 조인은 {@code innerJoin}이라 짝이 없는 상품을
     * 제외하므로 총 건수에 영향을 주어 그대로 재현한다.
     */
    private long countTodayDiscountProducts(LocalDateTime now) {
        Long total = queryFactory
            .select(productJpaEntity.count())
            .from(productJpaEntity)
            .innerJoin(shopJpaEntity).on(productJpaEntity.shopId.eq(shopJpaEntity.id))
            .where(todayDiscountSearchable(now))
            .fetchOne();

        return total == null ? 0L : total;
    }

    /**
     * 통합검색 상품 결과 — 판매 중(노출·비품절) 상품 중 영업 중인 가게의 것만 검색한다.
     */
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

    /**
     * 상품 옵션 목록 — 개별 옵션 그룹과 공통 옵션 그룹을 단일 목록으로 병합해 반환한다(개별 먼저).
     * 그룹·옵션을 각각 배치(in) 조회해 N+1을 방지한다.
     */
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
            // 정렬은 그룹이 아니라 링크가 갖는다 — 같은 그룹도 메뉴마다 순서가 다를 수 있다.
            .orderBy(productOptionGroupLinkJpaEntity.sort.asc())
            .fetch();

        if (groups.isEmpty()) {
            return List.of();
        }

        List<Long> groupIds = groups.stream().map(tuple -> tuple.get(productOptionGroupJpaEntity.id)).toList();
        // select와 Tuple.get이 같은 표현식을 참조하도록 numberPath를 지역 변수로 추출한다.
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
            // 정렬은 그룹이 아니라 링크가 갖는다 — 같은 그룹도 메뉴마다 순서가 다를 수 있다.
            .orderBy(productCommonOptionGroupLinkJpaEntity.sort.asc())
            .fetch();

        if (groups.isEmpty()) {
            return List.of();
        }

        List<Long> groupIds = groups.stream().map(tuple -> tuple.get(productCommonOptionGroupJpaEntity.id)).toList();
        // select와 Tuple.get이 같은 표현식을 참조하도록 numberPath를 지역 변수로 추출한다.
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
                    // 공통 옵션은 보증금 대상이 아니므로 컵 관련 값이 없다(§공통 미러 테이블 미변경 결정).
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
                // 공통 옵션그룹은 점주 CRUD 대상이 아니고 주문 검증 경로도 일반 옵션만 보므로, 구조상
                // 보증금 유형이 될 수 없다. 미러 테이블에 죽은 컬럼을 추가하는 대신 여기서 하드코딩해
                // 그 사실을 코드에 남긴다(common=true를 하드코딩하는 방식과 동일).
                ProductOptionGroupType.NORMAL.name(),
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
            // 그룹이 없거나, 옵션이 요청한 상품에 연결돼 있지 않으면 제외.
            // 연결이 0건인 그룹(고아)은 set 자체가 없으므로 함께 걸러진다.
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
                    // 금액은 저장값이 아니라 컵 개수에서 매번 계산한다 — findProductOptions와 같은
                    // 원천을 써야 메뉴판과 결제화면의 보증금이 갈리지 않는다.
                    cupDepositPolicy.depositAmountOf(optionInfo.cupCount()),
                    optionInfo.personalCupDiscountAmount()
                ));
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

        // select와 Tuple.get이 같은 표현식을 참조하도록 numberPath를 지역 변수로 추출한다.
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
                    // 공통 옵션그룹은 구조상 보증금 유형이 될 수 없어(findCommonOptionGroups의
                    // NORMAL 하드코딩과 같은 근거) 보증금 필드가 항상 null이다.
                    null,
                    null
                )
            ));

        return optionById;
    }

    /**
     * 옵션의 소속 상품 검증용 — 옵션 그룹(개별/공통)에 <b>연결된 상품 전부</b>를 조회한다.
     *
     * <p><b>반환이 {@code Map<Long, Long>}이 아니라 {@code Map<Long, Set<Long>>}인 것이 핵심이다.</b>
     * 링크 테이블 도입으로 한 그룹이 여러 메뉴에 연결되므로, 소유 상품을 단건으로 보면
     * "그 그룹의 임의의 한 메뉴"만 통과하고 나머지 메뉴의 옵션은 <b>예외도 로그도 없이 사라져</b>
     * 장바구니 금액만 조용히 틀어진다.
     *
     * <p>개별·공통 그룹의 id 공간이 서로 겹칠 수 있으므로 결과 키는
     * {@link BatchOptionInfo#groupKey()}(공통 여부를 함께 담은 키)다.
     */
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

    /**
     * 상품의 노출 이미지 표시용 URL 목록(sort 오름차순). 화면 이미지 갤러리용.
     */
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

    /**
     * 가게 상세 화면의 상품 목록 — 대표 상품·평점 순으로, 카테고리별 그룹핑은 소비 모듈이 수행한다.
     *
     * <p><b>{@code PRODUCT.shop_id}가 아니라 {@code PRODUCT_SHOP_LINK}로 조회한다.</b> 한 메뉴가 여러
     * 가게 메뉴판에 노출될 수 있으므로 "이 가게 메뉴판에 무엇이 걸려 있는가"의 진실원은 링크 테이블이다.
     * {@code shop_id}는 원본 소유 가게로 남아 다른 판정(메뉴명 중복·옵션그룹 소유권)에 계속 쓰이지만,
     * 메뉴판 구성에는 쓰지 않는다.
     *
     * <p><b>메뉴그룹도 링크에서 읽는다</b> — 같은 메뉴가 가게마다 다른 메뉴그룹에 배치될 수 있어
     * {@code PRODUCT.product_category_id}(원본 가게에서의 배치)를 쓰면 다른 가게 메뉴판에서 엉뚱한
     * 그룹에 묶인다.
     *
     * <p>품절·노출은 메뉴가 소유하므로({@code Product.soldOut}·{@code visible}) 링크로 분리하지 않는다.
     * 링크가 1개인 메뉴는 이 조회의 결과가 이전과 완전히 동일하다 — 이 설계의 안전장치다.
     */
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

    // ── admin ──────────────────────────────────────────────────────────────

    /**
     * 관리자 상품 목록 — 조건 페이징 조회.
     */
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

    /**
     * 상품 상세 — web(간략)·admin(관리) 양쪽이 공유한다. 노출 여부와 무관하게 단건 조회한다.
     */
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

    /**
     * 메뉴 한 개의 가격 행 전체를 {@code sort} 오름차순으로 조회한다 — 손님 메뉴 상세의 가격 목록용이다.
     *
     * <p>write 포트 {@code ProductPriceRepository#findAllByProductId}와 같은 데이터를 읽지만, 이쪽은
     * <b>표현 목적</b>이라 여기에 둔다. 조회 서비스({@code *QueryService})가 write 포트를 주입하는 것은
     * ArchUnit {@code queryServicesShouldNotDependOnWritePorts}가 금지하므로, 손님 조회 경로는 이 DAO를 쓴다.
     */
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

    /**
     * 여러 메뉴의 가격 행을 한 번에 {@code sort} 오름차순으로 조회한다 — 장바구니·주문서의 배치 조회용이다.
     *
     * <p>메뉴마다 {@link #findProductPrices}를 부르면 장바구니 항목 수만큼 쿼리가 나가므로(N+1),
     * 배치 경로는 이 메서드로 한 번에 읽고 소비 측이 {@code productId}로 그룹핑한다.
     *
     * <p>가격 행이 없는 메뉴는 결과에 등장하지 않는다 — 소비 측이 빈 목록으로 다루면 되고, 그때 화면은
     * 기존 {@code PRODUCT.original_price} 경로로 표시된다(가격 행 도입 이전 데이터 호환).
     */
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

    /**
     * 가게 <b>메뉴판에 걸린</b> (삭제되지 않은) 모든 메뉴의 가격 행을 조회한다 — 매장가격 뱃지 판정 입력이다.
     *
     * <p>가격 행이 {@code shop_id}를 직접 들고 있지 않으므로 {@code PRODUCT_SHOP_LINK}로 조인해 노출
     * 가게와 소프트 삭제를 함께 판정한다(write 포트 {@code ProductPriceRepository#findAllByShopId}와
     * 동일한 조건이라야 같은 뱃지 판정이 두 경로에서 갈리지 않는다 — 한쪽만 바꾸면 손님 화면과 점주
     * 화면의 뱃지가 갈린다).
     *
     * <p>메뉴-가게 연결(N:M) 도입으로 {@code PRODUCT.shop_id}가 아니라 링크를 쓴다. 뱃지는 "이 가게에서
     * 파는 메뉴들이 매장가와 같은가"를 묻는 것이므로 판정 대상은 그 가게 메뉴판의 구성이다. 가격은
     * 연결된 가게끼리 공유되므로 가격 행 자체는 메뉴 단위 그대로다.
     */
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

    /**
     * 가게의 손님에게 노출되는(삭제되지 않은) 메뉴 수 — '매장가격 픽업' 뱃지 커버리지 판정의 <b>분모</b>다.
     *
     * <p>write 포트 {@code ProductRepository#countVisibleByShopId}와 같은 수를 세지만, 조회 서비스가
     * write 포트를 주입할 수 없어 표현 경로용으로 여기에 둔다. 두 경로의 조건({@code visible = true}
     * <b>이고</b> {@code deleted = false})은 반드시 일치해야 한다 — 어긋나면 같은 가게의 뱃지가 점주
     * 화면과 손님 화면에서 다르게 켜진다.
     */
    @Override
    public long countVisibleProducts(Long shopId) {
        Long count = queryFactory
            .select(productJpaEntity.count())
            .from(productJpaEntity)
            .where(productJpaEntity.shopId.eq(shopId), productJpaEntity.visible.isTrue(), notDeleted())
            .fetchOne();
        return count != null ? count : 0L;
    }

    /**
     * 점주 메뉴 상세 관리 화면 — 메뉴그룹명과 대표 이미지 URL까지 조인해 완성한다. 삭제 여부와 무관하게
     * 단건 조회하지 않고(관리 화면은 살아있는 메뉴만 다룸) {@link #notDeleted()}를 적용한다.
     */
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

    /**
     * 메뉴 영양성분. 미입력이면 {@code Optional.empty()}이며, 점주·손님 양쪽이 그때 {@code data: null}을
     * 내려준다.
     *
     * <p>알레르기 성분은 {@link #findAllergenTypes(Long)}로 따로 읽는다 — 1:N을 함께 조인하면 성분
     * 개수만큼 행이 늘어나 수치 14개가 중복 투영된다.
     */
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

    /**
     * 메뉴의 알레르기 유발성분 코드 목록. 미입력이면 빈 목록이다.
     *
     * <p>정렬을 {@code allergen_type} 오름차순이 아니라 <b>id 순</b>으로 두는 이유는, 성분 코드
     * 알파벳순이 법령 열거 순서와 무관해 화면 나열 순서가 고지 순서와 어긋나기 때문이다. 저장 순서를
     * 유지하면 점주가 체크한 순서(= 화면의 법령 순서)가 그대로 보인다.
     */
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

    /**
     * 가게의 <b>노출 중인</b> 상품 카테고리 목록(sort 오름차순) — 손님 메뉴판용.
     *
     * <p>관리 화면은 숨긴 그룹도 봐야 하므로 이것을 쓰지 않고
     * {@link #findProductCategoriesForManagement}를 쓴다.
     */
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

    /**
     * 점주·관리자 메뉴그룹 관리 목록 — <b>숨긴 그룹도 포함</b>하고 소속 메뉴 수를 함께 센다.
     *
     * <p>{@code visible} 필터를 걸지 않는 이유는 관리 화면이 숨김 상태 자체를 조작하는 화면이기
     * 때문이다 — 걸면 숨긴 그룹을 다시 켤 방법이 없어진다.
     *
     * <p>메뉴 수는 삭제된 메뉴를 제외한다. 이 값이 0이 아니면 그룹 삭제가
     * {@code PRODUCT_CATEGORY_HAS_PRODUCTS}로 거절되므로, 화면이 미리 안내할 수 있다.
     */
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

    /**
     * 점주 옵션그룹 관리 화면의 가게 단위 옵션그룹 목록 — <b>일반 옵션그룹만</b> 반환한다.
     *
     * <p>공통 옵션그룹({@code PRODUCT_COMMON_OPTION_GROUP})은 담지 않는다: 점주 CRUD 대상이 일반
     * 갈래뿐이고, 두 테이블의 id 공간이 독립적이라 한 목록에 섞으면 화면이 뒤이어 보내는
     * {@code optionGroupId}가 어느 테이블을 가리키는지 알 수 없어진다.
     *
     * <p><b>가게 범위는 링크를 거쳐 판정한다</b>(그룹 → 링크 → 메뉴 → 가게). 옵션그룹 행에도
     * {@code product_id}가 남아 있지만 그것은 1:N 시절의 잔재이며 N:M에서는 진실원이 아니다.
     * 삭제된 메뉴의 링크만 남은 그룹은 목록에서 사라진다 — {@code notDeleted()}가 걸리기 때문이며,
     * 이는 의도된 동작이다(그 그룹은 어느 살아있는 메뉴에서도 보이지 않는다).
     *
     * <p>{@code visible} 필터를 걸지 않는 이유는 메뉴그룹 관리 목록과 같다 — 이 화면이 숨김 상태를
     * 조작하므로, 필터를 걸면 감춘(소프트 삭제된) 그룹이 목록에서 영구히 사라져 되살릴 수 없다.
     */
    @Override
    public List<ProductOptionGroupManagementResult> findProductOptionGroupsForManagement(Long shopId) {
        // select와 Tuple.get이 같은 표현식 인스턴스를 참조해야 하므로 서브쿼리를 지역 변수로 추출한다.
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

        // 같은 그룹이 여러 메뉴에 연결돼 있으면 링크 sort가 달라 행이 여럿 나온다. 먼저 만난 행(=가장
        // 작은 sort)만 남겨 그룹당 1건으로 접는다.
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

    /**
     * 관리 화면용 옵션을 그룹별로 배치 조회한다(N+1 방지). 감춘 옵션도 포함한다 — 그룹과 같은
     * 이유로, 필터를 걸면 감춘 옵션을 되살릴 방법이 없어진다.
     */
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

    /**
     * 옵션그룹을 사용하는(삭제되지 않은) 메뉴 목록 — 연결 해제 전 영향 확인 화면에 쓴다.
     *
     * <p>메뉴의 {@code shopId}를 함께 반환하는 것이 <b>의도</b>다 — 옵션그룹은 자기 가게를 모르므로
     * 호출부가 이 값으로 소유권을 역판정한다. 결과가 비면 소유 가게를 판정할 수 없다는 뜻이므로
     * 호출부는 이를 "접근 불가"로 다뤄야 한다(빈 목록을 "허용"으로 읽으면 IDOR이 열린다).
     */
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

    /**
     * 가게 단위로 옵션그룹별 연결 메뉴 목록을 <b>한 번의 조회</b>로 반환한다 — 옵션그룹 연결 다이얼로그가
     * 후보 그룹마다 {@link #findLinkedProductsByOptionGroupId}를 개별 호출하던 N+1을 없앤다.
     *
     * <p>단일 가게 불변식(옵션그룹은 한 가게에만 속한다) 덕분에, 이 가게의 메뉴로 조인을 걸면 결과가
     * 곧 이 가게 옵션그룹 전체의 연결 목록이 된다 — 그룹마다 다시 조회할 필요가 없다.
     */
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

    /**
     * 옵션그룹 합치기 <b>추천 후보</b>를 조회한다 — 동일성 서명이 같은 그룹이 2개 이상인 묶음만 반환한다.
     *
     * <p><b>이 메서드만 네이티브 쿼리를 쓴다.</b> 판정 기준(그룹명 + min/max + 옵션명·가격 집합 동일)이
     * 파생 키 {@code GROUP BY}인데 QueryDSL이 {@code GROUP_CONCAT}을 표현하지 못한다. self-join으로
     * 바꾸면 O(n²)이라 옵션그룹이 수십 개인 가게에서 급격히 느려진다.
     *
     * <p><b>서명 해싱은 하지 않는다</b> — 원시 {@code sig_payload}만 돌려주고 SHA-256은 도메인의
     * {@code ProductOptionGroupSignature}가 계산한다(그 클래스 주석 참조).
     *
     * <p>필터의 의미
     * <ul>
     *   <li>{@code o.is_visible = 1} — 숨은(소프트 삭제된) 옵션이 동일성에 참여하면 눈에 똑같은 두
     *       그룹이 유령 옵션 하나 때문에 다르다고 판정된다.</li>
     *   <li>{@code g.is_visible = 1} — 이미 합쳐졌거나 삭제된 그룹은 추천하지 않는다.</li>
     *   <li>{@code JOIN link/product} + {@code p.is_deleted = 0} — 관리 목록과 같은 가게 스코핑이며,
     *       "삭제된 메뉴에만 걸린 그룹은 사라진다"는 기존 동작을 그대로 재현한다.
     *       {@code GROUP BY g.id}가 링크 fan-out을 접는다.</li>
     *   <li>{@code COUNT(o.id)}를 payload에 포함 — {@code group_concat_max_len} 기본 1024바이트가
     *       옵션 30개쯤에서 목록을 잘라 <b>서로 다른 그룹이 같다고 판정되는 조용한 오탐</b>을 만든다.</li>
     * </ul>
     */
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

    /**
     * 점주가 [X]로 제외한 묶음의 <b>서명 집합</b>을 반환한다.
     *
     * <p>이 조회가 write 포트가 아니라 DAO에 있는 이유는 CQRS 교차 주입 금지 때문이다 — 추천 목록을
     * 만드는 것은 query 서비스인데, 그 서비스가 exclusion write 포트를 주입하면
     * {@code queryServicesShouldNotDependOnWritePorts} 규칙을 위반한다.
     */
    @Override
    public Set<String> findOptionGroupMergeExcludedSignatures(Long shopId) {
        return Set.copyOf(queryFactory
            .select(productOptionGroupMergeExclusionJpaEntity.groupSignature)
            .from(productOptionGroupMergeExclusionJpaEntity)
            .where(productOptionGroupMergeExclusionJpaEntity.shopId.eq(shopId))
            .fetch());
    }

    /**
     * 옵션그룹 유형 enum을 표현 계층으로 넘길 문자열로 바꾼다. {@code null}은 {@code NORMAL}로 본다 —
     * 기존 행은 DDL {@code DEFAULT 'NORMAL'}로 채워지지만, 방어적으로 같은 기본값을 여기서도 쓴다.
     */
    private static String groupTypeNameOf(ProductOptionGroupType groupType) {
        return groupType == null ? ProductOptionGroupType.NORMAL.name() : groupType.name();
    }

    private static Long toLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private static Integer toInteger(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }

    // ── 품절·숨김 관리 ─────────────────────────────────────────────────────

    /**
     * 품절·숨김 관리 화면의 메뉴 탭 — 카테고리를 {@code leftJoin}(미분류 메뉴 포함)하고 대표 이미지 URL을
     * 완성한다. 손님 화면과 달리 {@code visible.eq(true)} 필터를 걸지 않는다 — 점주가 품절·숨김 상태
     * 자체를 관리하는 화면이라 숨김·품절 항목도 그대로 노출해야 한다.
     *
     * <p><b>{@code PRODUCT_SHOP_LINK}를 통해 조회한다</b> — 이 화면은 "이 가게 메뉴판"의 관리 화면이므로
     * 다른 가게에서 불러온 메뉴도 보여야 하고, 메뉴그룹·표시 순서도 <b>그 가게에서의 값</b>(링크 소유)을
     * 써야 한다. 원본 컬럼을 쓰면 연결된 가게에서 엉뚱한 그룹·순서로 보인다.
     *
     * <p>품절·숨김 자체는 메뉴가 소유하므로 링크로 분리하지 않는다 — 한 가게에서 품절하면 연결된 모든
     * 가게에서 품절이다.
     */
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

    /**
     * 품절·숨김 관리 화면의 옵션 탭 — 일반 옵션 그룹과 공통 옵션 그룹을 각각 조회해 병합한다(일반 먼저).
     * 손님 화면 조회와 달리 {@code visible.eq(true)} 필터를 걸지 않고, {@code keyword}는 옵션명을 기준으로
     * 매칭한다.
     */
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

        // 같은 옵션 그룹 id 라도 연결 메뉴가 여러 건일 수 있어(1:N) 그룹 단위로 메뉴명을 모은다.
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

    /**
     * 그룹에 속한 일반 옵션을 조회한다.
     *
     * <p>검색어·품절보기·숨김보기를 <b>옵션 단위로</b> 적용한다 — 그룹 단위로만 걸면 "치즈"를 검색했을 때
     * 치즈 옵션을 가진 그룹의 <b>모든</b> 옵션이 함께 나와 검색이 사실상 무의미해진다.
     */
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

        // 같은 옵션 그룹 id 라도 연결 메뉴가 여러 건일 수 있어(1:N) 그룹 단위로 메뉴명을 모은다.
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

    /**
     * 그룹에 속한 공통 옵션을 조회한다. 필터는 일반 옵션과 동일하게 옵션 단위로 적용한다.
     */
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

    // ── ceo/admin — 이미지·채식 승인 워크플로 ───────────────────────────────

    /**
     * 점주 메뉴 이미지 관리 목록(sort 오름차순) — 숨김 이미지도 포함한다.
     *
     * <p>{@link #findProductImageUrls}와 목적이 다르다. 그쪽은 손님 화면용이라 노출 중인 URL만
     * 내보내지만, 관리 화면은 순서 변경·삭제 대상을 지목해야 하므로 <b>이미지 식별자</b>가 필요하고
     * 숨김 상태도 보여야 한다.
     */
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

    /**
     * 이 메뉴가 이 가게 메뉴판에 걸려 있는지 — <b>메뉴-가게 소유권 판정의 기준</b>.
     *
     * <p><b>메뉴의 가게와 대상 가게를 단순 동등 비교하던 방식을 이것으로 대체했다.</b> 메뉴-가게 연결(N:M)
     * 도입 전에는 메뉴 하나에 가게가 하나라 "메뉴의 가게 == 내 가게"로 판정할 수 있었지만, 이제 한 메뉴가
     * 여러 가게에 걸리므로 그 비교는 <b>포함 관계</b>여야 한다. 동등 비교를 남기면 연결된 가게의 점주가
     * 자기 메뉴판의 메뉴를 열지 못한다.
     *
     * <p>원본 소유 가게({@code PRODUCT.shop_id})도 함께 인정한다 — 이관으로 모든 메뉴에 원본 링크가
     * 생기지만, 링크가 아직 없는 메뉴(이관 직후 새로 만들어진 행 등)에서 원본 가게 점주가 잠기는 일을
     * 막는 이중 안전장치다.
     *
     * <p>삭제된 메뉴는 없는 것으로 다룬다 — "메뉴 없음"과 "남의 가게 메뉴"를 호출부가 같은
     * {@code PRODUCT_NOT_FOUND}로 합쳐 존재 여부가 새지 않게 한다.
     */
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

    /**
     * 특정 메뉴의 이미지 변경요청 목록 — 최근 요청 순.
     */
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

    /**
     * 관리자 검수용 이미지 변경요청 페이징 목록 — 승인 상태로 필터하며 최근 요청 순.
     */
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

    /**
     * 특정 메뉴의 채식 설정 요청 목록 — 최근 요청 순.
     */
    @Override
    public List<ProductVegetarianRequestResult> findVegetarianRequests(Long productId) {
        return vegetarianRequestProjection()
            .where(productVegetarianRequestJpaEntity.productId.eq(productId))
            .orderBy(productVegetarianRequestJpaEntity.id.desc())
            .fetch();
    }

    /**
     * 관리자 검수용 채식 설정 요청 페이징 목록 — 승인 상태로 필터하며 최근 요청 순.
     */
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

    /**
     * 관리자 검수용 사장님 추천 지정 요청 페이징 목록 — 승인 상태로 필터하며 최근 요청 순.
     */
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

    /**
     * 가게 상세 상단 "가장 인기 있는 메뉴" 그룹 — <b>사장님 추천을 먼저 채우고 남는 자리를 주문
     * 데이터 기반 인기 메뉴로 채운다.</b> 최대 {@value #POPULAR_PRODUCT_LIMIT}건.
     *
     * <p><b>집계 테이블·배치를 두지 않고 실시간 조회로 처리한다.</b> 이 저장소에는 상품 판매량 집계가
     * 없었고({@code POPULAR_KEYWORD}는 검색어 전용), 가게 단위·30일 창이면 인덱스
     * ({@code idx_orders_shop_id}·{@code idx_orders_created_at})로 좁혀지는 범위라 집계 자산을 새로
     * 만들어 동기화 책임을 늘릴 이유가 없다.
     *
     * <p>집계 규칙:
     * <ul>
     *   <li>원천은 {@code ORDERS} ⨝ {@code ORDER_PRODUCT}이며 <b>{@code COMPLETED} 주문만</b> 센다 —
     *       취소·대기 주문이 인기 순위를 흔들면 안 된다.</li>
     *   <li>집계 창은 최근 {@value #POPULAR_PRODUCT_WINDOW_DAYS}일이다. 창이 없으면 오래 전 히트
     *       메뉴가 순위를 영구히 점유한다.</li>
     *   <li>순위는 {@code SUM(quantity)} 내림차순, <b>동수는 {@code product_id} 오름차순</b>으로
     *       안정 정렬한다 — 타이브레이크가 없으면 같은 데이터에도 호출마다 순서가 달라져 화면이 흔들린다.</li>
     * </ul>
     *
     * <p>두 갈래를 <b>애플리케이션에서 합치는 이유</b>는 정렬 키가 서로 다르기 때문이다. 추천 자리는
     * 판매량과 무관하게 우선하고(판매 이력이 0이어도 남는다) 나머지 자리만 판매량 순이므로, 한 쿼리의
     * {@code ORDER BY}로 표현하면 "추천이면서 판매량 0"인 항목이 뒤로 밀린다. 두 목록을 각각 뽑아
     * 순서대로 이어 붙이면 그 규칙이 그대로 드러난다.
     *
     * <p>양쪽 모두 <b>판매중지·숨김·미노출 메뉴를 제외</b>한다({@link #findShopProducts}와 같은 술어).
     * 주문할 수 없는 메뉴를 상단에 올리면 손님이 눌렀을 때 막힌다.
     */
    @Override
    public List<PopularProductItemResult> findPopularProducts(Long shopId) {
        LocalDateTime now = nowInServiceZone();

        // 1) 사장님 추천 — 판매량과 무관하게 자리를 먼저 차지한다. 표시 순서는 목록 정렬과 같게
        //    맞춘다(평점 내림차순 → id 오름차순)。
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

        // 2) 판매량 순위 — 추천으로 이미 채운 메뉴는 중복 제외한다. 제외를 SQL 술어로 넣어야
        //    limit이 정확해진다(뽑은 뒤 걸러내면 자리가 비는 만큼 결과가 모자란다).
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

    /**
     * 메뉴에 현재 반영된 채식 설정. 삭제된 메뉴는 제외하므로 비어 있으면 대상이 없는 것으로 다룬다.
     *
     * <p>{@code shopId}를 함께 담는 이유는 소비 측(ceo-api)이 <b>이 메뉴가 정말 그 가게 것인지</b>
     * 재확인해야 하기 때문이다 — 경로의 메뉴 id와 query의 가게 id가 서로를 검증하지 않으면 IDOR이 된다.
     */
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

    /**
     * 메뉴에 현재 설정된 노출기간(기간 축). 삭제된 메뉴는 제외하므로 비어 있으면 대상이 없는 것으로 다룬다.
     *
     * <p>요일·시간대 축은 판정 계산기가 도메인 모델을 필요로 하므로 write 포트({@code
     * ProductExposureHourRepository})를 통해 별도로 읽는다 — 이 투영은 기간 축과 소유 가게만 담는다.
     */
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

    /**
     * 사장님 추천 지정 요청 투영 — 검수 화면이 사진을 보고 판정하도록 대표 이미지를 함께 조인한다.
     *
     * <p>{@code shop_id}는 요청 행이 직접 들고 있으므로 메뉴를 거쳐 역조회하지 않는다. 가게명만
     * {@code SHOP}에서 가져온다.
     */
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

    /**
     * 인기 메뉴 항목 투영 — 추천 갈래와 판매량 갈래가 <b>같은 투영을 공유</b>한다. 따로 두면 한쪽만
     * 고쳐져 같은 화면의 항목이 서로 다른 필드를 갖게 된다.
     */
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

    /**
     * 최근 {@value #POPULAR_PRODUCT_WINDOW_DAYS}일간 <b>완료된</b> 주문의 판매 수량 합 서브쿼리.
     *
     * <p>{@code coalesce(0)}으로 감싸는 이유는 판매 이력이 없는 메뉴에서 스칼라 서브쿼리가 {@code NULL}이
     * 되어 정렬과 {@code > 0} 비교가 모두 예상과 달라지기 때문이다.
     *
     * <p>{@code shop_id}로 한 번 더 좁히는 것은 중복 조건이 아니다 — {@code ORDER_PRODUCT}는 가게를
     * 모르므로 {@code ORDERS}를 통해서만 가게 범위를 걸 수 있고, 이 조건이 있어야
     * {@code idx_orders_shop_id}를 탄다.
     */
    private NumberExpression<Long> soldQuantityOf(
        com.querydsl.core.types.Expression<Long> shopIdExpression,
        LocalDateTime now
    ) {
        // numberTemplate(Long.class, "{0}", ...)으로 감싸는 이유: Expressions.asNumber(subquery)는
        // 반환 타입을 Object로 지워버린다. 서브쿼리 자체는 Long을 보고하지만 asNumber를 거치면
        // getType()이 Object가 되고, Projections.constructor는 리플렉션으로 생성자를 찾으므로
        // 컴파일은 통과한 뒤 조회 시점에 "No constructor found ... class java.lang.Object"로 터진다.
        // numberTemplate은 대상 타입을 명시적으로 고정해 이 소실을 막는다.
        return com.querydsl.core.types.dsl.Expressions.numberTemplate(
            Long.class,
            "{0}",
            JPAExpressions
                // sumLong()을 쓰는 이유: 이 저장소의 QueryDSL 포크(OpenFeign 6.11)에서 sum()이
                // sumAggregate()로 개명됐고, 수량 합은 Integer 범위를 넘길 수 있어 Long 집계가 맞다.
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

    /**
     * 손님이 <b>지금 주문할 수 있는</b> 메뉴만 남기는 술어 — 노출 중 · 판매중 · 삭제되지 않음 ·
     * 노출기간·요일·시간대 안. 인기 메뉴 그룹은 상단에 올려 누르게 만드는 자리이므로, 눌렀을 때
     * 막히는 메뉴를 넣지 않는다.
     */
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

    // ── batch ──────────────────────────────────────────────────────────────

    /**
     * BBQ 옵션 동기화가 필요한 상품 1건 — 동기화 대상 식별자와 기본 옵션 생성용 상품명을 함께 투영한다.
     */
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

    /**
     * 투영된 저장 경로를 표시용 URL로 바꿔 재조립한다. {@code Projections.constructor}는 생성자 직접 투영이라
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
        // select와 Tuple.get이 같은 표현식을 참조하도록 numberPath를 지역 변수로 추출한다.
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

    /** 노출 판정 기준 시각(서비스 타임존). */
    private LocalDateTime nowInServiceZone() {
        return LocalDateTime.now(SERVICE_ZONE);
    }

    /**
     * 오늘의 할인 목록의 술어 — <b>count와 content가 이 하나를 공유</b>한다.
     *
     * <p>따로 두면 한쪽만 고쳐져 페이징 total이 어긋나고 마지막 페이지가 비는 사고가 난다.
     */
    private BooleanExpression todayDiscountSearchable(LocalDateTime now) {
        return productJpaEntity.discountInfo.discountPrice.isNotNull()
            .and(productJpaEntity.visible.eq(true))
            .and(notDeleted())
            .and(exposedNow(now));
    }

    /**
     * <b>지금 노출 중인 메뉴만</b> 남기는 SQL 술어 — 기간 축 + 요일·시간대 축.
     *
     * <p><b>애플리케이션 후처리로 할 수 없다.</b> 목록에 페이징이 걸려 있어 20건을 fetch한 뒤
     * 5건을 걸러내면 {@code totalElements}가 틀어지고 마지막 페이지가 비게 된다. 그래서 술어여야 한다.
     *
     * <p>이 술어는 {@code ProductExposureCalculator}와 <b>같은 결과를 내야 한다.</b> 규칙:
     * <ul>
     *   <li>기간: {@code start <= today}이고 {@code today <= end}. NULL이면 그 방향 제약 없음.
     *       종료일은 <b>당일 포함</b>이다.</li>
     *   <li>요일·시간대: 행이 <b>0건이면 제약 없음</b>({@code notExists}).</li>
     *   <li>행이 있으면 오늘 요일에 걸리는 행이 지금 시각을 덮거나, <b>전일 행이 자정을 넘겨</b>
     *       지금 시각을 덮어야 한다. 전일 확인을 빠뜨리면 01:00에 야식 메뉴가 사라진다.</li>
     * </ul>
     *
     * <p>{@code visible}은 이 술어에 넣지 않는다 — 기존 쿼리들이 이미 각자
     * {@code visible.eq(true)}를 걸고 있고, 관리 화면은 숨김도 봐야 하므로 축을 분리해 둔다.
     *
     * @param now 호출부가 {@code LocalDateTime.now(ZoneId.of("Asia/Seoul"))}로 넣는다.
     *            술어가 {@code CURRENT_DATE}/{@code CURRENT_TIME}를 쓰지 않는 이유는 DB 서버
     *            타임존에 판정이 좌우되지 않게 하기 위함이다.
     */
    private BooleanExpression exposedNow(LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        LocalTime time = now.toLocalTime();

        // 두 축의 OR 그룹을 각각 지역 변수로 분리한다 — 체이닝으로 이어 쓰면
        // `(A or B) and (C or D)`가 되는 것이 우연처럼 보이고, 조건을 하나 추가할 때
        // 결합 순서가 조용히 바뀐다.
        BooleanExpression startNotAfterToday = productJpaEntity.exposureStartDate.isNull()
            .or(productJpaEntity.exposureStartDate.loe(today));
        BooleanExpression endNotBeforeToday = productJpaEntity.exposureEndDate.isNull()
            .or(productJpaEntity.exposureEndDate.goe(today));
        BooleanExpression withinPeriod = startNotAfterToday.and(endNotBeforeToday);

        // 행이 0건이면 "요일·시간 제약 없음".
        BooleanExpression noHourRows = JPAExpressions
            .selectOne()
            .from(subExposureHour)
            .where(subExposureHour.productId.eq(productJpaEntity.id))
            .notExists();

        // 오늘 행이 지금을 덮는 경우 / 전일 행이 자정을 넘어와 덮는 경우.
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

    /**
     * 요일 매칭 — 요일 묶음과 개별 요일을 모두 본다. {@code HOLIDAY}는 공휴일 판정이 이 술어에
     * 없으므로 <b>제외</b>한다(손님 목록에서 공휴일 전용 메뉴는 계산기를 타는 상세 경로에서만 정확하다).
     */
    private BooleanExpression dayTypeMatches(java.time.DayOfWeek dayOfWeek) {
        boolean weekend = dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY;
        return subExposureHour.dayType.eq(DayType.DAILY)
            .or(subExposureHour.dayType.eq(weekend ? DayType.WEEKEND : DayType.WEEKDAY))
            .or(subExposureHour.dayType.eq(DayType.valueOf(dayOfWeek.name())));
    }

    /** 오늘 시작한 구간이 지금 시각을 덮는지. 종일(NULL)이면 항상 참, 자정 넘김이면 시작 이후 구간만 본다. */
    private BooleanExpression coversTime(LocalTime time) {
        BooleanExpression allDay = subExposureHour.startTime.isNull().or(subExposureHour.endTime.isNull());
        BooleanExpression sameDay = subExposureHour.startTime.loe(time)
            .and(subExposureHour.endTime.gt(time))
            .and(subExposureHour.startTime.loe(subExposureHour.endTime));
        BooleanExpression overnightHead =
            subExposureHour.endTime.lt(subExposureHour.startTime).and(subExposureHour.startTime.loe(time));
        return allDay.or(sameDay).or(overnightHead);
    }

    /** 어제 시작해 자정을 넘어온 구간의 새벽 꼬리가 지금 시각을 덮는지. */
    private BooleanExpression coversAsOvernightTail(LocalTime time) {
        return subExposureHour.startTime.isNotNull()
            .and(subExposureHour.endTime.isNotNull())
            .and(subExposureHour.endTime.lt(subExposureHour.startTime))
            .and(subExposureHour.endTime.gt(time));
    }

    /**
     * 소프트 삭제된 메뉴 제외. <b>정적 고정 조건</b>이라 동적 필터 헬퍼와 달리 절대 {@code null}을
     * 반환하지 않는다 — null을 돌려주면 QueryDSL이 조건을 통째로 무시해 필터가 조용히 사라진다.
     *
     * <p><b>모든 조회에 거는 것이 정답이 아니다.</b> 리뷰 작성 가능 항목 조회처럼 PRODUCT를
     * INNER JOIN 하는 경로에 걸면 삭제된 메뉴를 주문했던 회원의 행이 통째로 사라져 하드 삭제와
     * 같은 데이터 손실이 난다. 거는 곳/거지 않는 곳은 각 DAO의 Javadoc에 명시한다.
     */
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

    /**
     * 품절·숨김 관리 화면의 "품절 보기"/"숨김 보기" OR 조합. varargs {@code .where(...)}는 AND라 OR을
     * 표현할 수 없으므로, 하나의 {@link BooleanExpression}으로 묶어 단일 인자로 넘긴다.
     */
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

    /**
     * 조건에 맞는 일반 옵션을 <b>하나라도 가진</b> 그룹만 남긴다.
     *
     * <p>검색어와 품절·숨김 필터를 EXISTS 서브쿼리 안에서 함께 적용하므로, 조건에 맞는 옵션이 없는
     * 그룹은 목록에서 빠진다 — 항목 단위 필터만 걸면 옵션이 0개인 빈 그룹이 화면에 남는다.
     */
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

    /**
     * 조건에 맞는 공통 옵션을 하나라도 가진 그룹만 남긴다.
     */
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

    /**
     * 일반 옵션 <b>항목</b>의 이름 부분일치. 그룹 선별용 EXISTS와 짝을 이룬다 — 그룹을 고른 뒤
     * 그 안에서 실제로 일치하는 항목만 남기는 데 쓴다.
     */
    private BooleanExpression optionNameContains(String keyword) {
        return StringUtils.hasText(keyword)
            ? productOptionJpaEntity.name.containsIgnoreCase(keyword)
            : null;
    }

    /** 공통 옵션 <b>항목</b>의 이름 부분일치. */
    private BooleanExpression commonOptionNameContainsItem(String keyword) {
        return StringUtils.hasText(keyword)
            ? productCommonOptionJpaEntity.name.containsIgnoreCase(keyword)
            : null;
    }

    /** 일반 옵션 항목의 "품절 보기"/"숨김 보기" OR 조합({@link #soldOutOrHidden}의 옵션판). */
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

    /** 공통 옵션 항목의 "품절 보기"/"숨김 보기" OR 조합. */
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

    // ── @Convert VO 컬럼의 raw Long path ───────────────────────────────────

    /**
     * 배치 조회 내부 계산용 옵션 정보(개별/공통 구분 포함). DAO 밖으로 나가지 않는다.
     */
    private record BatchOptionInfo(
        Long groupId,
        String name,
        Integer additionalPrice,
        boolean common,
        Integer cupCount,
        Integer personalCupDiscountAmount
    ) {

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
