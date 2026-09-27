package com.tastyhouse.application.point.store;

import java.util.Optional;

import com.tastyhouse.application.point.port.out.write.PointStatePort;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.model.Point;

public class PointStore implements PointRepository {
    private final PointStatePort pointStatePort;

    public PointStore(PointStatePort pointStatePort) {
        this.pointStatePort = pointStatePort;
    }

    @Override
    public Optional<Point> findByMemberId(MemberId memberId) {
        return pointStatePort.findByMemberId(memberId.value()).map(PointStateMapper::toDomain);
    }

    @Override
    public Point save(Point point) {
        return PointStateMapper.toDomain(pointStatePort.save(PointStateMapper.toState(point)));
    }
}
