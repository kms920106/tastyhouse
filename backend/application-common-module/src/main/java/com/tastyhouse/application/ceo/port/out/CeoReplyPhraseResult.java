package com.tastyhouse.application.ceo.port.out;

import java.time.LocalDateTime;

/**
 * 자주 쓰는 문구 목록 항목 조회 결과.
 *
 * <p>반드시 {@code public}이어야 한다 — package-private이면 QueryDSL {@code Projections.constructor}가
 * {@code Class#getConstructors()}(public 생성자만 반환)에서 생성자를 찾지 못해 <b>컴파일은 통과하고 그 쿼리
 * 실행 시에만 500</b>이 난다. {@code QueryResultRecordVisibilityTest}가 이를 가드한다.
 *
 * <p>{@code ceoId}는 투영하지 않는다 — 토큰의 점주 것만 조회하므로 응답에 되돌려 줄 의미가 없다.
 *
 * <p><b>표시명({@code displayName})도 여기 담지 않는다.</b> "이름이 비면 내용 앞부분"은 화면 규칙이라
 * 소비 측 {@code CeoReplyPhraseQueryService}의 private 매퍼가 계산한다.
 */
public record CeoReplyPhraseResult(
    Long id,
    String name,
    String content,
    Integer sort,
    LocalDateTime createdAt
) {
}
