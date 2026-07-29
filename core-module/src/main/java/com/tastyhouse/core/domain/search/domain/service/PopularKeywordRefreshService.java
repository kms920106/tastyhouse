package com.tastyhouse.core.domain.search.domain.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.tastyhouse.core.domain.search.domain.model.PopularKeyword;
import com.tastyhouse.core.domain.search.domain.repository.PopularKeywordRepository;
import com.tastyhouse.core.domain.search.domain.repository.SearchKeywordLogRepository;

/**
 * 인기 검색어 갱신 규칙(도메인 서비스).
 *
 * <p>"인기 검색어 목록은 최근 검색 로그 집계 결과로 통째 교체되며, 교체 전 목록에 없던 키워드만
 * 신규(new)로 표시된다"는 규칙은 한 애그리거트만으로 판단할 수 없다 — 기존 인기 검색어 전체를 읽어
 * 신규 여부를 판정하고, 삭제 후 새 목록을 한 트랜잭션에서 원자적으로 다시 저장해야 하므로 같은
 * 애그리거트 타입의 여러 인스턴스를 load &amp; save 한다(공통 지침 분류 C). 스케줄러(batch)가 트리거하는
 * 액터 무관 연산이라 특정 소비 모듈의 command 서비스가 아니라 도메인 계층에 두어, 다른 모듈에서
 * 호출해도 갱신 규칙이 갈리지 않게 한다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 호출자(batch-module의
 * {@code SearchKeywordSchedulerService})의 트랜잭션 안에서 실행되므로, {@code deleteAll} 이후
 * {@code saveAll}이 같은 트랜잭션에서 원자적으로 수행된다.
 */
public class PopularKeywordRefreshService {

    /**
     * 집계 대상 기간 — 최근 7일간의 검색 로그를 집계한다.
     */
    private static final int AGGREGATION_WINDOW_DAYS = 7;

    /**
     * 검색 로그 보관 기간 — 30일이 지난 로그는 정리한다.
     */
    private static final int LOG_RETENTION_DAYS = 30;

    private final SearchKeywordLogRepository searchKeywordLogRepository;
    private final PopularKeywordRepository popularKeywordRepository;

    public PopularKeywordRefreshService(
        SearchKeywordLogRepository searchKeywordLogRepository,
        PopularKeywordRepository popularKeywordRepository
    ) {
        this.searchKeywordLogRepository = searchKeywordLogRepository;
        this.popularKeywordRepository = popularKeywordRepository;
    }

    /**
     * 최근 검색 로그를 집계해 인기 검색어 목록을 통째로 교체한다.
     *
     * <p>교체 전 목록의 키워드 집합을 먼저 확보해, 새 목록에서 그 집합에 없던 키워드만 신규로 표시한다.
     * 도메인이 프레임워크-프리라 더티 체킹이 없으므로 새 목록은 명시적으로 저장한다.
     */
    public void refresh() {
        LocalDateTime since = LocalDateTime.now().minusDays(AGGREGATION_WINDOW_DAYS);
        List<Object[]> rows = searchKeywordLogRepository.findTop10KeywordsSince(since);

        Set<String> previousKeywords = popularKeywordRepository.findActiveOrderByRank().stream()
            .map(PopularKeyword::getKeyword)
            .collect(Collectors.toSet());

        popularKeywordRepository.deleteAll();

        List<PopularKeyword> newRanks = new ArrayList<>();
        int rank = 1;
        for (Object[] row : rows) {
            String keyword = (String) row[0];
            newRanks.add(PopularKeyword.of(keyword, rank++, !previousKeywords.contains(keyword)));
        }
        popularKeywordRepository.saveAll(newRanks);
    }

    /**
     * 보관 기간이 지난 검색 로그를 정리한다.
     */
    public void deleteOldSearchLogs() {
        searchKeywordLogRepository.deleteOlderThan(LocalDateTime.now().minusDays(LOG_RETENTION_DAYS));
    }
}
