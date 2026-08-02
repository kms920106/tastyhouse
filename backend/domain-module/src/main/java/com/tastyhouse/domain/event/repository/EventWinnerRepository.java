package com.tastyhouse.domain.event.repository;

import java.util.Optional;

import com.tastyhouse.domain.event.model.EventWinner;

/**
 * 이벤트 당첨자 write 포트.
 *
 * <p>command 경로에서 소비되는 단건 로드·저장만 남긴다. 당첨자 목록 조회(표현 목적 read)는 이 포트가
 * 아니라 infrastructure-module의 {@code EventQueryDao}가 담당한다(CQRS 분리).
 */
public interface EventWinnerRepository {

    Optional<EventWinner> findById(Long id);

    EventWinner save(EventWinner eventWinner);
}
