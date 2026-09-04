package com.tastyhouse.application.event.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

/**
 * 이벤트 쓰기 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code EventCommandService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p>이벤트 본체·발표 공지·당첨자는 모두 하나의 이벤트 컨텍스트를 다루는 단일 컨트롤러
 * ({@code EventApiController})의 연산이므로 연산별로 쪼개지 않고 한 포트에 모은다.
 */
@AdminApp
public interface EventCommandUseCase {

    Long createEvent(EventCreateCommand command);

    void updateEvent(EventUpdateCommand command);

    void deleteEvent(EventDeleteCommand command);

    Long createAnnouncement(EventAnnouncementCreateCommand command);

    void updateAnnouncement(EventAnnouncementUpdateCommand command);

    Long createWinner(EventWinnerCreateCommand command);

    void deleteWinner(EventWinnerDeleteCommand command);
}
