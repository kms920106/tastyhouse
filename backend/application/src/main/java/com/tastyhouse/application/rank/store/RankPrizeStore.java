package com.tastyhouse.application.rank.store;

import java.util.Optional;

import com.tastyhouse.domain.rank.model.RankPrize;
import com.tastyhouse.domain.rank.vo.RankPrizeId;
import com.tastyhouse.application.rank.port.out.write.RankPrizeStatePort;

public class RankPrizeStore implements RankPrizeRepository {
    private final RankPrizeStatePort rankPrizeStatePort;

    public RankPrizeStore(RankPrizeStatePort rankPrizeStatePort) {
        this.rankPrizeStatePort = rankPrizeStatePort;
    }

    @Override
    public RankPrize save(RankPrize rankPrize) {
        return RankPrizeStateMapper.toDomain(rankPrizeStatePort.save(RankPrizeStateMapper.toState(rankPrize)));
    }

    @Override
    public Optional<RankPrize> findById(RankPrizeId id) {
        return rankPrizeStatePort.findById(id.value()).map(RankPrizeStateMapper::toDomain);
    }

    @Override
    public void delete(RankPrize rankPrize) {
        rankPrizeStatePort.delete(rankPrize.getId());
    }
}
