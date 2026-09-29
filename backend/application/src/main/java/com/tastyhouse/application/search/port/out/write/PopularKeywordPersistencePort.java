package com.tastyhouse.application.search.port.out.write;

import java.util.List;

import com.tastyhouse.domain.search.model.PopularKeyword;

public interface PopularKeywordPersistencePort {

    List<PopularKeyword> findActiveOrderByRank();

    List<PopularKeyword> saveAll(List<PopularKeyword> keywords);

    void deleteAll();
}
