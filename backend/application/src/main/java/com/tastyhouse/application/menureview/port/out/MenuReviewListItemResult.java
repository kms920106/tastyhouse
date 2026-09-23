package com.tastyhouse.application.menureview.port.out;

import java.time.LocalDateTime;

public record MenuReviewListItemResult(
    Long id,
    String memberNickname,
    String memberProfileImageUrl,
    Integer rating,
    String comment,
    LocalDateTime createdAt
) {
}
