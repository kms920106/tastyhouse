package com.tastyhouse.application.review.port.in;

public interface ReviewBlindRequestOwnerCommandUseCase {

    Long request(ReviewBlindRequestCreateCommand command);

    void cancel(ReviewBlindRequestCancelCommand command);
}
