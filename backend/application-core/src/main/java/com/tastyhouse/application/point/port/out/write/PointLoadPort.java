package com.tastyhouse.application.point.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.model.Point;

public interface PointLoadPort {

    Optional<Point> findByMemberId(MemberId memberId);
}
