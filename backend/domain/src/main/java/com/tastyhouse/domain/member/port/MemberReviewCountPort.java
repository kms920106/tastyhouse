package com.tastyhouse.domain.member.port;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 등급 산정용 회원별 리뷰 수 조회 출력 포트.
 *
 * <p>등급 확정({@code GradeSettlementService})은 "전체 기간 회원별 리뷰 수"를 입력으로 등급을 판정하는데,
 * 그 집계 조회는 리뷰 테이블에 대한 QueryDSL 그룹 투영이라 도메인이 직접 알 수 없다. 도메인 서비스가
 * 프레임워크·infra를 모르는 상태를 유지하도록(공통 지침 패턴 1) 이 포트를 도메인에 두고,
 * infrastructure-module의 어댑터가 구현한다.
 *
 * <p><strong>rank 컨텍스트의 동명 포트({@code com.tastyhouse.domain.rank.port.MemberReviewCountPort})와
 * 통합하지 않는다.</strong> rank는 이미 {@code member.vo.MemberId}를 참조하므로, member가 rank의 포트를
 * 쓰면 {@code member ↔ rank} 컨텍스트 순환이 생긴다(컨텍스트 경계 규칙). 같은 집계 SQL을 두 벌 쓰지
 * 않도록 <em>구현</em>은 infrastructure의 {@code MemberReviewCountQueryDao} 하나를 공유하고, 어댑터만
 * 컨텍스트별로 둔다 — 중복되는 것은 얇은 변환 어댑터뿐이다.
 */
public interface MemberReviewCountPort {

    /**
     * 기간 내 회원별 리뷰 수를 집계한다.
     */
    List<MemberReviewCount> countReviewsByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate);
}
