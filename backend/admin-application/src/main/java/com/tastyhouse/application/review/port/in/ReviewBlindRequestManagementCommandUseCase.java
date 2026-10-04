package com.tastyhouse.application.review.port.in;

public interface ReviewBlindRequestManagementCommandUseCase {

    void approveBlindRequest(ReviewBlindRequestApproveCommand command);

    void rejectBlindRequest(ReviewBlindRequestRejectCommand command);
}
