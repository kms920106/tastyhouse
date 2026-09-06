package com.tastyhouse.application.rank.port.in;

import com.tastyhouse.application.shared.marker.BatchApp;

@BatchApp
public interface AggregateRanksUseCase {

    void aggregateAllRanks();
}
