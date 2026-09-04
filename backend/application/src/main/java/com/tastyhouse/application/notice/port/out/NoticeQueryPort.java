package com.tastyhouse.application.notice.port.out;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 공지사항 회원 노출 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>노출(visible=true) 공지만 조회한다. 비노출을 포함한 관리 조회는
 * {@code NoticeManagementQueryPort}가 소유한다 — 공유 메서드가 0개라 소비 앱별로 깨끗하게 갈린다.
 */
public interface NoticeQueryPort {

    /**
     * 회원 노출 목록 조회 — 노출(visible=true) 공지만 조회한다.
     */
    PageResult<NoticeListItemResult> findVisibleNotices(PageQuery pageQuery);
}
