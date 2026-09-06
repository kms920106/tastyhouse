package com.tastyhouse.application.point.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface PointCommandUseCase {

    void earnPoint(PointEarnCommand command);

    void deductPoint(PointDeductCommand command);
}
