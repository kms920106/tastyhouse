package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopHygieneBadge;

public interface ShopHygieneBadgeSavePort {

    ShopHygieneBadge save(ShopHygieneBadge shopHygieneBadge);

    void deleteById(Long id);
}
