package com.tastyhouse.domain.event.repository;

import java.util.Optional;

import com.tastyhouse.domain.event.model.Event;
import com.tastyhouse.domain.event.vo.EventId;

/**
 * 이벤트 write 포트.
 *
 * <p>command 경로에서 소비되는 단건 로드·저장만 남긴다. 목록·검색·상세 등 표현 목적 read는 이 포트가
 * 아니라 infrastructure-module의 {@code EventQueryDao}가 담당한다(CQRS 분리).
 */
public interface EventRepository {

    Optional<Event> findById(EventId eventId);

    Event save(Event event);
}
