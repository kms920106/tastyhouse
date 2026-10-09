package com.tastyhouse.infrastructure.jpa.product.query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.PopularProductItemResult;
import com.tastyhouse.application.product.port.out.ProductExposureWindow;
import com.tastyhouse.application.product.port.out.ProductStorefrontQueryPort;
import com.tastyhouse.application.product.port.out.SearchProductItemResult;
import com.tastyhouse.application.product.port.out.ShopProductItemResult;
import com.tastyhouse.application.product.port.out.TodayDiscountProductResult;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.infrastructure.jpa.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.jpa.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.jpa.order.persistence.QOrderJpaEntity.orderJpaEntity;
import static com.tastyhouse.infrastructure.jpa.order.persistence.QOrderProductJpaEntity.orderProductJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductImageJpaEntity.productImageJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductJpaEntity.productJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductShopLinkJpaEntity.productShopLinkJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopJpaEntity.shopJpaEntity;

@Repository
class ProductStorefrontQueryAdapter implements ProductStorefrontQueryPort {

    private static final com.tastyhouse.infrastructure.jpa.product.persistence.QProductExposureHourJpaEntity
        subExposureHour =
        new com.tastyhouse.infrastructure.jpa.product.persistence.QProductExposureHourJpaEntity("subExposureHour");

    private static final int POPULAR_PRODUCT_LIMIT = 5;

    private static final long POPULAR_PRODUCT_WINDOW_DAYS = 30L;

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ProductStorefrontQueryAdapter(
        JPAQueryFactory queryFactory,
        FileUrlResolver fileUrlResolver
    ) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
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
            .leftJoin(productImageJpaEntity).on(ProductQueryPredicates.representativeImageOf(productJpaEntity.id))
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
            .and(ProductQueryPredicates.notDeleted())
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
            .leftJoin(productImageJpaEntity).on(ProductQueryPredicates.representativeImageOf(productJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(searchable)
            .orderBy(productJpaEntity.representative.desc().nullsLast(), productJpaEntity.rating.desc().nullsLast())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
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
            .leftJoin(productImageJpaEntity).on(ProductQueryPredicates.representativeImageOf(productJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(productShopLinkJpaEntity.shopId.eq(shopId), productJpaEntity.visible.eq(true), ProductQueryPredicates.notDeleted(),
                exposedNow(window))
            .orderBy(
                productJpaEntity.representative.desc(),
                productJpaEntity.rating.desc(),
                productJpaEntity.id.asc()
            )
            .fetch();
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
            .leftJoin(productImageJpaEntity).on(ProductQueryPredicates.representativeImageOf(productJpaEntity.id))
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
            .and(ProductQueryPredicates.notDeleted())
            .and(exposedNow(window));
    }

    private BooleanExpression todayDiscountSearchable(ProductExposureWindow window) {
        return productJpaEntity.discountInfo.discountPrice.isNotNull()
            .and(productJpaEntity.visible.eq(true))
            .and(ProductQueryPredicates.notDeleted())
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
}
