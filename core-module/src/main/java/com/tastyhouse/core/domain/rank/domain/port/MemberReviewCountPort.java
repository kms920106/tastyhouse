package com.tastyhouse.core.domain.rank.domain.port;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 랭킹 집계용 리뷰 수 조회 출력 포트.
 *
 * <p>랭킹 확정({@code RankSettlementService})은 "기간 내 회원별 리뷰 수"를 입력으로 순위를 매기는데, 그
 * 집계 조회는 리뷰 테이블에 대한 QueryDSL 그룹 투영이라 도메인이 직접 알 수 없다. 도메인 서비스가
 * 프레임워크·infra를 모르는 상태를 유지하도록(공통 지침 패턴 1) 이 포트를 도메인에 두고,
 * infrastructure-module의 어댑터가 구현한다.
 *
 * <p>반환 순서는 구현이 보장한다 — 리뷰 수 내림차순, 동수면 마지막 작성이 이른 회원 우선, 그다음
 * 회원 ID 오름차순. 도메인 서비스는 이 순서를 그대로 순위(1위부터)로 사용한다.
 */
public interface MemberReviewCountPort {

    List<MemberReviewCount> countReviewsByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate);
}
