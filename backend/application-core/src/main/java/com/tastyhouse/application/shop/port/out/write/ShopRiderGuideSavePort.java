package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopRiderGuide;
import com.tastyhouse.domain.shop.model.ShopRiderGuideHistory;

public interface ShopRiderGuideSavePort {

    ShopRiderGuide save(ShopRiderGuide riderGuide);

    ShopRiderGuideHistory saveHistory(ShopRiderGuideHistory history);
}
