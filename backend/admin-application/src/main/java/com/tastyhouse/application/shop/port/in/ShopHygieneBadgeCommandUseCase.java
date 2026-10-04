package com.tastyhouse.application.shop.port.in;

public interface ShopHygieneBadgeCommandUseCase {

    Long createHygieneBadge(ShopHygieneBadgeCreateCommand command);

    void deleteHygieneBadge(ShopHygieneBadgeDeleteCommand command);
}
