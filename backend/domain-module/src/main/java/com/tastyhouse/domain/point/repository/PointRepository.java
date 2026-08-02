package com.tastyhouse.domain.point.repository;

import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.model.Point;

public interface PointRepository {

    Optional<Point> findByMemberId(MemberId memberId);

    Point save(Point point);
}
