package com.tastyhouse.application.rank.store;

import java.util.Optional;

import com.tastyhouse.domain.rank.model.RankPeriod;
import com.tastyhouse.domain.rank.vo.RankPeriodId;
import com.tastyhouse.application.rank.port.out.write.RankPeriodStatePort;

public class RankPeriodStore implements RankPeriodRepository {
    private final RankPeriodStatePort rankPeriodStatePort;

    public RankPeriodStore(RankPeriodStatePort rankPeriodStatePort) {
        this.rankPeriodStatePort = rankPeriodStatePort;
    }

    @Override
    public RankPeriod save(RankPeriod rankPeriod) {
        return RankPeriodStateMapper.toDomain(rankPeriodStatePort.save(RankPeriodStateMapper.toState(rankPeriod)));
    }

    @Override
    public Optional<RankPeriod> findById(RankPeriodId id) {
        return rankPeriodStatePort.findById(id.value()).map(RankPeriodStateMapper::toDomain);
    }

    @Override
    public void delete(RankPeriod rankPeriod) {
        rankPeriodStatePort.delete(rankPeriod.getId());
    }
}
