package com.tastyhouse.application.search.port.in;

import java.util.List;

import com.tastyhouse.application.search.port.out.RecommendedKeywordResult;

public interface SearchRecommendedKeywordQueryUseCase {

    List<RecommendedKeywordResult> getRecommendedKeywords();
}
