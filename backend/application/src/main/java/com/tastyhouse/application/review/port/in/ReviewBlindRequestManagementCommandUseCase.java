package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface ReviewBlindRequestManagementCommandUseCase {

    void approveBlindRequest(ReviewBlindRequestApproveCommand command);

    void rejectBlindRequest(ReviewBlindRequestRejectCommand command);
}
