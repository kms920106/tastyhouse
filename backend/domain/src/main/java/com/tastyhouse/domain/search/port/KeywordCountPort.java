package com.tastyhouse.domain.search.port;

import java.time.LocalDateTime;
import java.util.List;

public interface KeywordCountPort {
    List<KeywordCount> findTopKeywordsSince(LocalDateTime since);
}
