package com.tastyhouse.application.rank.port.in;

import java.util.Optional;

import com.tastyhouse.application.rank.port.out.RankDurationResult;

public interface RankDurationQueryUseCase {

    Optional<RankDurationResult> getDuration();
}
