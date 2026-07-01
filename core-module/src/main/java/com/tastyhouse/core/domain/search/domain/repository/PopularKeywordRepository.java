package com.tastyhouse.core.domain.search.domain.repository;

import java.util.List;

import com.tastyhouse.core.domain.search.domain.model.PopularKeyword;

public interface PopularKeywordRepository {

    List<PopularKeyword> findActiveOrderByRank();

    List<PopularKeyword> saveAll(List<PopularKeyword> keywords);

    void deleteAll();
}
