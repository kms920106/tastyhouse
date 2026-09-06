package com.tastyhouse.application.rank.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface RankCommandUseCase {

    void aggregate(RankAggregateCommand command);

    Long createPeriod(RankPeriodCreateCommand command);

    void updatePeriod(RankPeriodUpdateCommand command);

    void deletePeriod(RankPeriodDeleteCommand command);

    Long createPrize(RankPrizeCreateCommand command);

    void updatePrize(RankPrizeUpdateCommand command);

    void deletePrize(RankPrizeDeleteCommand command);
}
