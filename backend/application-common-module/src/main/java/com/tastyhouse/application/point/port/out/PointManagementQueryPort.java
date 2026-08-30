package com.tastyhouse.application.point.port.out;

import java.util.Optional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 포인트 관리 화면 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>전체 회원의 포인트 이력을 검색 조건으로 조회한다. 회원 화면 조회는 {@link PointQueryPort}가
 * 소유한다.
 *
 * <p>{@link #findBalanceByMemberId}는 두 포트가 함께 쓰는 <b>공유 메서드</b>라 양쪽에 선언만 중복한다.
 */
public interface PointManagementQueryPort {

    /** 공유 메서드 — {@link PointQueryPort}에도 같은 시그니처로 선언돼 있다. */
    Optional<PointBalanceResult> findBalanceByMemberId(Long memberId);

    PageResult<PointHistoryResult> findPointHistoryPage(PointSearchCondition condition, PageQuery pageQuery);
}
