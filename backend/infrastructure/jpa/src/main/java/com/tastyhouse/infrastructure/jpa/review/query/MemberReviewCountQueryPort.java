package com.tastyhouse.infrastructure.jpa.review.query;

import java.time.LocalDateTime;
import java.util.List;

public interface MemberReviewCountQueryPort {

    List<MemberReviewCountResult> countReviewsByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate);
}
