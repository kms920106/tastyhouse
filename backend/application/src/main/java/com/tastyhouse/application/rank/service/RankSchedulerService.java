package com.tastyhouse.application.rank.service;

import com.tastyhouse.application.shared.marker.BatchApp;
import com.tastyhouse.application.rank.port.in.AggregateRanksUseCase;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.rank.service.RankSettlementService;

@Service
@BatchApp
public class RankSchedulerService implements AggregateRanksUseCase {

    private static final Logger log = LoggerFactory.getLogger(RankSchedulerService.class);

    private final RankSettlementService rankSettlementService;

    public RankSchedulerService(RankSettlementService rankSettlementService) {
        this.rankSettlementService = rankSettlementService;
    }

    @Transactional
    @Override
    public void aggregateAllRanks() {
        LocalDate baseDate = LocalDate.now();

        int settled = rankSettlementService.settleAll(baseDate);

        log.info("랭킹 집계 완료: baseDate={}, 적재 {} 건", baseDate, settled);
    }
}
