package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.ShopRequestCommentAuthorType;

public record ShopRequestCommentResult(
    Long commentId,
    ShopRequestCommentAuthorType authorType,
    String content,
    LocalDateTime createdAt
) {
}
