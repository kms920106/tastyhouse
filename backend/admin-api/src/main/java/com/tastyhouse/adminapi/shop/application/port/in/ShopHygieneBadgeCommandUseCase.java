package com.tastyhouse.adminapi.shop.application.port.in;

/** 위생 인증 뱃지 관리 쓰기 인바운드 포트(admin). */
public interface ShopHygieneBadgeCommandUseCase {

    Long createHygieneBadge(ShopHygieneBadgeCreateCommand command);

    void deleteHygieneBadge(ShopHygieneBadgeDeleteCommand command);
}
