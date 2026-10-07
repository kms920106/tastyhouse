package com.tastyhouse.application.rank.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.rank.model.RankType;
import com.tastyhouse.application.rank.port.in.RankAggregateCommand;
import com.tastyhouse.application.rank.port.in.RankAggregateUseCase;

@Service
@Transactional
class RankAggregateService implements RankAggregateUseCase {

    private static final int DEFAULT_AGGREGATE_LIMIT = 10;

    private final RankSettlementService rankSettlementService;

    public RankAggregateService(RankSettlementService rankSettlementService) {
        this.rankSettlementService = rankSettlementService;
    }

    @Override
    public void aggregate(RankAggregateCommand command) {
        String type = command.type();
        if (type == null) {
            rankSettlementService.settleAll(LocalDate.now());
            return;
        }

        LocalDate baseDate = command.baseDate();
        Integer limit = command.limit();
        RankType rankType = RankType.from(type);
        LocalDate targetDate = baseDate != null ? baseDate : LocalDate.now();
        int targetLimit = limit != null ? limit : DEFAULT_AGGREGATE_LIMIT;
        rankSettlementService.settle(rankType, targetDate, targetLimit);
    }
}
