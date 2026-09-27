package com.tastyhouse.application.review.port.out.write;

import java.util.List;

public interface ReviewTagStatePort {
    void saveAll(List<ReviewTagState> states);

    void deleteByReviewId(Long reviewId);
}
