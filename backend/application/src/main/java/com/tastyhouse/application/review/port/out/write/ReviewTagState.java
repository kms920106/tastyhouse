package com.tastyhouse.application.review.port.out.write;

public record ReviewTagState(
    Long id,
    Long reviewId,
    Long tagId
) {
}
