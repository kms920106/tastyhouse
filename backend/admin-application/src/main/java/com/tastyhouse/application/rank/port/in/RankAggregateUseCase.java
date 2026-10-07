package com.tastyhouse.application.rank.port.in;

public interface RankAggregateUseCase {

    void aggregate(RankAggregateCommand command);
}
