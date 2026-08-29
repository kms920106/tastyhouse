package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.application.shop.port.out.ShopRiderGuideQueryPort;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideHistoryResult;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideListItemResult;
import com.tastyhouse.application.shop.port.out.ShopRiderGuidePickupPresenceResult;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideResult;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopRiderGuideHistoryJpaEntity.shopRiderGuideHistoryJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopRiderGuideJpaEntity.shopRiderGuideJpaEntity;

/**
 * 라이더 안내 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로 write
 * 포트({@code ShopRiderGuideRepository})와 역할이 겹치지 않는다 — 그 포트에는 도메인 불변식 판정에
 * 필요한 3개 메서드만 남기고, 관리자 목록·이력 조회는 이 DAO가 소유한다.
 *
 * <p><b>고객 비노출 보장</b>: 이 DAO는 ceo-api·admin-api의 query 서비스만 주입해 사용하며, web-api의
 * 가게 상세·목록 조회는 {@code SHOP_RIDER_GUIDE}를 조인하지 않는다.
 */
@Repository
public class ShopRiderGuideQueryDao implements ShopRiderGuideQueryPort {

    /**
     * 관리자 검수 화면의 이력 노출 상한. 그 이상은 별도 페이징 엔드포인트를 두지 않고 필요해지면 추가한다.
     */
    private static final int HISTORY_LIMIT = 20;

    private final JPAQueryFactory queryFactory;

    public ShopRiderGuideQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 가게 단건의 라이더 안내 — 아직 등록 이력이 없어도 가게가 존재하면 결과를 반환한다("미등록"은
     * 오류가 아니라 정상 상태이므로 라이더 안내 테이블을 left join 한다).
     */
    @Override
    public Optional<ShopRiderGuideResult> findRiderGuide(Long shopId) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(ShopRiderGuideResult.class,
                    shopJpaEntity.id,
                    shopJpaEntity.name,
                    shopRiderGuideJpaEntity.visitGuide,
                    shopRiderGuideJpaEntity.pickupRoadAddress,
                    shopRiderGuideJpaEntity.pickupLotAddress,
                    shopRiderGuideJpaEntity.pickupDetailAddress,
                    shopRiderGuideJpaEntity.pickupLatitude,
                    shopRiderGuideJpaEntity.pickupLongitude,
                    shopJpaEntity.roadAddress,
                    shopJpaEntity.lotAddress,
                    shopJpaEntity.latitude,
                    shopJpaEntity.longitude,
                    shopRiderGuideJpaEntity.updatedAt
                ))
                .from(shopJpaEntity)
                .leftJoin(shopRiderGuideJpaEntity).on(shopRiderGuideJpaEntity.shopId.eq(shopJpaEntity.id))
                .where(shopJpaEntity.id.eq(shopId))
                .fetchFirst()
        );
    }

    /**
     * 라이더 안내가 등록된 가게 목록 — 최근 변경분부터 검수하는 실제 운영 순서를 따라
     * {@code updatedAt} 내림차순으로 정렬한다.
     */
    @Override
    public PageResult<ShopRiderGuideListItemResult> findRiderGuidePage(
        String shopName,
        Boolean hasVisitGuide,
        PageQuery pageQuery
    ) {
        Long total = queryFactory
            .select(shopRiderGuideJpaEntity.count())
            .from(shopRiderGuideJpaEntity)
            .join(shopJpaEntity).on(shopJpaEntity.id.eq(shopRiderGuideJpaEntity.shopId))
            .where(
                shopNameContains(shopName),
                visitGuidePresenceEq(hasVisitGuide)
            )
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        // 픽업 위치 설정 여부는 SQL 술어를 select 절에 투영하지 않고 원본 컬럼을 읽어 Java에서 판정한다 —
        // 판정 기준(도로명·위경도가 모두 채워졌는가)을 ShopRiderGuide#hasPickupLocation과 한 곳에서 일치시키기
        // 위함이다.
        List<ShopRiderGuideListItemResult> content = queryFactory
            .select(Projections.constructor(ShopRiderGuidePickupPresenceResult.class,
                shopRiderGuideJpaEntity.shopId,
                shopJpaEntity.name,
                shopRiderGuideJpaEntity.visitGuide,
                shopRiderGuideJpaEntity.pickupRoadAddress,
                shopRiderGuideJpaEntity.pickupLatitude,
                shopRiderGuideJpaEntity.pickupLongitude,
                shopRiderGuideJpaEntity.updatedAt
            ))
            .from(shopRiderGuideJpaEntity)
            .join(shopJpaEntity).on(shopJpaEntity.id.eq(shopRiderGuideJpaEntity.shopId))
            .where(
                shopNameContains(shopName),
                visitGuidePresenceEq(hasVisitGuide)
            )
            .orderBy(shopRiderGuideJpaEntity.updatedAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(ShopRiderGuidePickupPresenceResult::toListItem)
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    /**
     * 가게별 라이더 안내 변경 이력 — 최근 {@value #HISTORY_LIMIT}건까지 최신순으로 반환한다.
     */
    @Override
    public List<ShopRiderGuideHistoryResult> findHistories(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopRiderGuideHistoryResult.class,
                shopRiderGuideHistoryJpaEntity.id,
                shopRiderGuideHistoryJpaEntity.actorType,
                shopRiderGuideHistoryJpaEntity.actorId,
                shopRiderGuideHistoryJpaEntity.actionType,
                shopRiderGuideHistoryJpaEntity.previousVisitGuide,
                shopRiderGuideHistoryJpaEntity.newVisitGuide,
                shopRiderGuideHistoryJpaEntity.reason,
                shopRiderGuideHistoryJpaEntity.createdAt
            ))
            .from(shopRiderGuideHistoryJpaEntity)
            .where(shopRiderGuideHistoryJpaEntity.shopId.eq(shopId))
            .orderBy(shopRiderGuideHistoryJpaEntity.createdAt.desc(), shopRiderGuideHistoryJpaEntity.id.desc())
            .limit(HISTORY_LIMIT)
            .fetch();
    }

    private BooleanExpression shopNameContains(String shopName) {
        return shopName == null || shopName.isBlank() ? null : shopJpaEntity.name.contains(shopName);
    }

    /**
     * {@code hasVisitGuide=true}면 문구가 등록된 가게만, {@code false}면 문구 없이 픽업 위치만 설정된
     * 가게만 조회한다. 미지정이면 둘 다 포함한다.
     *
     * <p>{@code false}를 "미지정과 동일"로 두면 그 값이 응답을 전혀 바꾸지 않아 파라미터가 무의미해지고,
     * "문구 미등록 가게만" 조회할 방법도 사라진다. 다른 boolean 필터와 동일하게 여집합으로 판정한다.
     */
    private BooleanExpression visitGuidePresenceEq(Boolean hasVisitGuide) {
        if (hasVisitGuide == null) {
            return null;
        }
        return hasVisitGuide
            ? shopRiderGuideJpaEntity.visitGuide.isNotNull()
            : shopRiderGuideJpaEntity.visitGuide.isNull();
    }
}
