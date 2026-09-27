package com.tastyhouse.application.search.store;

import java.util.List;

import com.tastyhouse.domain.search.model.PopularKeyword;

public interface PopularKeywordRepository {

    List<PopularKeyword> findActiveOrderByRank();

    List<PopularKeyword> saveAll(List<PopularKeyword> keywords);

    void deleteAll();
}
