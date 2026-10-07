package com.tastyhouse.application.review.port.in;

public interface ReviewBlindRequestOwnerCancelUseCase {

    void cancel(ReviewBlindRequestCancelCommand command);
}
