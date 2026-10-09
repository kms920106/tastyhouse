package com.tastyhouse.application.review.port.out.write;

import com.tastyhouse.domain.review.model.ReviewBlindRequest;

public interface ReviewBlindRequestSavePort {

    ReviewBlindRequest save(ReviewBlindRequest reviewBlindRequest);
}
