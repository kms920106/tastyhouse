package com.tastyhouse.application.review.port.out;

public record ReviewSortSpec(
    boolean byLikeCount,
    boolean createdAtAscending
) {
}
