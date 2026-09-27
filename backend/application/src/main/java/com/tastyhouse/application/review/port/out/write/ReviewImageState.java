package com.tastyhouse.application.review.port.out.write;

public record ReviewImageState(
    Long id,
    Long reviewId,
    Long imageFileId,
    Integer sort
) {
}
