package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.ShopRequestCommentAuthorType;

/**
 * 요청건 문의 스레드 항목 조회 결과.
 *
 * <p>반드시 {@code public}이어야 한다 — 이유는 {@code ShopRequestListItemResult} Javadoc 참조.
 *
 * <p>{@code authorId}는 투영하지 않는다 — 화면은 작성자 유형 라벨("점주"/"담당자")로 구성하고 실명·식별자를
 * 노출하지 않으므로, 담지 않으면 CEO/ADMIN 조인 2개도 절약된다.
 */
public record ShopRequestCommentResult(
    Long commentId,
    ShopRequestCommentAuthorType authorType,
    String content,
    LocalDateTime createdAt
) {
}
