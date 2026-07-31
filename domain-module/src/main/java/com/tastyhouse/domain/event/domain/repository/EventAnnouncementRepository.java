package com.tastyhouse.domain.event.domain.repository;

import java.util.Optional;

import com.tastyhouse.domain.event.domain.model.EventAnnouncement;
import com.tastyhouse.domain.event.domain.vo.EventId;

/**
 * 이벤트 당첨자 발표 write 포트.
 *
 * <p>command 경로에서 소비되는 단건 로드·중복 검증·저장만 남긴다. {@code findByEventId}는 발표 수정 시
 * 대상 애그리거트를 로드하는 용도이고, {@code existsByEventId}는 "이벤트당 발표 1개" 불변식 검증용이므로
 * 둘 다 write 포트에 잔류한다. 발표 목록 조회(표현 목적 read)는 infrastructure-module의
 * {@code EventQueryDao}가 담당한다(CQRS 분리).
 */
public interface EventAnnouncementRepository {

    Optional<EventAnnouncement> findByEventId(EventId eventId);

    boolean existsByEventId(EventId eventId);

    EventAnnouncement save(EventAnnouncement eventAnnouncement);
}
