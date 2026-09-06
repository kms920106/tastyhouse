package com.tastyhouse.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductShopLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 메뉴 ↔ 가게 연결 write 포트.
 *
 * <p>{@code existsByProductIdAndShopId}가 <b>권한 판정의 근거</b>다. 연결 도입 전에는 메뉴 하나에
 * 가게가 하나라 "메뉴의 가게 == 내 가게"라는 <b>동등 비교</b>로 소유권을 판정했지만, 이제 한 메뉴가
 * 여러 가게에 걸리므로 그 비교는 <b>포함 관계</b>여야 한다. 동등 비교를 남겨 두면 연결된 가게의
 * 점주가 자기 메뉴판의 메뉴를 만지지 못하고, 반대로 첫 링크만 보고 통과시키면 남의 메뉴를 만질 수 있다.
 *
 * <p>{@code countByProductId}는 "마지막 연결은 해제할 수 없다" 판정에 쓴다 — 링크가 0개가 되면
 * 그 메뉴는 어느 메뉴판에도 없으면서 삭제되지도 않은 유령이 된다.
 */
public interface ProductShopLinkRepository {

    ProductShopLink save(ProductShopLink link);

    Optional<ProductShopLink> findByProductIdAndShopId(ProductId productId, ShopId shopId);

    /** 이 메뉴가 연결된 가게들. 연결 목록 화면과 "마지막 연결" 판정에 쓴다. */
    List<ProductShopLink> findAllByProductId(ProductId productId);

    /** 이 가게 메뉴판에 노출되는 메뉴들의 링크. 메뉴그룹 이동·정렬 판정에 쓴다. */
    List<ProductShopLink> findAllByShopId(ShopId shopId);

    /**
     * 이 메뉴가 이 가게에 연결돼 있는지 — <b>소유권 판정의 기준</b>.
     * 동등 비교가 아니라 포함 관계로 판정해야 하는 이유는 이 인터페이스 주석 참조.
     */
    boolean existsByProductIdAndShopId(ProductId productId, ShopId shopId);

    /** 이 메뉴의 연결 개수. "최소 1개 유지" 판정에 쓴다. */
    long countByProductId(ProductId productId);

    void delete(ProductShopLink link);
}
