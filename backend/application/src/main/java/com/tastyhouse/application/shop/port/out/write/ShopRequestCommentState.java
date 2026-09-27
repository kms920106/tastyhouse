package com.tastyhouse.application.shop.port.out.write;

import java.time.LocalDateTime;

public record ShopRequestCommentState(
    Long id,
    Long shopRequestIndexId,
    String authorType,
    Long authorId,
    String content,
    LocalDateTime createdAt
) {
}
