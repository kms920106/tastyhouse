package com.tastyhouse.webapplication.review.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 목록 응답")
public record ReviewCommentListResponse(
    @Schema(description = "댓글 목록")
    List<ReviewCommentResponse> comments,

    @Schema(description = "총 댓글 수 (답글 포함)", example = "15")
    int totalCount
) {
    public static ReviewCommentListResponse from(
        List<ReviewCommentResponse> comments,
        int totalCount
    ) {
        return new ReviewCommentListResponse(
            comments,
            totalCount
        );
    }
}
