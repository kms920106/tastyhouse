package com.tastyhouse.application.menureview.port.out;

import java.time.LocalDateTime;

public record MenuReviewMemberCountResult(
    Long memberId,
    Long menuReviewCount,
    LocalDateTime lastMenuReviewAt
) {
}
