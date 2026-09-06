package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDateTime;
import java.util.List;

public interface MemberReviewCountQueryPort {
    List<MemberReviewCountResult> countReviewsByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate);
}
