package com.tastyhouse.application.event.port.out;

import java.time.LocalDateTime;

/**
 * 이벤트 당첨자 발표 조회 결과.
 *
 * <p>표현 목적 read의 산출물이므로 domain(write 포트)이 아니라 이 query 패키지가 소유한다. admin 단건
 * 조회({@code findAnnouncementByEventId})와 web 전체 목록({@code findAnnouncements}) 둘 다 이 result를
 * 공유한다 — 두 소비자가 쓰는 필드 셋이 동일해 분리할 이유가 없다(web은 {@code eventId}만 응답에서
 * 제외하고 나머지를 그대로 쓴다).
 */
public record EventAnnouncementResult(
    Long id,
    Long eventId,
    String name,
    String content,
    LocalDateTime announcedAt
) {
}
