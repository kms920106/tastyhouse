package com.tastyhouse.application.review.port.in;

public interface ReviewCreateUseCase {

    Long createReview(ReviewCreateCommand command);
}
