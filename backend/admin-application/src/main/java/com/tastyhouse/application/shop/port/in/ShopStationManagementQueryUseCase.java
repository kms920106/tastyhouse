package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.StationResult;

public interface ShopStationManagementQueryUseCase {

    List<StationResult> getStations();
}
