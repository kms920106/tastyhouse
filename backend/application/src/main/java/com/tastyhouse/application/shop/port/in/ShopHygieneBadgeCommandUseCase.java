package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface ShopHygieneBadgeCommandUseCase {

    Long createHygieneBadge(ShopHygieneBadgeCreateCommand command);

    void deleteHygieneBadge(ShopHygieneBadgeDeleteCommand command);
}
