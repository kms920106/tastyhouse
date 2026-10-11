package com.tastyhouse.application.review.port.out.write;

import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.vo.ReviewId;

public interface ReviewSavePort {

    Review save(Review review);

    void deleteById(ReviewId reviewId);
}
