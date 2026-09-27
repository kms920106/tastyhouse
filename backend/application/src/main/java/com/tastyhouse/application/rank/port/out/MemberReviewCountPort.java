package com.tastyhouse.application.rank.port.out;

import java.time.LocalDateTime;
import java.util.List;

public interface MemberReviewCountPort {
    List<MemberReviewCount> countReviewsByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate);
}
