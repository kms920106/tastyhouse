package com.tastyhouse.application.search.port.out;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchQueryPort {

    List<PopularKeywordResult> findVisiblePopularKeywords();

    List<RecommendedKeywordResult> findVisibleRecommendedKeywords();

    List<KeywordCountResult> findTopKeywordsSince(LocalDateTime since);
}
