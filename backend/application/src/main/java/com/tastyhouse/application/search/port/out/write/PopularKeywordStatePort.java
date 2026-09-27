package com.tastyhouse.application.search.port.out.write;

import java.util.List;

public interface PopularKeywordStatePort {

    List<PopularKeywordState> findActiveOrderByRank();

    List<PopularKeywordState> saveAll(List<PopularKeywordState> states);

    void deleteAll();
}
