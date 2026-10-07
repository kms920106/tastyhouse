package com.tastyhouse.application.rank.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.rank.port.in.RankDurationQueryUseCase;
import com.tastyhouse.application.rank.port.out.RankDurationResult;
import com.tastyhouse.application.rank.port.out.RankQueryPort;

@Service
@Transactional(readOnly = true)
class RankDurationQueryService implements RankDurationQueryUseCase {

    private final RankQueryPort rankQueryPort;

    public RankDurationQueryService(RankQueryPort rankQueryPort) {
        this.rankQueryPort = rankQueryPort;
    }

    @Override
    public Optional<RankDurationResult> getDuration() {
        return rankQueryPort.findActiveDuration();
    }
}
