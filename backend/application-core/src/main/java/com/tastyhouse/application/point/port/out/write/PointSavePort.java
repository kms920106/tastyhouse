package com.tastyhouse.application.point.port.out.write;

import com.tastyhouse.domain.point.model.Point;

public interface PointSavePort {

    Point save(Point point);
}
