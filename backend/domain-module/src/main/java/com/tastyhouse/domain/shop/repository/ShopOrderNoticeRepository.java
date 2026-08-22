package com.tastyhouse.domain.shop.repository;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopOrderNotice;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 주문안내 write 포트.
 *
 * <p>표시용 조회는 infrastructure-module의 {@code shop/query/ShopOrderNoticeQueryDao}가 담당한다.
 * {@link #findByShopId(ShopId)}가 write 포트에 남는 이유는 <b>PUT이 전체교체(upsert) 의미론</b>이기
 * 때문이다 — 저장 전에 기존 행이 있는지 확인해 있으면 본문만 갱신해야 하며, 이는 표시용 투영이 아니라
 * command 경로의 선행 조회다. 관리자 게시중단 경로도 같은 조회를 쓴다({@code shopId} 하나로 대상이
 * 특정되므로 별도 {@code findById}가 필요하지 않다 — {@code SHOP_ORDER_NOTICE}의 {@code shop_id}에
 * 유니크 제약이 있어 가게당 1건이 DB로 보장된다).
 */
public interface ShopOrderNoticeRepository {

    ShopOrderNotice save(ShopOrderNotice shopOrderNotice);

    /**
     * 가게의 주문안내 1건. 미설정이면 빈 값이다.
     */
    Optional<ShopOrderNotice> findByShopId(ShopId shopId);
}
