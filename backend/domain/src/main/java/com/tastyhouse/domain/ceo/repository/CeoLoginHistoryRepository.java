package com.tastyhouse.domain.ceo.repository;

import com.tastyhouse.domain.ceo.model.CeoLoginHistory;

/**
 * 점주 로그인 이력 write 포트.
 *
 * <p>append-only 이력이라 저장만 필요하다. 조회는 CQRS query 측
 * {@code CeoLoginHistoryQueryDao}(infrastructure-module)가 담당하므로 이 포트에 두지 않는다.
 */
public interface CeoLoginHistoryRepository {

    CeoLoginHistory save(CeoLoginHistory ceoLoginHistory);
}
