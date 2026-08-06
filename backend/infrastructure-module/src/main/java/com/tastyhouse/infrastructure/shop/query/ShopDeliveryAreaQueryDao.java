package com.tastyhouse.infrastructure.shop.query;

import java.util.List;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import static com.tastyhouse.infrastructure.region.persistence.QAdminDongJpaEntity.adminDongJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopDeliveryAreaJpaEntity.shopDeliveryAreaJpaEntity;

/**
 * 가게 배달가능지역 read 어댑터(CQRS query 측).
 *
 * <p>배달가능지역 행에 행정동 마스터를 조인해 표시용 이름까지 완성한 Result로 직접 투영한다. 소비 모듈
 * (ceo-api)의 {@code ShopDeliveryAreaQueryService}가 이 DAO를 주입하므로 api 모듈은 QueryDSL을 알지 않는다.
 */
@Repository
public class ShopDeliveryAreaQueryDao {

    private final JPAQueryFactory queryFactory;

    public ShopDeliveryAreaQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 가게의 배달가능지역 목록. 등록 순(id 오름차순)으로 내려준다.
     */
    public List<ShopDeliveryAreaItemResult> findDeliveryAreas(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopDeliveryAreaItemResult.class,
                shopDeliveryAreaJpaEntity.id,
                shopDeliveryAreaJpaEntity.adminDongId,
                regionName()
            ))
            .from(shopDeliveryAreaJpaEntity)
            .join(adminDongJpaEntity).on(shopDeliveryAreaJpaEntity.adminDongId.eq(adminDongJpaEntity.id))
            .where(shopDeliveryAreaJpaEntity.shopId.eq(shopId))
            .orderBy(shopDeliveryAreaJpaEntity.id.asc())
            .fetch();
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
