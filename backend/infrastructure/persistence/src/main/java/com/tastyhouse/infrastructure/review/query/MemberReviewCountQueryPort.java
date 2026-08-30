package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * review 읽기 포트(CQRS query 측 아웃바운드 포트) — <b>infra 자체 소유 계약</b>.
 *
 * <p>읽기 계약은 원칙적으로 응용 계층이 소유하지만, 이 포트는 <b>application 소비자가 하나도 없고</b>
 * infra 어댑터({@code MemberReviewCountAdapter}·{@code MemberGradeReviewCountAdapter})만 소비한다.
 * 응용 계층에 두면 아무도 쓰지 않는 계약이 계약 모듈을 부풀리므로 infra가 자체 소유한다
 * ({@code ShopNoticeRow} 선례). ArchUnit {@code LayerRulesTest#queryDaosShouldImplementQueryPorts}가
 * 이 예외를 명시적으로 허용한다.
 *
 * <p>메서드명·시그니처는 DAO의 기존 공개 표면을 그대로 전사한 것이며 조회 동작·wire 계약은 바뀌지 않는다.
 */
public interface MemberReviewCountQueryPort {

    List<MemberReviewCountResult> countReviewsByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate);
}
