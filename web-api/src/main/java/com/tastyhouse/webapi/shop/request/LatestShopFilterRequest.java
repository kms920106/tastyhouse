package com.tastyhouse.webapi.shop.request;

import com.tastyhouse.core.domain.shop.domain.model.Amenity;
import com.tastyhouse.core.domain.shop.domain.model.FoodType;

import java.util.List;

public record LatestShopFilterRequest(
    Long stationId,
    List<FoodType> foodTypes,
    List<Amenity> amenities
) {
}
