package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

/** 위생 인증 뱃지 관리 쓰기 인바운드 포트(admin). */
@AdminApp
public interface ShopHygieneBadgeCommandUseCase {

    Long createHygieneBadge(ShopHygieneBadgeCreateCommand command);

    void deleteHygieneBadge(ShopHygieneBadgeDeleteCommand command);
}
