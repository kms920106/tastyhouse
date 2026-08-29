package com.tastyhouse.ceoapi.shop.application.port.in;

/**
 * 점주 가게 주문안내 쓰기 인바운드 포트.
 */
public interface ShopOrderNoticeCommandUseCase {

    void upsertOrderNotice(ShopOrderNoticeUpsertCommand command);
}
