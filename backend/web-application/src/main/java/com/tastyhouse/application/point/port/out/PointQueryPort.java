package com.tastyhouse.application.point.port.out;

import java.util.List;
import java.util.Optional;

/**
 * 포인트 조회 포트(CQRS query 측 아웃바운드 포트) — 회원 화면용.
 *
 * <p>회원이 자기 잔액과 적립·사용 내역을 보는 조회를 담당한다. 관리 화면 조회는
 * {@code PointManagementQueryPort}가 소유한다.
 *
 * <p>{@link #findBalanceByMemberId}는 두 포트가 함께 쓰는 <b>공유 메서드</b>라 양쪽에 선언만 중복한다.
 */
public interface PointQueryPort {

    /** 공유 메서드 — {@code PointManagementQueryPort}에도 같은 시그니처로 선언돼 있다. */
    Optional<PointBalanceResult> findBalanceByMemberId(Long memberId);

    List<PointHistoryResult> findPointHistories(Long memberId);
}
