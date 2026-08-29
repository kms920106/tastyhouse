package com.tastyhouse.adminapi.notice.application.port.in;

/**
 * 공지사항 쓰기 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code NoticeCommandService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface NoticeCommandUseCase {

    Long createNotice(NoticeCreateCommand command);

    void updateNotice(NoticeUpdateCommand command);

    void deleteNotice(NoticeDeleteCommand command);
}
