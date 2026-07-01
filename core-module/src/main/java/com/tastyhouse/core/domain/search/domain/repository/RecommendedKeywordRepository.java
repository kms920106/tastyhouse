package com.tastyhouse.core.domain.search.domain.repository;

import java.util.List;

import com.tastyhouse.core.domain.search.domain.model.RecommendedKeyword;

public interface RecommendedKeywordRepository {

    List<RecommendedKeyword> findActiveOrderBySortOrder();
}
