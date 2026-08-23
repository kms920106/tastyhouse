package com.tastyhouse.infrastructure.product.query;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import static com.tastyhouse.infrastructure.product.persistence.QProductCategoryJpaEntity.productCategoryJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductShopLinkJpaEntity.productShopLinkJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;

/**
 * 메뉴-가게 연결 화면 전용 read 어댑터.
 *
 * <p><b>왜 {@code ProductQueryDao}에 메서드를 더하지 않는가</b>: 그 클래스는 이미 2000줄이 넘는다.
 * 연결 화면은 "점주 소유 가게 × 이 메뉴의 연결 여부"라는 자체 조인 그래프를 갖는 독립 조회 대상이라
 * 별 파일로 두면 그 형태가 한눈에 보인다({@code StorePriceVerificationQueryDao}가 같은 이유로 분리돼 있다).
 */
@Repository
public class ProductShopLinkQueryDao {

    private final JPAQueryFactory queryFactory;

    public ProductShopLinkQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 점주가 소유한 <b>전체 가게</b>와 각각에 대한 이 메뉴의 연결 여부를 조회한다.
     *
     * <p>연결된 가게만 내려보내면 화면이 새 가게를 켤 수 없다 — 토글 목록의 원천이므로 소유 가게를
     * 모두 담고 {@code linked}로 상태만 구분한다.
     *
     * <p>연결 링크와 그 링크가 가리키는 메뉴그룹을 {@code left join}으로 붙인다 — 연결되지 않은 가게는
     * 링크가 없고, 연결됐더라도 메뉴그룹이 비어 있을 수 있어 어느 쪽도 행을 떨어뜨려서는 안 된다.
     */
    public List<ProductShopLinkResult> findOwnedShopLinks(Long ceoId, Long productId) {
        return queryFactory
            .select(new QProductShopLinkResult(
                shopJpaEntity.id,
                shopJpaEntity.name,
                productShopLinkJpaEntity.productCategoryId,
                productCategoryJpaEntity.name,
                productShopLinkJpaEntity.id.isNotNull()
            ))
            .from(shopJpaEntity)
            .leftJoin(productShopLinkJpaEntity)
            .on(
                productShopLinkJpaEntity.shopId.eq(shopJpaEntity.id),
                productShopLinkJpaEntity.productId.eq(productId)
            )
            .leftJoin(productCategoryJpaEntity)
            .on(productCategoryJpaEntity.id.eq(productShopLinkJpaEntity.productCategoryId))
            .where(shopJpaEntity.ceoId.eq(ceoId))
            .orderBy(shopJpaEntity.id.asc())
            .fetch();
    }

    /**
     * 점주가 소유한 가게 ID 전부.
     *
     * <p>연결 변경이 <b>본인 소유 가게에만</b> 허용되는지 판정하는 근거다. 도메인 서비스는 {@code ceoId}를
     * 알지 못하므로(소유권은 ceo-api의 인가 관심사다) 호출부가 이 집합을 구해 넘긴다.
     *
     * <p>가게마다 {@code ShopOwnershipValidator}를 반복 호출하지 않는 이유는 연결 목록이 여러 건이라
     * 그만큼 가게 조회가 늘기 때문이다 — 한 번에 읽어 집합으로 대조한다.
     */
    public List<Long> findOwnedShopIds(Long ceoId) {
        return queryFactory
            .select(shopJpaEntity.id)
            .from(shopJpaEntity)
            .where(shopJpaEntity.ceoId.eq(ceoId))
            .fetch();
    }
}
