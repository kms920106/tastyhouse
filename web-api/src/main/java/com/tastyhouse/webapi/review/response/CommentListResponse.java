package com.tastyhouse.webapi.review.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 목록 응답")
public record CommentListResponse(
    @Schema(description = "댓글 목록")
    List<CommentResponse> comments,

    @Schema(description = "총 댓글 수 (답글 포함)", example = "15")
    int totalCount
) {
    public static CommentListResponse from(
        List<CommentResponse> comments,
        int totalCount
    ) {
        return new CommentListResponse(
            comments,
            totalCount
        );
    }
}
