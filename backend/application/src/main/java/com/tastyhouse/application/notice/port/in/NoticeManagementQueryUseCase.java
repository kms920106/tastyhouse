package com.tastyhouse.application.notice.port.in;

import com.tastyhouse.application.notice.port.out.NoticeDetailResult;
import com.tastyhouse.application.notice.port.out.NoticeManagementListItemResult;
import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 공지사항 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code NoticeQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p><b>챕터 06</b> — 반환 타입은 Swagger를 아는 {@code *Response}가 아니라 프레임워크-프리
 * {@code *Result}다. Response 조립과 {@code PaginationResponse} 매핑은 컨트롤러가 담당한다.
 */
@AdminApp
public interface NoticeManagementQueryUseCase {

    PageResult<NoticeManagementListItemResult> getNotices(String title, String content, Boolean visible, int page, int size);

    NoticeDetailResult getNotice(Long id);
}
