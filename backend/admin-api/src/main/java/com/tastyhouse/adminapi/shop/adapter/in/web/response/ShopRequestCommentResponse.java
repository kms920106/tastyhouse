package com.tastyhouse.adminapi.shop.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopRequestCommentResult;

@Schema(description = "요청건 문의 스레드 항목")
public record ShopRequestCommentResponse(

    @Schema(description = "댓글 ID", example = "88")
    Long commentId,

    @Schema(description = "작성자 유형 코드", example = "CEO", allowableValues = {"CEO", "ADMIN"})
    String authorType,

    @Schema(description = "작성자 유형 한글 라벨", example = "점주")
    String authorTypeDescription,

    @Schema(description = "내용", example = "반려 사유를 좀 더 자세히 알려주실 수 있나요?")
    String content,

    @Schema(description = "작성 일시", example = "2026-08-12T09:20:11")
    LocalDateTime createdAt
) {
    public static ShopRequestCommentResponse from(ShopRequestCommentResult result) {
        return new ShopRequestCommentResponse(
            result.commentId(),
            result.authorType().name(),
            result.authorType().getDescription(),
            result.content(),
            result.createdAt()
        );
    }
}
