package com.tastyhouse.application.search.port.out;

import java.time.LocalDateTime;
import java.util.List;

public interface KeywordCountPort {
    List<KeywordCount> findTopKeywordsSince(LocalDateTime since);
}
