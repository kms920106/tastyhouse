package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopAmenityCategory;

public interface ShopAmenityLoadPort {

    Optional<ShopAmenityCategory> findAmenityCategoryById(Long id);
}
