package com.tastyhouse.application.menureview.port.out;

import java.time.LocalDateTime;
import java.util.List;

public interface MenuReviewStatisticsQueryPort {

    Long countVisibleByProductId(Long productId);

    Double getAverageRatingByProductId(Long productId);

    List<MenuReviewMemberCountResult> countByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate);
}
