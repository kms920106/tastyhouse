package com.tastyhouse.core.domain.search.domain.repository;

import com.tastyhouse.core.domain.search.domain.model.PopularKeyword;

import java.util.List;

public interface PopularKeywordRepository {

    List<PopularKeyword> findActiveOrderByRank();

    List<PopularKeyword> saveAll(List<PopularKeyword> keywords);

    void deleteAll();
}
