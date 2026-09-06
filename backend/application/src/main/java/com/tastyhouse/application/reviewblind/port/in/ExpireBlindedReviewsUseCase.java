package com.tastyhouse.application.reviewblind.port.in;

import com.tastyhouse.application.shared.marker.BatchApp;

@BatchApp
public interface ExpireBlindedReviewsUseCase {

    void expireBlindedReviews();
}
