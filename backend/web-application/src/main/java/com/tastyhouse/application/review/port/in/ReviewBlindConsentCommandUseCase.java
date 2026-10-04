package com.tastyhouse.application.review.port.in;

public interface ReviewBlindConsentCommandUseCase {

    void consent(ReviewBlindConsentCommand command);

    void reject(ReviewBlindRejectCommand command);
}
