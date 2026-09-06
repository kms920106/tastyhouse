package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface ReviewBlindConsentCommandUseCase {

    void consent(ReviewBlindConsentCommand command);

    void reject(ReviewBlindRejectCommand command);
}
