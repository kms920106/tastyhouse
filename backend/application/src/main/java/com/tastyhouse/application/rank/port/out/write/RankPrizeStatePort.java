package com.tastyhouse.application.rank.port.out.write;

import java.util.Optional;

public interface RankPrizeStatePort {
    RankPrizeState save(RankPrizeState state);

    Optional<RankPrizeState> findById(Long id);

    void delete(Long id);
}
