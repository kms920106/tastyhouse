package com.tastyhouse.application.search.store;

import com.tastyhouse.application.search.port.out.write.PopularKeywordState;
import com.tastyhouse.domain.search.model.PopularKeyword;

final class PopularKeywordStateMapper {
    private PopularKeywordStateMapper() {
    }

    static PopularKeyword toDomain(PopularKeywordState state) {
        return PopularKeyword.reconstitute(
            state.id(),
            state.keyword(),
            state.rank(),
            state.newKeyword(),
            state.visible()
        );
    }

    static PopularKeywordState toState(PopularKeyword popularKeyword) {
        return new PopularKeywordState(
            popularKeyword.getId(),
            popularKeyword.getKeyword(),
            popularKeyword.getRank(),
            popularKeyword.isNewKeyword(),
            popularKeyword.isVisible()
        );
    }
}
