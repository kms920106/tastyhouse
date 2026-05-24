package com.tastyhouse.core.domain.search.application.dto;

import com.tastyhouse.core.domain.search.domain.model.RecommendedKeyword;

public record RecommendedKeywordResult(String keyword) {

    public static RecommendedKeywordResult from(RecommendedKeyword recommendedKeyword) {
        return new RecommendedKeywordResult(recommendedKeyword.getKeyword());
    }
}
