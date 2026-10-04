package com.tastyhouse.application.product.port.in;

public interface StorePriceVerificationCommandUseCase {

    void startReview(StorePriceVerificationStartReviewCommand command);

    void approve(StorePriceVerificationApproveCommand command);

    void reject(StorePriceVerificationRejectCommand command);
}
