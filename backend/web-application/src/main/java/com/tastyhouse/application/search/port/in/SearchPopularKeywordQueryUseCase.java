package com.tastyhouse.application.search.port.in;

import java.util.List;

import com.tastyhouse.application.search.port.out.PopularKeywordResult;

public interface SearchPopularKeywordQueryUseCase {

    List<PopularKeywordResult> getPopularKeywords();
}
