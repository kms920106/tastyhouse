package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface StorePriceVerificationCommandUseCase {

    void startReview(StorePriceVerificationStartReviewCommand command);

    void approve(StorePriceVerificationApproveCommand command);

    void reject(StorePriceVerificationRejectCommand command);
}
