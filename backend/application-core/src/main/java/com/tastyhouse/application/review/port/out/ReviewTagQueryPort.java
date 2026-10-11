package com.tastyhouse.application.review.port.out;

import java.util.List;

public interface ReviewTagQueryPort {

    List<Long> findTagIdsByReviewId(Long reviewId);

    List<String> findTagNamesByIds(List<Long> tagIds);
}
