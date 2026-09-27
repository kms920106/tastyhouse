package com.tastyhouse.application.search.store;

import java.util.List;

import com.tastyhouse.domain.search.model.PopularKeyword;
import com.tastyhouse.application.search.port.out.write.PopularKeywordState;
import com.tastyhouse.application.search.port.out.write.PopularKeywordStatePort;

public class PopularKeywordStore implements PopularKeywordRepository {
    private final PopularKeywordStatePort popularKeywordStatePort;

    public PopularKeywordStore(PopularKeywordStatePort popularKeywordStatePort) {
        this.popularKeywordStatePort = popularKeywordStatePort;
    }

    @Override
    public List<PopularKeyword> findActiveOrderByRank() {
        return popularKeywordStatePort.findActiveOrderByRank().stream()
            .map(PopularKeywordStateMapper::toDomain)
            .toList();
    }

    @Override
    public List<PopularKeyword> saveAll(List<PopularKeyword> keywords) {
        List<PopularKeywordState> states = keywords.stream()
            .map(PopularKeywordStateMapper::toState)
            .toList();
        return popularKeywordStatePort.saveAll(states).stream()
            .map(PopularKeywordStateMapper::toDomain)
            .toList();
    }

    @Override
    public void deleteAll() {
        popularKeywordStatePort.deleteAll();
    }
}
