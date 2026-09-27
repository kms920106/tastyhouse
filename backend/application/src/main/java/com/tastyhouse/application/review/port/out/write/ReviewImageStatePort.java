package com.tastyhouse.application.review.port.out.write;

import java.util.List;

public interface ReviewImageStatePort {

    void saveAll(List<ReviewImageState> states);

    void deleteByReviewId(Long reviewId);
}
