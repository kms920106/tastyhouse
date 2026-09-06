package com.tastyhouse.domain.shop.repository;

import com.tastyhouse.domain.shop.model.ShopCeoAssignmentHistory;

/**
 * 가게-점주 접근권한 이력 write 포트.
 *
 * <p>append-only 이력이라 저장만 필요하다. 조회는 CQRS query 측
 * {@code ShopCeoAssignmentHistoryQueryDao}(infrastructure-module)가 담당하므로 이 포트에 두지 않는다.
 */
public interface ShopCeoAssignmentHistoryRepository {

    ShopCeoAssignmentHistory save(ShopCeoAssignmentHistory shopCeoAssignmentHistory);
}
