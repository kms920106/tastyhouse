package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

public record ShopRequestCommentResult(
    Long commentId,
    String authorType,
    String authorTypeDescription,
    String content,
    LocalDateTime createdAt
) {

    public ShopRequestCommentResult withAuthorTypeDescription(String authorTypeDescription) {
        return new ShopRequestCommentResult(
            this.commentId,
            this.authorType,
            authorTypeDescription,
            this.content,
            this.createdAt
        );
    }
}
