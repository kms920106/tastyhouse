package com.tastyhouse.application.point.port.in;

public interface PointCommandUseCase {

    void earnPoint(PointEarnCommand command);

    void deductPoint(PointDeductCommand command);
}
