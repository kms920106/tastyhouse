package com.tastyhouse.application.notice.port.out;

import java.time.LocalDateTime;

/**
 * 공지사항 상세 조회 결과.
 *
 * <p>비-admin 형제가 없어 {@code Management} 한정어 없이 순수명을 쓴다. 식별자는 HTTP 경계까지
 * 그대로 전달되는 표현용 값이므로 도메인 VO가 아니라 {@code Long}으로 투영한다.
 */
public record NoticeDetailResult(
    Long id,
    String title,
    String content,
    boolean visible,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
