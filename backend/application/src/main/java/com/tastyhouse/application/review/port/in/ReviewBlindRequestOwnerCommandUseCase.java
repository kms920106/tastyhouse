package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ReviewBlindRequestOwnerCommandUseCase {

    Long request(ReviewBlindRequestCreateCommand command);

    void cancel(ReviewBlindRequestCancelCommand command);
}
