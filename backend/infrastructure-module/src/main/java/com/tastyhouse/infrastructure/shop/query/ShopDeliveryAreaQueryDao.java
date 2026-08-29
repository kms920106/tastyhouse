package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaQueryPort;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaItemResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaPolygonResult;
import com.tastyhouse.application.shop.port.out.ShopLocationResult;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static com.tastyhouse.infrastructure.region.persistence.QAdminDongJpaEntity.adminDongJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopDeliveryAreaJpaEntity.shopDeliveryAreaJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopDeliveryAreaPolygonJpaEntity.shopDeliveryAreaPolygonJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopDeliveryTipRegionJpaEntity.shopDeliveryTipRegionJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;

/**
 * 가게 배달가능지역 read 어댑터(CQRS query 측).
 *
 * <p>배달가능지역 행에 행정동 마스터를 조인해 표시용 이름까지 완성한 Result로 직접 투영한다. 소비 모듈
 * (ceo-api)의 {@code ShopDeliveryAreaQueryService}가 이 DAO를 주입하므로 api 모듈은 QueryDSL을 알지 않는다.
 */
@Repository
public class ShopDeliveryAreaQueryDao implements ShopDeliveryAreaQueryPort {

    private final JPAQueryFactory queryFactory;

    public ShopDeliveryAreaQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 가게의 배달가능지역 목록. 등록 순(id 오름차순)으로 내려준다.
     */
    @Override
    public List<ShopDeliveryAreaItemResult> findDeliveryAreas(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopDeliveryAreaItemResult.class,
                shopDeliveryAreaJpaEntity.id,
                shopDeliveryAreaJpaEntity.adminDongId,
                regionName(),
                shopDeliveryAreaJpaEntity.source.stringValue()
            ))
            .from(shopDeliveryAreaJpaEntity)
            .join(adminDongJpaEntity).on(shopDeliveryAreaJpaEntity.adminDongId.eq(adminDongJpaEntity.id))
            .where(shopDeliveryAreaJpaEntity.shopId.eq(shopId))
            .orderBy(shopDeliveryAreaJpaEntity.id.asc())
            .fetch();
    }

    /** 가게에 등록된 행정동 식별자 집합. 미리보기가 "이미 등록됨"을 표시하는 데 쓴다. */
    @Override
    public Set<Long> findAdminDongIds(Long shopId) {
        return Set.copyOf(queryFactory
            .select(shopDeliveryAreaJpaEntity.adminDongId)
            .from(shopDeliveryAreaJpaEntity)
            .where(shopDeliveryAreaJpaEntity.shopId.eq(shopId))
            .fetch());
    }

    /** 가게에 등록된 행정동 식별자 중 특정 출처의 것만. 도형 미리보기가 "닫히는 동"을 계산하는 데 쓴다. */
    @Override
    public Set<Long> findAdminDongIdsBySource(Long shopId, String source) {
        return Set.copyOf(queryFactory
            .select(shopDeliveryAreaJpaEntity.adminDongId)
            .from(shopDeliveryAreaJpaEntity)
            .where(
                shopDeliveryAreaJpaEntity.shopId.eq(shopId),
                shopDeliveryAreaJpaEntity.source.stringValue().eq(source)
            )
            .fetch());
    }

    /**
     * 점주가 소유한 가게의 좌표를 조회한다.
     *
     * <p><b>소유권을 조회 조건({@code ceo_id})으로 함께 건다.</b> 조회 서비스는 write 포트를 주입할 수
     * 없어({@code queryServicesShouldNotDependOnWritePorts}) {@code ShopOwnershipValidator}를 쓸 수 없는데,
     * 검증을 생략하면 남의 가게 좌표를 읽는 IDOR이 된다. 조건을 쿼리에 넣으면 소유하지 않은 가게는 결과가
     * 비어 자연스럽게 차단된다.
     *
     * <p>소유 가게가 아니거나 좌표가 없으면 예외를 던진다 — 좌표 없이는 7km 상한의 기준점이 없어 미리보기
     * 자체가 성립하지 않는다.
     */
    @Override
    public ShopLocationResult findShopLocation(Long ceoId, Long shopId) {
        ShopLocationResult result = queryFactory
            .select(Projections.constructor(ShopLocationResult.class,
                shopJpaEntity.id,
                shopJpaEntity.latitude,
                shopJpaEntity.longitude
            ))
            .from(shopJpaEntity)
            .where(shopJpaEntity.id.eq(shopId), shopJpaEntity.ceoId.eq(ceoId))
            .fetchOne();

        if (result == null) {
            throw new BusinessException(ErrorCode.SHOP_ACCESS_DENIED);
        }
        if (result.latitude() == null || result.longitude() == null) {
            throw new BusinessException(
                ErrorCode.SHOP_DELIVERY_AREA_RADIUS_EXCEEDED,
                "가게 좌표가 등록돼 있지 않아 배달지역을 설정할 수 없습니다."
            );
        }
        return result;
    }

    /**
     * 가게의 저장된 배달지역 도형. <b>미설정은 정상 상태</b>이므로 빈 {@code Optional}을 반환하고, 404
     * 판단은 호출 측이 하지 않는다(이 도메인에서 미설정은 오류가 아니다).
     */
    @Override
    public Optional<ShopDeliveryAreaPolygonResult> findPolygon(Long shopId) {
        return Optional.ofNullable(queryFactory
            .select(Projections.constructor(ShopDeliveryAreaPolygonResult.class,
                shopDeliveryAreaPolygonJpaEntity.id,
                shopDeliveryAreaPolygonJpaEntity.rings,
                shopDeliveryAreaPolygonJpaEntity.centerLatitude,
                shopDeliveryAreaPolygonJpaEntity.centerLongitude,
                shopDeliveryAreaPolygonJpaEntity.maxRadiusMeters,
                shopDeliveryAreaPolygonJpaEntity.ringCount,
                shopDeliveryAreaPolygonJpaEntity.vertexCount,
                shopDeliveryAreaPolygonJpaEntity.updatedAt
            ))
            .from(shopDeliveryAreaPolygonJpaEntity)
            .where(shopDeliveryAreaPolygonJpaEntity.shopId.eq(shopId))
            .fetchOne());
    }

    /**
     * 지역별 배달팁이 참조하는 행정동 식별자 집합. 미리보기가 "닫을 수 없는 동"을 미리 보여주는 데 쓴다.
     */
    @Override
    public Set<Long> findRegionTipAdminDongIds(Long shopId) {
        return Set.copyOf(queryFactory
            .select(shopDeliveryTipRegionJpaEntity.adminDongId)
            .from(shopDeliveryTipRegionJpaEntity)
            .where(shopDeliveryTipRegionJpaEntity.shopId.eq(shopId))
            .fetch());
    }

    /**
     * 표시용 행정동 전체 이름({@code "서울특별시 강남구 역삼1동"})을 SQL에서 조립한다. 세 컬럼이 전부
     * NOT NULL이라 구분자 처리 분기가 없어 단순 {@code concat} 체인으로 충분하다.
     */
    private StringExpression regionName() {
        return adminDongJpaEntity.sidoName
            .concat(Expressions.asString(" "))
            .concat(adminDongJpaEntity.sigunguName)
            .concat(Expressions.asString(" "))
            .concat(adminDongJpaEntity.dongName);
    }
}
