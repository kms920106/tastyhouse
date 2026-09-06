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

    public MenuReviewListItemResult withMemberProfileImageUrl(String memberProfileImageUrl) {
        return new MenuReviewListItemResult(
            this.id,
            this.memberNickname,
            memberProfileImageUrl,
            this.rating,
            this.comment,
            this.createdAt
        );
    }
}
