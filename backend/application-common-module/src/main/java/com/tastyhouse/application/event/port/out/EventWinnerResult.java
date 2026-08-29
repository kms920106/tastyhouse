package com.tastyhouse.application.event.port.out;

import java.time.LocalDateTime;

/**
 * 이벤트 당첨자 조회 결과(admin 당첨자 목록).
 *
 * <p>표현 목적 read의 산출물이므로 domain(write 포트)이 아니라 이 query 패키지가 소유한다. 이관 이전에는
 * 도메인 모델을 로드해 {@code from(EventWinner)}로 조립했으나, 화면 조립용 read는 도메인을 거치지 않고
 * JPA 엔티티에서 직접 투영하도록 바꿨다.
 *
 * <p>{@code phoneNumber}는 도메인의 {@code PhoneNumber} VO가 아니라 그 값(원시 {@code String})을 담는다 —
 * JPA 엔티티의 {@code @Embedded} 값 컬럼을 그대로 투영하므로 VO 접근자({@code value()})를 거치지 않는다.
 */
public record EventWinnerResult(
    Long id,
    Long eventId,
    Integer rankNo,
    String winnerName,
    String phoneNumber,
    LocalDateTime announcedAt
) {
}
