package com.tastyhouse.domain.point.domain.repository;

import java.util.Optional;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.point.domain.model.Point;

public interface PointRepository {

    Optional<Point> findByMemberId(MemberId memberId);

    Point save(Point point);
}
