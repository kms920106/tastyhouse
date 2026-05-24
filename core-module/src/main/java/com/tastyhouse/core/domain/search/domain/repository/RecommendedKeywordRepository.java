package com.tastyhouse.core.domain.search.domain.repository;

import com.tastyhouse.core.domain.search.domain.model.RecommendedKeyword;

import java.util.List;

public interface RecommendedKeywordRepository {

    List<RecommendedKeyword> findActiveOrderBySortOrder();
}
