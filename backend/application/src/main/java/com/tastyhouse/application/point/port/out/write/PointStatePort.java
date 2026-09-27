package com.tastyhouse.application.point.port.out.write;

import java.util.Optional;

public interface PointStatePort {

    Optional<PointState> findByMemberId(Long memberId);

    PointState save(PointState state);
}
