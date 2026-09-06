package com.tastyhouse.domain.shop.repository;

import com.tastyhouse.domain.shop.model.ShopChangeHistory;

/**
 * 가게 변경이력 write 포트.
 *
 * <p>append-only 이력이라 저장만 필요하다. 조회는 CQRS query 측
 * {@code ShopChangeHistoryQueryDao}(infrastructure-module)가 담당하므로 이 포트에 두지 않는다.
 */
public interface ShopChangeHistoryRepository {

    ShopChangeHistory save(ShopChangeHistory shopChangeHistory);
}
