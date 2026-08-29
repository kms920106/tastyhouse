package com.tastyhouse.application.notice.port.out;

import java.time.LocalDateTime;

/**
 * 공지사항 목록 항목(web 노출용) 조회 결과.
 *
 * <p>노출(visible=true) 공지만 조회하므로 노출 여부 필드를 갖지 않는다. 관리 목록용 형제인
 * {@link NoticeManagementListItemResult}와 필드 셋이 달라 통합하지 않는다(과잉 노출 방지).
 */
public record NoticeListItemResult(
    Long id,
    String title,
    String content,
    LocalDateTime createdAt
) {
}
