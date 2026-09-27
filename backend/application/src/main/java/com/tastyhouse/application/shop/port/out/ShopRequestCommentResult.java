package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

public record ShopRequestCommentResult(
    Long commentId,
    String authorType,
    String authorTypeDescription,
    String content,
    LocalDateTime createdAt
) {
}
