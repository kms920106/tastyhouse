package com.tastyhouse.domain.point.domain.repository;

import com.tastyhouse.domain.point.domain.model.PointHistory;

/**
 * 포인트 변동 이력 write 포트.
 *
 * <p>이력은 insert 전용이며, 표현 목적 조회(전체 목록·페이징·유형 필터)는 write 포트가 아니라
 * infrastructure-module의 {@code PointQueryDao}가 담당한다(공통 지침 패턴 3·4). 이력 조회는 잔액
 * 불변식 검증에 쓰이지 않으므로 write 포트에 남길 이유가 없다.
 */
public interface PointHistoryRepository {

    PointHistory save(PointHistory history);
}
