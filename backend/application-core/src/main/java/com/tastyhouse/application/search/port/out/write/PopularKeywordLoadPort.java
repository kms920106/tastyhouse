package com.tastyhouse.application.search.port.out.write;

import java.util.List;

import com.tastyhouse.domain.search.model.PopularKeyword;

public interface PopularKeywordLoadPort {

    List<PopularKeyword> findVisibleOrderByRank();
}
