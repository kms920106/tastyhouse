package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopMapMarkerResult;

public interface ShopMapMarkerQueryUseCase {

    List<ShopMapMarkerResult> searchMapMarkers(Double latitude, Double longitude);
}
