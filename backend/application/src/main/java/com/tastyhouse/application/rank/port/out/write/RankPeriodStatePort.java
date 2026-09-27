package com.tastyhouse.application.rank.port.out.write;

import java.util.Optional;

public interface RankPeriodStatePort {
    RankPeriodState save(RankPeriodState state);

    Optional<RankPeriodState> findById(Long id);

    void delete(Long id);
}
