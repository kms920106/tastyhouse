package com.tastyhouse.ceoapi.shop.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 요청건 문의 스레드 항목 응답.
 *
 * <p>작성자 실명·식별자는 노출하지 않는다 — 관리자 실명 노출을 피하고, 화면은 작성자 유형 라벨
 * ("점주"/"담당자")로 구성한다.
 */
@Schema(description = "요청건 문의 스레드 항목")
public record ShopRequestCommentResponse(

    @Schema(description = "댓글 ID", example = "88")
    Long commentId,

    @Schema(description = "작성자 유형 코드", example = "ADMIN", allowableValues = {"CEO", "ADMIN"})
    String authorType,

    @Schema(description = "작성자 유형 한글 라벨", example = "담당자")
    String authorTypeDescription,

    @Schema(description = "내용", example = "제출하신 동의서의 서명 페이지가 누락되어 있습니다.")
    String content,

    @Schema(description = "작성 일시", example = "2026-08-12T09:20:11")
    LocalDateTime createdAt
) {

    public static ShopRequestCommentResponse from(
        Long commentId,
        String authorType,
        String authorTypeDescription,
        String content,
        LocalDateTime createdAt
    ) {
        return new ShopRequestCommentResponse(
            commentId,
            authorType,
            authorTypeDescription,
            content,
            createdAt
        );
    }
}
