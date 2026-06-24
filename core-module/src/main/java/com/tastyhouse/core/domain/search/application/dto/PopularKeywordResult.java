package com.tastyhouse.core.domain.search.application.dto;

import com.tastyhouse.core.domain.search.domain.model.PopularKeyword;

public record PopularKeywordResult(int rank, String keyword, boolean newKeyword) {

    public static PopularKeywordResult from(PopularKeyword popularKeyword) {
        return new PopularKeywordResult(
            popularKeyword.getRank(),
            popularKeyword.getKeyword(),
            popularKeyword.isNewKeyword()
        );
    }
}
