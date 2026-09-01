package com.tastyhouse.application.notice.port.out;

import java.util.Optional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 공지사항 관리 화면 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>비노출 공지를 포함한 관리 조회를 담당한다. 회원 노출 조회는 {@code NoticeQueryPort}가 소유한다.
 */
public interface NoticeManagementQueryPort {

    /**
     * 관리 목록 조회 — 비노출 공지를 포함하며 title/content 부분일치·visible 필터를 적용한다.
     */
    PageResult<NoticeManagementListItemResult> findAllNotices(NoticeSearchCondition condition, PageQuery pageQuery);

    /**
     * 관리 상세 조회 — 비노출 공지도 조회된다.
     */
    Optional<NoticeDetailResult> findDetailById(Long id);
}
